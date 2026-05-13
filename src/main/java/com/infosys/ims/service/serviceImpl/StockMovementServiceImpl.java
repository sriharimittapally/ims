package com.infosys.ims.service.serviceImpl;

import com.infosys.ims.dtos.response.StockMovementResponse;
import com.infosys.ims.entity.*;
import com.infosys.ims.enums.StockMovementReferenceType;
import com.infosys.ims.enums.StockMovementType;
import com.infosys.ims.exception.BadRequestException;
import com.infosys.ims.exception.ResourceNotFoundException;
import com.infosys.ims.mapper.StockMovementMapper;
import com.infosys.ims.repository.*;
import com.infosys.ims.service.StockMovementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StockMovementServiceImpl implements StockMovementService {

    private final StockMovementRepository stockMovementRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final UserRepository userRepository;
    private final StockMovementMapper stockMovementMapper;

    // ── Internal: called by PO and StockIssue services ────────────────────

    @Override
    @Transactional
    public void log(
            Inventory inventory,
            StockMovementType type,
            int quantity,
            Long referenceId,
            StockMovementReferenceType referenceType,
            String note,
            Users createdBy) {

        StockMovement movement = new StockMovement();
        movement.setProduct(inventory.getProduct());
        movement.setWarehouse(inventory.getWarehouse());
        movement.setType(type);
        movement.setQuantity(quantity);
        // quantityAfter is the current quantity AFTER the change was already applied
        movement.setQuantityAfter(inventory.getQuantity());
        movement.setReferenceId(referenceId);
        movement.setReferenceType(referenceType);
        movement.setNote(note);
        movement.setCreatedBy(createdBy);
        stockMovementRepository.save(movement);
    }

    // ── ADMIN: global ─────────────────────────────────────────────────────

    @Override
    public List<StockMovementResponse> getAllMovements() {
        return stockMovementRepository.findAll().stream()
                .map(stockMovementMapper::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<StockMovementResponse> getMovementsByProduct(Long productId) {
        Product product = getProduct(productId);
        return stockMovementRepository.findByProduct(product).stream()
                .map(stockMovementMapper::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<StockMovementResponse> getMovementsByWarehouse(Long warehouseId) {
        Warehouse warehouse = getWarehouse(warehouseId);
        return stockMovementRepository.findByWarehouse(warehouse).stream()
                .map(stockMovementMapper::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<StockMovementResponse> getMovementsByProductAndWarehouse(Long productId, Long warehouseId) {
        Product product = getProduct(productId);
        Warehouse warehouse = getWarehouse(warehouseId);
        return stockMovementRepository.findByProductAndWarehouse(product, warehouse).stream()
                .map(stockMovementMapper::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<StockMovementResponse> getMovementsByReference(
            Long referenceId, StockMovementReferenceType referenceType) {
        return stockMovementRepository
                .findByReferenceIdAndReferenceType(referenceId, referenceType).stream()
                .map(stockMovementMapper::mapToResponse)
                .collect(Collectors.toList());
    }

    // ── MANAGER: his warehouse only ───────────────────────────────────────
    // Warehouse is resolved from the manager's email — manager can never
    // pass a different warehouseId to snoop on another warehouse.

    @Override
    public List<StockMovementResponse> getMyWarehouseMovements(String managerEmail) {
        Warehouse warehouse = getWarehouseForUser(managerEmail);
        return stockMovementRepository.findByWarehouseId(warehouse.getId()).stream()
                .map(stockMovementMapper::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<StockMovementResponse> getMyWarehouseMovementsByProduct(
            String managerEmail, Long productId) {
        Warehouse warehouse = getWarehouseForUser(managerEmail);
        // Validates product exists first
        getProduct(productId);
        return stockMovementRepository
                .findByWarehouseIdAndProductId(warehouse.getId(), productId).stream()
                .map(stockMovementMapper::mapToResponse)
                .collect(Collectors.toList());
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private Warehouse getWarehouseForUser(String email) {
        Users user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
        if (user.getWarehouse() == null) {
            throw new BadRequestException("You are not assigned to any warehouse");
        }
        return user.getWarehouse();
    }

    private Product getProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
    }

    private Warehouse getWarehouse(Long id) {
        return warehouseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found: " + id));
    }
}