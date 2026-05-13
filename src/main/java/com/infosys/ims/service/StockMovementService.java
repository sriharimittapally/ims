package com.infosys.ims.service;

import com.infosys.ims.dtos.response.StockMovementResponse;
import com.infosys.ims.entity.Inventory;
import com.infosys.ims.entity.Users;
import com.infosys.ims.enums.StockMovementReferenceType;
import com.infosys.ims.enums.StockMovementType;

import java.util.List;

public interface StockMovementService {

    /** Internal use: called by PO and StockIssue services to log movements */
    void log(
            Inventory inventory,
            StockMovementType type,
            int quantity,
            Long referenceId,
            StockMovementReferenceType referenceType,
            String note,
            Users createdBy
    );

    // ── ADMIN: global queries ─────────────────────────────────────────────
    List<StockMovementResponse> getAllMovements();

    List<StockMovementResponse> getMovementsByProduct(Long productId);

    /** Admin uses this to query any warehouse by ID */
    List<StockMovementResponse> getMovementsByWarehouse(Long warehouseId);

    List<StockMovementResponse> getMovementsByProductAndWarehouse(Long productId, Long warehouseId);

    List<StockMovementResponse> getMovementsByReference(Long referenceId, StockMovementReferenceType referenceType);

    // ── MANAGER: his warehouse only — resolved from email, no warehouseId param ──
    /** All movements in the manager's own warehouse */
    List<StockMovementResponse> getMyWarehouseMovements(String managerEmail);

    /** Movements for a specific product in the manager's warehouse only */
    List<StockMovementResponse> getMyWarehouseMovementsByProduct(String managerEmail, Long productId);
}