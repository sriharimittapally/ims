package com.infosys.ims.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ManagerDashboardResponse {
    private Long warehouseId;
    private String warehouseName;
    private long totalStaff;
    private long totalProducts;
    private long totalInventoryItems;
    private long lowStockAlerts;
    private long pendingPurchaseOrders;
    private long pendingIssues;
    private BigDecimal warehouseInventoryValue;
}