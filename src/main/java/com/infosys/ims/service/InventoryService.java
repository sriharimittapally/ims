package com.infosys.ims.service;

import com.infosys.ims.dtos.response.InventoryResponse;
import com.infosys.ims.entity.Inventory;
import com.infosys.ims.entity.Product;
import com.infosys.ims.entity.Warehouse;

import java.util.List;

public interface InventoryService {

    // ── READ: ADMIN (global) ──────────────────────────────────────────────
    List<InventoryResponse> getAllInventory();

    /** All low-stock across ALL warehouses — ADMIN only */
    List<InventoryResponse> getLowStockItemsGlobal();

    // ── READ: MANAGER/STAFF (their warehouse only) ────────────────────────
    /** Inventory for the warehouse the caller belongs to */
    List<InventoryResponse> getMyWarehouseInventory(String userEmail);

    /** One product's inventory in the caller's warehouse */
    InventoryResponse getMyWarehouseInventoryForProduct(String userEmail, Long productId);

    /** Low-stock items in the MANAGER's warehouse only — manager never sees others */
    List<InventoryResponse> getLowStockItemsForMyWarehouse(String managerEmail);

    // ── Internal use by PO / StockIssue services ─────────────────────────
    Inventory getOrCreateInventory(Product product, Warehouse warehouse);

    void addStock(Inventory inventory, int quantity);

    void reserveStock(Inventory inventory, int quantity);

    void releaseReservation(Inventory inventory, int quantity);

    void deductStock(Inventory inventory, int quantity);

    void validateStockAvailability(Inventory inventory, int quantity);
}