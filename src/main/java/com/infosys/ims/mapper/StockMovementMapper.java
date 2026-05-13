package com.infosys.ims.mapper;

import com.infosys.ims.dtos.response.StockMovementResponse;
import com.infosys.ims.entity.StockMovement;
import org.springframework.stereotype.Component;

@Component
public class StockMovementMapper {

    public StockMovementResponse mapToResponse(StockMovement movement) {
        StockMovementResponse response = new StockMovementResponse();
        response.setId(movement.getId());
        response.setProductId(movement.getProduct().getId());
        response.setProductName(movement.getProduct().getProductName());
        response.setSku(movement.getProduct().getSku());
        response.setWarehouseId(movement.getWarehouse().getId());
        response.setWarehouseName(movement.getWarehouse().getName());
        // FIX: enum → String
        response.setType(movement.getType().name());
        response.setQuantity(movement.getQuantity());
        response.setQuantityAfter(movement.getQuantityAfter());
        response.setReferenceId(movement.getReferenceId());
        if (movement.getReferenceType() != null) {
            response.setReferenceType(movement.getReferenceType().name());
        }
        response.setNote(movement.getNote());
        if (movement.getCreatedBy() != null) {
            response.setCreatedByName(movement.getCreatedBy().getName());
        }
        response.setCreatedAt(movement.getCreatedAt());
        return response;
    }
}