package com.infosys.ims.controller;

import com.infosys.ims.dtos.response.ApiResponse;
import com.infosys.ims.dtos.response.report.*;
import com.infosys.ims.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    // =========================================================================
    // ADMIN REPORTS — global, all warehouses, all data
    // =========================================================================

    /**
     * Full inventory summary across all warehouses.
     * Only ADMIN can see this — managers/staff never see other warehouses.
     */
    @GetMapping("/admin/inventory-summary")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<InventorySummaryReport>> adminInventorySummary() {
        return ResponseEntity.ok(ApiResponse.success(
                "Inventory summary report generated",
                reportService.getAdminInventorySummary()
        ));
    }

    /**
     * Global low-stock alerts across ALL warehouses.
     * ADMIN only — manager uses /manager/low-stock-alerts for his warehouse.
     */
    @GetMapping("/admin/low-stock-alerts")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<LowStockAlertReport>> adminLowStockAlerts() {
        return ResponseEntity.ok(ApiResponse.success(
                "Low stock alert report generated",
                reportService.getAdminLowStockAlerts()
        ));
    }

    /**
     * PO analytics report — all warehouses, all suppliers.
     */
    @GetMapping("/admin/purchase-orders")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PurchaseOrderReport>> adminPOReport() {
        return ResponseEntity.ok(ApiResponse.success(
                "Purchase order report generated",
                reportService.getAdminPurchaseOrderReport()
        ));
    }

    /**
     * Supplier performance — fulfillment rates, rejection rates, total spend.
     */
    @GetMapping("/admin/supplier-performance")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SupplierPerformanceReport>> adminSupplierPerformance() {
        return ResponseEntity.ok(ApiResponse.success(
                "Supplier performance report generated",
                reportService.getAdminSupplierPerformanceReport()
        ));
    }

    /**
     * Stock IN/OUT trend — daily breakdown over a date range across all warehouses.
     * Default: last 30 days.
     */
    @GetMapping("/admin/stock-trend")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<StockTrendReport>> adminStockTrend(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        if (from == null) from = LocalDate.now().minusDays(30);
        if (to == null) to = LocalDate.now();

        return ResponseEntity.ok(ApiResponse.success(
                "Stock trend report generated",
                reportService.getAdminStockTrend(from, to)
        ));
    }

    /**
     * Top moving and slow-moving products globally.
     */
    @GetMapping("/admin/top-products")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TopProductsReport>> adminTopProducts() {
        return ResponseEntity.ok(ApiResponse.success(
                "Top products report generated",
                reportService.getAdminTopProducts()
        ));
    }

    /**
     * Detailed warehouse stock snapshot — admin can query ANY warehouse.
     */
    @GetMapping("/admin/warehouse/{warehouseId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<WarehouseStockReport>> adminWarehouseReport(
            @PathVariable Long warehouseId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Warehouse stock report generated",
                reportService.getAdminWarehouseReport(warehouseId)
        ));
    }

    // =========================================================================
    // MANAGER REPORTS — his warehouse ONLY, never any other warehouse
    // =========================================================================

    /**
     * Full stock report for the manager's own warehouse.
     * The service layer enforces warehouse ownership — manager cannot pass
     * another warehouseId; their email is the boundary.
     */
    @GetMapping("/manager/warehouse-stock")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<WarehouseStockReport>> managerWarehouseStock(
            Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(
                "Warehouse stock report generated",
                reportService.getManagerWarehouseReport(auth.getName())
        ));
    }

    /**
     * Low stock alerts for the manager's warehouse only.
     * Manager NEVER sees low-stock from other warehouses.
     */
    @GetMapping("/manager/low-stock-alerts")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<LowStockAlertReport>> managerLowStockAlerts(
            Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(
                "Low stock alerts for your warehouse",
                reportService.getManagerLowStockAlerts(auth.getName())
        ));
    }

    /**
     * Stock IN/OUT daily trend for the manager's warehouse only.
     */
    @GetMapping("/manager/stock-trend")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<StockTrendReport>> managerStockTrend(
            Authentication auth,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        if (from == null) from = LocalDate.now().minusDays(30);
        if (to == null) to = LocalDate.now();

        return ResponseEntity.ok(ApiResponse.success(
                "Stock trend report generated",
                reportService.getManagerStockTrend(auth.getName(), from, to)
        ));
    }

    /**
     * PO history for the manager's warehouse only.
     */
    @GetMapping("/manager/purchase-orders")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<PurchaseOrderReport>> managerPOReport(
            Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(
                "Purchase order report for your warehouse",
                reportService.getManagerPOReport(auth.getName())
        ));
    }

    /**
     * Staff activity breakdown — how many issues each staff member created,
     * approved, issued, rejected. Manager's warehouse only.
     */
    @GetMapping("/manager/staff-activity")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<StaffActivityReport>> managerStaffActivity(
            Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(
                "Staff activity report generated",
                reportService.getManagerStaffActivityReport(auth.getName())
        ));
    }

    /**
     * Top/slow moving products in the manager's warehouse.
     */
    @GetMapping("/manager/top-products")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<TopProductsReport>> managerTopProducts(
            Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(
                "Top products report for your warehouse",
                reportService.getManagerTopProducts(auth.getName())
        ));
    }

    // =========================================================================
    // STAFF REPORTS — their own issues + their warehouse movements only
    // =========================================================================

    /**
     * Staff's own stock issue history — full breakdown of everything they created.
     */
    @GetMapping("/staff/my-issue-history")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<ApiResponse<MyIssueHistoryReport>> staffIssueHistory(
            Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(
                "Your issue history report",
                reportService.getMyIssueHistory(auth.getName())
        ));
    }

    /**
     * Stock IN/OUT trend for the staff's warehouse.
     * Staff can see movements for their warehouse (read-only context awareness),
     * but cannot see any other warehouse.
     */
    @GetMapping("/staff/warehouse-trend")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<ApiResponse<StockTrendReport>> staffWarehouseTrend(
            Authentication auth,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        if (from == null) from = LocalDate.now().minusDays(30);
        if (to == null) to = LocalDate.now();

        return ResponseEntity.ok(ApiResponse.success(
                "Warehouse trend for your warehouse",
                reportService.getStaffWarehouseTrend(auth.getName(), from, to)
        ));
    }

    // =========================================================================
    // SUPPLIER REPORTS — their own POs and linked products
    // =========================================================================

    /**
     * Supplier's full PO history — only their own POs.
     */
    @GetMapping("/supplier/po-history")
    @PreAuthorize("hasRole('SUPPLIER')")
    public ResponseEntity<ApiResponse<SupplierPOReport>> supplierPOReport(
            Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(
                "Your PO history report",
                reportService.getSupplierPOReport(auth.getName())
        ));
    }
}