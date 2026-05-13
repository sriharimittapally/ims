package com.infosys.ims.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardResponse {
    private long totalUsers;
    private long totalWarehouses;
    private long activeWarehouses;
    private long totalProducts;
    private long totalSuppliers;
    private long pendingSupplierApprovals;
    private long lowStockProducts;
    private long pendingPurchaseOrders;
    private BigDecimal totalInventoryValue;
}