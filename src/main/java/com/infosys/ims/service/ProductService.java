package com.infosys.ims.service;

import com.infosys.ims.dtos.request.ProductRequest;
import com.infosys.ims.dtos.request.SupplierLinkRequest;
import com.infosys.ims.dtos.response.ProductResponse;
import com.infosys.ims.dtos.response.ProductSupplierResponse;
import com.infosys.ims.entity.Product;

import java.math.BigDecimal;
import java.util.List;

public interface ProductService {

    ProductResponse createProduct(ProductRequest request);

    ProductResponse updateProduct(Long id, ProductRequest request);

    void deleteProduct(Long id);

    void activateProduct(Long id);

    void deactivateProduct(Long id);

    /** Manager sets which supplier is preferred for a product */
    void setPreferredSupplier(Long productId, Long productSupplierId);

    /** Admin/Manager removes a supplier link */
    void removeSupplierLink(Long productSupplierId);

    /** Supplier links themselves to a product with their price */
    ProductSupplierResponse linkProduct(Long productId, String supplierEmail, SupplierLinkRequest request);

    /** Supplier updates their own purchase price for a product link */
    ProductSupplierResponse updateMyPrice(Long productSupplierId, String supplierEmail, BigDecimal newPrice);

    ProductResponse getProductById(Long id);

    List<ProductResponse> getAllProducts();

    List<ProductResponse> getActiveProducts();

    List<ProductResponse> getProductsByCategory(Long categoryId);

    /** Products in this supplier's categories (for supplier to browse) */
    List<ProductResponse> getProductsInMyCategories(String supplierEmail);

    /** Products this supplier has already linked */
    List<ProductResponse> getMyLinkedProducts(String supplierEmail);

    Product getProductEntity(Long id);

    ProductSupplierResponse updateMyLink(Long productSupplierId, String name, SupplierLinkRequest request);
}