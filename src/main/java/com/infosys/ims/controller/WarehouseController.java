package com.infosys.ims.controller;

import com.infosys.ims.dtos.request.WarehouseRequest;
import com.infosys.ims.dtos.response.ApiResponse;
import com.infosys.ims.dtos.response.WarehouseResponse;
import com.infosys.ims.service.WarehouseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/warehouses")
@RequiredArgsConstructor
public class WarehouseController {

    private final WarehouseService warehouseService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<WarehouseResponse>> create(@Valid @RequestBody WarehouseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Warehouse created", warehouseService.createWarehouse(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<WarehouseResponse>> update(@PathVariable Long id,
                                                                 @Valid @RequestBody WarehouseRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Warehouse updated", warehouseService.updateWarehouse(id, request)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','STAFF')")
    public ResponseEntity<ApiResponse<WarehouseResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Warehouse fetched", warehouseService.getWarehouseById(id)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','STAFF')")
    public ResponseEntity<ApiResponse<List<WarehouseResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success("Warehouses fetched", warehouseService.getAllWarehouses()));
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','STAFF')")
    public ResponseEntity<ApiResponse<List<WarehouseResponse>>> getActive() {
        return ResponseEntity.ok(ApiResponse.success("Active warehouses fetched", warehouseService.getActiveWarehouses()));
    }

    @PutMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> activate(@PathVariable Long id) {
        warehouseService.activateWarehouse(id);
        return ResponseEntity.ok(ApiResponse.success("Warehouse activated"));
    }

    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deactivate(@PathVariable Long id) {
        warehouseService.deactivateWarehouse(id);
        return ResponseEntity.ok(ApiResponse.success("Warehouse deactivated"));
    }
}