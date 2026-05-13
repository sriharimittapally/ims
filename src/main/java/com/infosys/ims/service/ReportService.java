package com.infosys.ims.service;

import com.infosys.ims.dtos.response.report.*;

import java.time.LocalDate;

public interface ReportService {

    // ── ADMIN: global scope ───────────────────────────────────────────────
    InventorySummaryReport getAdminInventorySummary();

    LowStockAlertReport getAdminLowStockAlerts();

    PurchaseOrderReport getAdminPurchaseOrderReport();

    SupplierPerformanceReport getAdminSupplierPerformanceReport();

    StockTrendReport getAdminStockTrend(LocalDate from, LocalDate to);

    TopProductsReport getAdminTopProducts();

    WarehouseStockReport getAdminWarehouseReport(Long warehouseId);

    // ── MANAGER: his warehouse only ───────────────────────────────────────
    WarehouseStockReport getManagerWarehouseReport(String managerEmail);

    LowStockAlertReport getManagerLowStockAlerts(String managerEmail);

    StockTrendReport getManagerStockTrend(String managerEmail, LocalDate from, LocalDate to);

    PurchaseOrderReport getManagerPOReport(String managerEmail);

    StaffActivityReport getManagerStaffActivityReport(String managerEmail);

    TopProductsReport getManagerTopProducts(String managerEmail);

    // ── STAFF: his warehouse + his own activity ───────────────────────────
    MyIssueHistoryReport getMyIssueHistory(String staffEmail);

    StockTrendReport getStaffWarehouseTrend(String staffEmail, LocalDate from, LocalDate to);

    // ── SUPPLIER: his own data ────────────────────────────────────────────
    SupplierPOReport getSupplierPOReport(String supplierEmail);
}