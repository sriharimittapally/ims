package com.infosys.ims.dtos.response.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor
public class WarehouseStockReport {
    private Long warehouseId;
    private String warehouseName;
    private String warehouseCity;
    private String managerName;
    private long totalProducts;
    private long totalUnits;
    private long lowStockItems;
    private long outOfStockItems;
    private BigDecimal inventoryValue;
    private long totalStockIn30Days;
    private long totalStockOut30Days;
    private List<ProductStockRow> products;

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class ProductStockRow {
        private Long productId;
        private String sku;
        private String productName;
        private String categoryName;
        private String unit;
        private int quantity;
        private int reservedQuantity;
        private int availableQuantity;
        private int reorderLevel;
        private boolean lowStock;
        private boolean outOfStock;
        private BigDecimal sellingPrice;
        private BigDecimal stockValue;
        private String preferredSupplierName;
    }
}