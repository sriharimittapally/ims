package com.infosys.ims.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockMovementResponse {
    private Long id;
    private Long productId;
    private String productName;
    private String sku;
    private Long warehouseId;
    private String warehouseName;
    private String type;
    private Integer quantity;
    private Integer quantityAfter;
    private Long referenceId;
    private String referenceType;
    private String note;
    private String createdByName;
    private LocalDateTime createdAt;
}