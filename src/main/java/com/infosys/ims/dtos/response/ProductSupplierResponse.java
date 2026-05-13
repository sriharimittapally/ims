package com.infosys.ims.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductSupplierResponse {
    private Long id;
    private Long productId;
    private String productName;
    private String sku;
    private String categoryName;
    private Long supplierId;
    private String supplierName;
    private String supplierUserCode;
    private String companyName;
    private BigDecimal purchasePrice;
    private Integer leadTimeDays;
    private Boolean isPreferred;
    private Boolean isActive;
    private LocalDateTime createdAt;
}