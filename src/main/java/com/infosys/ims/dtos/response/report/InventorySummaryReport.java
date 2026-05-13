// ═══════════════════════════════════════════════════════════════════
// FILE: dtos/response/report/InventorySummaryReport.java
// ═══════════════════════════════════════════════════════════════════
package com.infosys.ims.dtos.response.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor
public class InventorySummaryReport {
    private long totalProducts;
    private long totalQuantity;
    private long totalWarehouses;
    private long lowStockCount;
    private long outOfStockCount;
    private List<WarehouseInventoryRow> warehouseBreakdowns;

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class WarehouseInventoryRow {
        private Long warehouseId;
        private String warehouseName;
        private long totalProducts;
        private long totalQuantity;
        private long lowStockCount;
        private BigDecimal inventoryValue;
    }
}