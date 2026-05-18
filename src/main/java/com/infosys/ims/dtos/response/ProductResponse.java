package com.infosys.ims.dtos.response;

import com.infosys.ims.entity.ProductSupplier;
import com.infosys.ims.entity.Supplier;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    private Long id;
    private String sku;
    private String productName;
    private String description;
    private CategoryResponse category;
    private String unit;
    private Integer reorderLevel;
    private BigDecimal sellingPrice;
    private String status;
    private LocalDateTime createdAt;
    private List<ProductSupplierResponse> suppliers;
}