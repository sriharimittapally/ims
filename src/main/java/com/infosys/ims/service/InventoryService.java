package com.infosys.ims.service;

import com.infosys.ims.dtos.response.InventoryResponse;
import com.infosys.ims.dtos.response.PageResponse;
import com.infosys.ims.entity.Inventory;
import com.infosys.ims.entity.Product;
import com.infosys.ims.entity.Warehouse;

import java.util.List;

public interface InventoryService {

    // â”€â”€ READ: ADMIN (global) â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    List<InventoryResponse> getAllInventory();

    /** All low-stock across ALL warehouses â€” ADMIN only */
    List<InventoryResponse> getLowStockItemsGlobal();

    // â”€â”€ READ: MANAGER/STAFF (their warehouse only) â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    /** Inventory for the warehouse the caller belongs to */
    List<InventoryResponse> getMyWarehouseInventory(String userEmail);

    PageResponse<InventoryResponse> getMyWarehouseInventoryPaged(
            String userEmail,
            int page,
            int size,
            String search,
            boolean lowStockOnly);

    /** One product's inventory in the caller's warehouse */
    InventoryResponse getMyWarehouseInventoryForProduct(String userEmail, Long productId);

    /** Low-stock items in the MANAGER's warehouse only â€” manager never sees others */
    List<InventoryResponse> getLowStockItemsForMyWarehouse(String managerEmail);

    // â”€â”€ Internal use by PO / StockIssue services â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    Inventory getOrCreateInventory(Product product, Warehouse warehouse);

    void addStock(Inventory inventory, int quantity);

    void reserveStock(Inventory inventory, int quantity);

    void releaseReservation(Inventory inventory, int quantity);

    void deductStock(Inventory inventory, int quantity);

    void validateStockAvailability(Inventory inventory, int quantity);
}
