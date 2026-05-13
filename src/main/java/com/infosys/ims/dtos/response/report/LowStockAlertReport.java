package com.infosys.ims.dtos.response.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor
public class LowStockAlertReport {
    private long totalAlerts;
    private List<LowStockAlertRow> alerts;

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class LowStockAlertRow {
        private Long productId;
        private String sku;
        private String productName;
        private String categoryName;
        private Long warehouseId;
        private String warehouseName;
        private int currentStock;
        private int reservedQuantity;
        private int availableQuantity;
        private int reorderLevel;
        private int deficit;              // reorderLevel - availableQuantity
        private boolean hasPreferredSupplier;
        private String preferredSupplierName;
        private boolean autoDraftExists;  // whether system already drafted a PO
    }
}