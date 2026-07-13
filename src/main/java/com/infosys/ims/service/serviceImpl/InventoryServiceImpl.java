package com.infosys.ims.service.serviceImpl;

import com.infosys.ims.dtos.response.InventoryResponse;
import com.infosys.ims.dtos.response.PageResponse;
import com.infosys.ims.entity.Inventory;
import com.infosys.ims.entity.Product;
import com.infosys.ims.entity.Users;
import com.infosys.ims.entity.Warehouse;
import com.infosys.ims.exception.BadRequestException;
import com.infosys.ims.exception.ResourceNotFoundException;
import com.infosys.ims.mapper.InventoryMapper;
import com.infosys.ims.repository.InventoryRepository;
import com.infosys.ims.repository.UserRepository;
import com.infosys.ims.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final UserRepository userRepository;
    private final InventoryMapper inventoryMapper;

    // Ã¢â€â‚¬Ã¢â€â‚¬ ADMIN: global Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬

    @Override
    public List<InventoryResponse> getAllInventory() {
        return inventoryRepository.findAll().stream()
                .map(inventoryMapper::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<InventoryResponse> getLowStockItemsGlobal() {
        // Uses fixed JPQL: (quantity - reservedQuantity) <= reorderLevel
        return inventoryRepository.findAllLowStockItems().stream()
                .map(inventoryMapper::mapToResponse)
                .collect(Collectors.toList());
    }

    // Ã¢â€â‚¬Ã¢â€â‚¬ MANAGER / STAFF: their warehouse only Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬

    @Override
    public List<InventoryResponse> getMyWarehouseInventory(String userEmail) {
        Warehouse warehouse = getWarehouseForUser(userEmail);
        return inventoryRepository.findByWarehouse(warehouse).stream()
                .map(inventoryMapper::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public PageResponse<InventoryResponse> getMyWarehouseInventoryPaged(
            String userEmail,
            int page,
            int size,
            String search,
            boolean lowStockOnly) {
        Warehouse warehouse = getWarehouseForUser(userEmail);
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 5), 50);
        Pageable pageable = PageRequest.of(safePage, safeSize);
        Page<InventoryResponse> result = inventoryRepository
                .searchByWarehouse(warehouse, normalizeSearch(search), lowStockOnly, pageable)
                .map(inventoryMapper::mapToResponse);

        return PageResponse.from(result);
    }

    @Override
    public InventoryResponse getMyWarehouseInventoryForProduct(String userEmail, Long productId) {
        Warehouse warehouse = getWarehouseForUser(userEmail);
        return inventoryRepository.findByWarehouse(warehouse).stream()
                .filter(inv -> inv.getProduct().getId().equals(productId))
                .map(inventoryMapper::mapToResponse)
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No inventory found for product " + productId + " in your warehouse"));
    }

    @Override
    public List<InventoryResponse> getLowStockItemsForMyWarehouse(String managerEmail) {
        Warehouse warehouse = getWarehouseForUser(managerEmail);
        // STRICTLY scoped: only items in THIS warehouse below reorder level
        return inventoryRepository.findLowStockByWarehouse(warehouse.getId()).stream()
                .map(inventoryMapper::mapToResponse)
                .collect(Collectors.toList());
    }

    // Ã¢â€â‚¬Ã¢â€â‚¬ Internal Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬

    @Override
    @Transactional
    public Inventory getOrCreateInventory(Product product, Warehouse warehouse) {
        return inventoryRepository.findByProductAndWarehouse(product, warehouse)
                .orElseGet(() -> {
                    Inventory inv = new Inventory();
                    inv.setProduct(product);
                    inv.setWarehouse(warehouse);
                    inv.setQuantity(0);
                    inv.setReservedQuantity(0);
                    return inventoryRepository.save(inv);
                });
    }

    @Override
    @Transactional
    public void addStock(Inventory inventory, int quantity) {
        inventory.setQuantity(inventory.getQuantity() + quantity);
        inventoryRepository.save(inventory);
    }

    @Override
    @Transactional
    public void reserveStock(Inventory inventory, int quantity) {
        validateStockAvailability(inventory, quantity);
        inventory.setReservedQuantity(inventory.getReservedQuantity() + quantity);
        inventoryRepository.save(inventory);
    }

    @Override
    @Transactional
    public void releaseReservation(Inventory inventory, int quantity) {
        int newReserved = Math.max(0, inventory.getReservedQuantity() - quantity);
        inventory.setReservedQuantity(newReserved);
        inventoryRepository.save(inventory);
    }

    @Override
    @Transactional
    public void deductStock(Inventory inventory, int quantity) {
        validateStockAvailability(inventory, quantity);
        inventory.setQuantity(inventory.getQuantity() - quantity);
        inventoryRepository.save(inventory);
    }

    @Override
    public void validateStockAvailability(Inventory inventory, int quantity) {
        if (inventory.getAvailableQuantity() < quantity) {
            throw new BadRequestException(
                    "Insufficient stock for '" + inventory.getProduct().getProductName() +
                            "'. Available: " + inventory.getAvailableQuantity() +
                            ", Requested: " + quantity
            );
        }
    }

    private String normalizeSearch(String search) {
        return search == null ? "" : search.trim();
    }

    // Ã¢â€â‚¬Ã¢â€â‚¬ Helper Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬

    private Warehouse getWarehouseForUser(String email) {
        Users user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
        if (user.getWarehouse() == null) {
            throw new BadRequestException("You are not assigned to any warehouse");
        }
        return user.getWarehouse();
    }
}
