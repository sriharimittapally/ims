package com.infosys.ims.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderItemResponse {
    private Long id;
    private Long productId;
    private String productName;
    private String sku;
    private String categoryName;
    private Integer quantity;
    private BigDecimal purchasePrice;
    private BigDecimal lineTotal;
}