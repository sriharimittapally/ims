package com.infosys.ims.service.serviceImpl;

import com.infosys.ims.dtos.request.WarehouseRequest;
import com.infosys.ims.dtos.response.WarehouseResponse;
import com.infosys.ims.entity.Warehouse;
import com.infosys.ims.enums.WarehouseStatus;
import com.infosys.ims.exception.BadRequestException;
import com.infosys.ims.exception.DuplicateResourceException;
import com.infosys.ims.exception.ResourceNotFoundException;
import com.infosys.ims.mapper.WarehouseMapper;
import com.infosys.ims.repository.WarehouseRepository;
import com.infosys.ims.service.WarehouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WarehouseServiceImpl implements WarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final WarehouseMapper warehouseMapper;

    @Override
    @Transactional
    public WarehouseResponse createWarehouse(WarehouseRequest request) {
        if (warehouseRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Warehouse with name already exists: " + request.getName());
        }
        Warehouse warehouse = new Warehouse();
        warehouse.setName(request.getName());
        warehouse.setAddress(request.getAddress());
        warehouse.setCity(request.getCity());
        return warehouseMapper.mapToResponse(warehouseRepository.save(warehouse));
    }

    @Override
    @Transactional
    public WarehouseResponse updateWarehouse(Long id, WarehouseRequest request) {
        Warehouse warehouse = getEntity(id);
        warehouse.setName(request.getName());
        warehouse.setAddress(request.getAddress());
        warehouse.setCity(request.getCity());
        return warehouseMapper.mapToResponse(warehouseRepository.save(warehouse));
    }

    @Override
    public WarehouseResponse getWarehouseById(Long id) {
        return warehouseMapper.mapToResponse(getEntity(id));
    }

    @Override
    public List getAllWarehouses() {
        return warehouseRepository.findAll().stream()
                .map(warehouseMapper::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List getActiveWarehouses() {
        return warehouseRepository.findByStatus(WarehouseStatus.ACTIVE).stream()
                .map(warehouse -> warehouseMapper.mapToResponse(warehouse))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void activateWarehouse(Long id) {
        Warehouse warehouse = getEntity(id);
        warehouse.setStatus(WarehouseStatus.ACTIVE);
        warehouseRepository.save(warehouse);
    }

    @Override
    @Transactional
    public void deactivateWarehouse(Long id) {
        Warehouse warehouse = getEntity(id);
        warehouse.setStatus(WarehouseStatus.INACTIVE);
        warehouseRepository.save(warehouse);
    }

    private Warehouse getEntity(Long id) {
        return warehouseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found: " + id));
    }
}