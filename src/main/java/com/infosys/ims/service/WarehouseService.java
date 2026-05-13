package com.infosys.ims.service;

import com.infosys.ims.dtos.request.WarehouseRequest;
import com.infosys.ims.dtos.response.WarehouseResponse;

import java.util.List;

public interface WarehouseService {

    WarehouseResponse createWarehouse(WarehouseRequest request);

    WarehouseResponse updateWarehouse(Long id, WarehouseRequest request);

    WarehouseResponse getWarehouseById(Long id);

    List<WarehouseResponse> getAllWarehouses();

    List<WarehouseResponse> getActiveWarehouses();

    void activateWarehouse(Long id);

    void deactivateWarehouse(Long id);
}