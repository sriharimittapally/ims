package com.infosys.ims.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryResponse {
    private Long id;
    private Long productId;
    private String productName;
    private String sku;
    private String categoryName;
    private Long warehouseId;
    private String warehouseName;
    private String warehouseCity;
    private Integer quantity;
    private Integer reservedQuantity;
    private Integer availableQuantity;
    private Integer reorderLevel;
    private Boolean lowStock;
    private LocalDateTime updatedAt;
}