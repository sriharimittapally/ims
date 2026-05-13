package com.infosys.ims.controller;

import com.infosys.ims.dtos.response.ApiResponse;
import com.infosys.ims.dtos.response.StockMovementResponse;
import com.infosys.ims.enums.StockMovementReferenceType;
import com.infosys.ims.service.StockMovementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stock-movements")
@RequiredArgsConstructor
public class StockMovementController {

    private final StockMovementService stockMovementService;

    // =========================================================================
    // ADMIN — global audit trail, any warehouse, any product
    // =========================================================================

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<StockMovementResponse>>> getAllMovements() {
        return ResponseEntity.ok(ApiResponse.success(
                "All stock movements fetched", stockMovementService.getAllMovements()));
    }

    /** Admin queries movements for any product globally */
    @GetMapping("/product/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<StockMovementResponse>>> byProductGlobal(
            @PathVariable Long productId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Movements fetched", stockMovementService.getMovementsByProduct(productId)));
    }

    /** Admin queries movements for any warehouse by ID */
    @GetMapping("/warehouse/{warehouseId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<StockMovementResponse>>> byWarehouseGlobal(
            @PathVariable Long warehouseId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Movements fetched", stockMovementService.getMovementsByWarehouse(warehouseId)));
    }

    /** Admin: product+warehouse combo for any warehouse */
    @GetMapping("/warehouse/{warehouseId}/product/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<StockMovementResponse>>> byProductAndWarehouseGlobal(
            @PathVariable Long warehouseId, @PathVariable Long productId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Movements fetched",
                stockMovementService.getMovementsByProductAndWarehouse(productId, warehouseId)));
    }

    /** Admin: audit trail for a specific PO or StockIssue */
    @GetMapping("/reference/{referenceType}/{referenceId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<StockMovementResponse>>> byReference(
            @PathVariable StockMovementReferenceType referenceType,
            @PathVariable Long referenceId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Reference movements fetched",
                stockMovementService.getMovementsByReference(referenceId, referenceType)));
    }

    // =========================================================================
    // MANAGER — his warehouse ONLY — cannot query other warehouses
    // =========================================================================

    /**
     * All movements in the manager's own warehouse.
     * Warehouse is resolved from the manager's email — no warehouseId param,
     * so the manager cannot access another warehouse's movements.
     */
    @GetMapping("/my-warehouse")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<List<StockMovementResponse>>> myWarehouseMovements(
            Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(
                "Your warehouse movements",
                stockMovementService.getMyWarehouseMovements(auth.getName())));
    }

    /**
     * Movements for a specific product inside the manager's warehouse only.
     * Manager cannot use this to query a product in another warehouse.
     */
    @GetMapping("/my-warehouse/product/{productId}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<List<StockMovementResponse>>> myWarehouseProductMovements(
            @PathVariable Long productId, Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(
                "Product movements in your warehouse",
                stockMovementService.getMyWarehouseMovementsByProduct(auth.getName(), productId)));
    }

    // NOTE: STAFF has no movement endpoints here.
    // Staff can view their warehouse's trend through /api/reports/staff/warehouse-trend.
    // Raw movement log access is manager+ only.
}