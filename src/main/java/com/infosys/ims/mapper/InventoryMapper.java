package com.infosys.ims.mapper;

import com.infosys.ims.dtos.response.InventoryResponse;
import com.infosys.ims.entity.Inventory;
import org.springframework.stereotype.Component;

@Component
public class InventoryMapper {

    public InventoryResponse mapToResponse(Inventory inventory) {
        InventoryResponse response = new InventoryResponse();
        response.setId(inventory.getId());
        response.setProductId(inventory.getProduct().getId());
        // FIX: getProductName()
        response.setProductName(inventory.getProduct().getProductName());
        response.setSku(inventory.getProduct().getSku());
        response.setCategoryName(inventory.getProduct().getCategory().getName());
        response.setWarehouseId(inventory.getWarehouse().getId());
        response.setWarehouseName(inventory.getWarehouse().getName());
        response.setWarehouseCity(inventory.getWarehouse().getCity());
        response.setQuantity(inventory.getQuantity());
        response.setReservedQuantity(inventory.getReservedQuantity());
        response.setAvailableQuantity(inventory.getAvailableQuantity());
        response.setReorderLevel(inventory.getProduct().getReorderLevel());
        response.setLowStock(inventory.getAvailableQuantity() <= inventory.getProduct().getReorderLevel());
        response.setUpdatedAt(inventory.getUpdatedAt());
        return response;
    }
}