package com.infosys.ims.controller;

import com.infosys.ims.dtos.response.ApiResponse;
import com.infosys.ims.dtos.response.InventoryResponse;
import com.infosys.ims.dtos.response.PageResponse;
import com.infosys.ims.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    // =========================================================================
    // ADMIN â€” global inventory, all warehouses
    // =========================================================================

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<InventoryResponse>>> getAllInventory() {
        return ResponseEntity.ok(ApiResponse.success(
                "All inventory fetched", inventoryService.getAllInventory()));
    }

    /**
     * ADMIN sees global low-stock from all warehouses.
     * This endpoint is ADMIN-only. Manager uses /my-warehouse/low-stock.
     */
    @GetMapping("/low-stock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<InventoryResponse>>> getLowStockGlobal() {
        return ResponseEntity.ok(ApiResponse.success(
                "Global low stock items fetched", inventoryService.getLowStockItemsGlobal()));
    }

    // =========================================================================
    // MANAGER â€” his warehouse only
    // =========================================================================

    /**
     * Manager views inventory for HIS warehouse only.
     * The service resolves warehouse from the manager's email â€” no warehouse ID param
     * means the manager cannot query another warehouse.
     */
    @GetMapping("/my-warehouse")
    @PreAuthorize("hasAnyRole('MANAGER','STAFF')")
    public ResponseEntity<ApiResponse<List<InventoryResponse>>> getMyWarehouseInventory(
            Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(
                "Warehouse inventory fetched",
                inventoryService.getMyWarehouseInventory(auth.getName())));
    }

    @GetMapping("/my-warehouse/page")
    @PreAuthorize("hasAnyRole('MANAGER','STAFF')")
    public ResponseEntity<ApiResponse<PageResponse<InventoryResponse>>> getMyWarehouseInventoryPage(
            Authentication auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "false") boolean lowStockOnly) {
        return ResponseEntity.ok(ApiResponse.success(
                "Warehouse inventory page fetched",
                inventoryService.getMyWarehouseInventoryPaged(auth.getName(), page, size, search, lowStockOnly)));
    }

    /**
     * Single product in the caller's warehouse.
     * Both manager and staff can query this â€” both are bound to their own warehouse.
     */
    @GetMapping("/my-warehouse/product/{productId}")
    @PreAuthorize("hasAnyRole('MANAGER','STAFF')")
    public ResponseEntity<ApiResponse<InventoryResponse>> getProductInMyWarehouse(
            @PathVariable Long productId, Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(
                "Product inventory fetched",
                inventoryService.getMyWarehouseInventoryForProduct(auth.getName(), productId)));
    }

    /**
     * Low stock items for the manager's warehouse ONLY.
     * Manager NEVER sees low-stock from other warehouses through this endpoint.
     * STAFF cannot call this â€” they do not need low-stock visibility.
     */
    @GetMapping("/my-warehouse/low-stock")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<List<InventoryResponse>>> getLowStockForMyWarehouse(
            Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(
                "Low stock items for your warehouse",
                inventoryService.getLowStockItemsForMyWarehouse(auth.getName())));
    }
}
