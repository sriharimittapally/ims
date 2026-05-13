package com.infosys.ims.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductAnalyticsResponse {
    private Long idd;
    private String productName;
    private String sku;
    private Integer currentStock;
    private BigDecimal currentStockValue;
    private Integer totalUnitsSold;
}