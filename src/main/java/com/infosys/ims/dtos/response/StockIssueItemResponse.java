package com.infosys.ims.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockIssueItemResponse {
    private Long id;
    private Long productId;
    private String productName;
    private String sku;
    private String categoryName;
    private Integer quantityRequested;
    private Integer quantityIssued;
}