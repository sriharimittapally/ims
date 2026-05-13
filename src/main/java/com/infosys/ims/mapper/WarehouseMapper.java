package com.infosys.ims.mapper;

import com.infosys.ims.dtos.response.WarehouseResponse;
import com.infosys.ims.entity.Warehouse;
import org.springframework.stereotype.Component;

@Component
public class WarehouseMapper {

    public WarehouseResponse mapToResponse(Warehouse warehouse) {
        WarehouseResponse response = new WarehouseResponse();
        response.setId(warehouse.getId());
        response.setName(warehouse.getName());
        response.setAddress(warehouse.getAddress());
        response.setCity(warehouse.getCity());
        response.setStatus(warehouse.getStatus().name());
        response.setCreatedAt(warehouse.getCreatedAt());
        response.setUpdatedAt(warehouse.getUpdatedAt());
        if (warehouse.getManager() != null) {
            response.setManagerId(warehouse.getManager().getId());
            response.setManagerName(warehouse.getManager().getName());
        }
        return response;
    }
}