package com.infosys.ims.mapper;

import com.infosys.ims.dtos.response.ProductResponse;
import com.infosys.ims.dtos.response.ProductSupplierResponse;
import com.infosys.ims.dtos.response.SupplierProfileResponse;
import com.infosys.ims.entity.Product;
import com.infosys.ims.entity.ProductSupplier;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ProductMapper {

    private final CategoryMapper categoryMapper;

    public ProductResponse mapToResponse(Product product, List<ProductSupplier> productSuppliers) {
        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setSku(product.getSku());
        response.setProductName(product.getProductName());
        response.setDescription(product.getDescription());
        response.setCategory(categoryMapper.mapToResponse(product.getCategory()));
        response.setUnit(product.getUnit());
        response.setReorderLevel(product.getReorderLevel());
        response.setSellingPrice(product.getSellingPrice());
        response.setStatus(product.getStatus().name());
        response.setCreatedAt(product.getCreatedAt());

        if (productSuppliers != null) {
            response.setSuppliers(
                     productSuppliers.stream()
                            .map(ps -> mapToSupplierResponse(ps))
                            .collect(Collectors.toList())
            );
        } else {
            response.setSuppliers(Collections.emptyList());
        }
        return response;
    }

    public ProductSupplierResponse mapToSupplierResponse(ProductSupplier ps) {
        ProductSupplierResponse response = new ProductSupplierResponse();
        response.setId(ps.getId());
        response.setProductId(ps.getProduct().getId());
        response.setProductName(ps.getProduct().getProductName());
        response.setSku(ps.getProduct().getSku());
        response.setCategoryName(ps.getProduct().getCategory().getName());
        response.setSupplierId(ps.getSupplier().getId());
        response.setSupplierName(ps.getSupplier().getUser().getName());
        response.setSupplierUserCode(ps.getSupplier().getUser().getUserCode());
        response.setCompanyName(ps.getSupplier().getCompanyName());
        response.setPurchasePrice(ps.getPurchasePrice());
        response.setLeadTimeDays(ps.getLeadTimeDays());
        response.setIsPreferred(ps.getIsPreferred());
        response.setIsActive(ps.getIsActive());
        response.setCreatedAt(ps.getCreatedAt());
        return response;
    }
}
