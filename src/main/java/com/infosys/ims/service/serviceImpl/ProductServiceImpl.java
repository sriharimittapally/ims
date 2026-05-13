package com.infosys.ims.service.serviceImpl;

import com.infosys.ims.dtos.request.ProductRequest;
import com.infosys.ims.dtos.request.SupplierLinkRequest;
import com.infosys.ims.dtos.response.ProductResponse;
import com.infosys.ims.dtos.response.ProductSupplierResponse;
import com.infosys.ims.entity.*;
import com.infosys.ims.enums.ApprovalStatus;
import com.infosys.ims.enums.CategoryStatus;
import com.infosys.ims.enums.ProductStatus;
import com.infosys.ims.exception.BadRequestException;
import com.infosys.ims.exception.DuplicateResourceException;
import com.infosys.ims.exception.ForbiddenOperationException;
import com.infosys.ims.exception.ResourceNotFoundException;
import com.infosys.ims.mapper.ProductMapper;
import com.infosys.ims.repository.*;
import com.infosys.ims.service.ProductService;
import com.infosys.ims.service.SupplierService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductSupplierRepository productSupplierRepository;
    private final SupplierRepository supplierRepository;
    private final ProductMapper productMapper;
    private final SupplierService supplierService;

    @Override
    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        if (productRepository.existsBySku(request.getSku())) {
            throw new DuplicateResourceException("SKU already exists: " + request.getSku());
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + request.getCategoryId()));

        if (category.getStatus() == CategoryStatus.INACTIVE) {
            throw new BadRequestException("Cannot add product to an inactive category");
        }

        Product product = new Product();
        product.setSku(request.getSku());
        product.setProductName(request.getProductName());
        product.setDescription(request.getDescription());
        product.setCategory(category);
        product.setUnit(request.getUnit());
        product.setSellingPrice(request.getSellingPrice());
        product.setReorderLevel(request.getReorderLevel());

        Product saved = productRepository.save(product);
        return productMapper.mapToResponse(saved, List.of());
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = getProductEntity(id);

        // SKU uniqueness check (excluding self)
        productRepository.findBySku(request.getSku()).ifPresent(p -> {
            if (!p.getId().equals(id)) {
                throw new DuplicateResourceException("SKU already in use: " + request.getSku());
            }
        });

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + request.getCategoryId()));

        product.setSku(request.getSku());
        product.setProductName(request.getProductName());
        product.setDescription(request.getDescription());
        product.setCategory(category);
        product.setUnit(request.getUnit());
        product.setSellingPrice(request.getSellingPrice());
        product.setReorderLevel(request.getReorderLevel());

        Product saved = productRepository.save(product);
        List suppliers = productSupplierRepository.findByProduct(saved);
        return productMapper.mapToResponse(saved, suppliers);
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        Product product = getProductEntity(id);
        product.setStatus(ProductStatus.INACTIVE);
        productRepository.save(product);
    }

    @Override
    @Transactional
    public void activateProduct(Long id) {
        Product product = getProductEntity(id);
        product.setStatus(ProductStatus.ACTIVE);
        productRepository.save(product);
    }

    @Override
    @Transactional
    public void deactivateProduct(Long id) {
        Product product = getProductEntity(id);
        product.setStatus(ProductStatus.INACTIVE);
        productRepository.save(product);
    }

    @Override
    @Transactional
    public void setPreferredSupplier(Long productId, Long productSupplierId) {
        Product product = getProductEntity(productId);

        // Clear existing preferred
        productSupplierRepository.findByProductAndIsPreferredTrue(product).ifPresent(existing -> {
            existing.setIsPreferred(false);
            productSupplierRepository.save(existing);
        });

        // Set new preferred
        ProductSupplier link = productSupplierRepository.findById(productSupplierId)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier link not found: " + productSupplierId));

        if (!link.getProduct().getId().equals(productId)) {
            throw new BadRequestException("This supplier link does not belong to the specified product");
        }
        if (!link.getIsActive()) {
            throw new BadRequestException("Cannot set an inactive supplier link as preferred");
        }

        link.setIsPreferred(true);
        productSupplierRepository.save(link);
    }

    @Override
    @Transactional
    public void removeSupplierLink(Long productSupplierId) {
        ProductSupplier link = productSupplierRepository.findById(productSupplierId)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier link not found: " + productSupplierId));
        link.setIsActive(false);
        link.setIsPreferred(false);
        productSupplierRepository.save(link);
    }

    @Override
    @Transactional
    public ProductSupplierResponse linkProduct(Long productId, String supplierEmail, SupplierLinkRequest request) {
        Supplier supplier = supplierService.getApprovedSupplierEntity(supplierEmail);
        Product product = getProductEntity(productId);

        if (product.getStatus() == ProductStatus.INACTIVE) {
            throw new BadRequestException("Cannot link to an inactive product");
        }

        // Validate product category is in supplier's categories
        boolean categoryAllowed = supplier.getCategories().stream()
                .anyMatch(c -> c.getId().equals(product.getCategory().getId()));
        if (!categoryAllowed) {
            throw new ForbiddenOperationException("This product's category is not in your approved categories");
        }

        // Check if already linked
        if (productSupplierRepository.existsByProductAndSupplier(product, supplier)) {
            throw new DuplicateResourceException("You have already linked to this product");
        }

        ProductSupplier ps = new ProductSupplier();
        ps.setProduct(product);
        ps.setSupplier(supplier);
        ps.setPurchasePrice(request.getPurchasePrice());
        ps.setLeadTimeDays(request.getLeadTimeDays());

        ProductSupplier saved = productSupplierRepository.save(ps);
        return productMapper.mapToSupplierResponse(saved);
    }

    @Override
    @Transactional
    public ProductSupplierResponse updateMyPrice(Long productSupplierId, String supplierEmail, BigDecimal newPrice) {
        Supplier supplier = supplierService.getSupplierEntity(supplierEmail);

        ProductSupplier link = productSupplierRepository.findById(productSupplierId)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier link not found: " + productSupplierId));

        if (!link.getSupplier().getId().equals(supplier.getId())) {
            throw new ForbiddenOperationException("You can only update your own price");
        }

        link.setPurchasePrice(newPrice);
        return productMapper.mapToSupplierResponse(productSupplierRepository.save(link));
    }

    @Override
    public ProductResponse getProductById(Long id) {
        Product product = getProductEntity(id);
        List suppliers = productSupplierRepository.findByProduct(product);
        return productMapper.mapToResponse(product, suppliers);
    }

    @Override
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream()
                .map(p -> productMapper.mapToResponse(p, productSupplierRepository.findByProduct(p)))
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductResponse> getActiveProducts() {
        return productRepository.findByStatus(ProductStatus.ACTIVE).stream()
                .map(p -> productMapper.mapToResponse(p, productSupplierRepository.findByProduct(p)))
                .collect(Collectors.toList());
    }

    @Override
    public List getProductsByCategory(Long categoryId) {
        return productRepository.findByCategory_Id(categoryId).stream()
                .map(p -> productMapper.mapToResponse(p, productSupplierRepository.findByProduct(p)))
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductResponse> getProductsInMyCategories(String supplierEmail) {

        Supplier supplier = supplierService
                .getApprovedSupplierEntity(supplierEmail);

        List<Long> categoryIds = supplier.getCategories()
                .stream()
                .map(Category::getId)
                .toList();

        return productRepository.findByCategory_IdIn(categoryIds)
                .stream()
                .filter(product ->
                        product.getStatus() == ProductStatus.ACTIVE
                )
                .map(product ->
                        productMapper.mapToResponse(product, List.of())
                )
                .toList();
    }

    @Override
    public List<ProductResponse> getMyLinkedProducts(String supplierEmail) {
        Supplier supplier = supplierService.getSupplierEntity(supplierEmail);
        return productSupplierRepository.findBySupplier(supplier).stream()
                .filter(ProductSupplier::getIsActive)
                .map(ps -> productMapper.mapToResponse(
                        ps.getProduct(),
                        productSupplierRepository.findByProduct(ps.getProduct())
                ))
                .collect(Collectors.toList());
    }

    @Override
    public Product getProductEntity(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
    }

    @Override
    @Transactional
    public ProductSupplierResponse updateMyLink(Long productSupplierId, String supplierEmail, SupplierLinkRequest request) {
        Supplier supplier = supplierService.getSupplierEntity(supplierEmail);
        ProductSupplier link = productSupplierRepository.findById(productSupplierId)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier link not found: " + productSupplierId));
        if (!link.getSupplier().getId().equals(supplier.getId())) {
            throw new ForbiddenOperationException("You can only update your own link");
        }
        link.setPurchasePrice(request.getPurchasePrice());
        link.setLeadTimeDays(request.getLeadTimeDays());
        return productMapper.mapToSupplierResponse(productSupplierRepository.save(link));
    }
}