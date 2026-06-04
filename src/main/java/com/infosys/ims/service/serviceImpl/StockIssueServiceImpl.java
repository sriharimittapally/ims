package com.infosys.ims.service.serviceImpl;

import com.infosys.ims.dtos.request.StockIssueRejectRequest;
import com.infosys.ims.dtos.response.StockIssueResponse;
import com.infosys.ims.entity.*;
import com.infosys.ims.enums.StockIssueStatus;
import com.infosys.ims.enums.StockMovementReferenceType;
import com.infosys.ims.enums.StockMovementType;
import com.infosys.ims.exception.BadRequestException;
import com.infosys.ims.exception.ForbiddenOperationException;
import com.infosys.ims.exception.ResourceNotFoundException;
import com.infosys.ims.mapper.StockIssueMapper;
import com.infosys.ims.repository.*;
import com.infosys.ims.service.InventoryService;
import com.infosys.ims.service.PurchaseOrderService;
import com.infosys.ims.service.StockIssueService;
import com.infosys.ims.service.StockMovementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StockIssueServiceImpl implements StockIssueService {

    private final StockIssueRepository stockIssueRepository;
    private final StockIssueItemRepository stockIssueItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final InventoryService inventoryService;
    private final StockMovementService stockMovementService;
    private final PurchaseOrderService purchaseOrderService;
    private final StockIssueMapper stockIssueMapper;

    // ─────────────────────────────────────────────────────────────────────────
    // STAFF — CREATE EMPTY ISSUE (header only)
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public StockIssueResponse createIssue(String staffEmail, String note) {
        Users staff = getUser(staffEmail);
        if (staff.getWarehouse() == null) {
            throw new BadRequestException("Staff is not assigned to any warehouse");
        }

        StockIssue issue = new StockIssue();
        issue.setIssueNumber(generateIssueNumber());
        issue.setWarehouse(staff.getWarehouse());
        issue.setIssuedBy(staff);
        issue.setNote(note);

        return stockIssueMapper.toResponse(stockIssueRepository.save(issue));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // STAFF — ADD ITEM TO PENDING ISSUE
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public StockIssueResponse addItem(Long issueId, Long productId, int quantity, String staffEmail) {
        StockIssue issue = getIssueEntity(issueId);
        Users staff = getUser(staffEmail);

        validateStaffOwnsIssue(staff, issue);

        if (issue.getStatus() != StockIssueStatus.DRAFT) {
            throw new BadRequestException("Cannot modify a " + issue.getStatus() + " stock issue");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));
        Inventory inventory = inventoryService.getOrCreateInventory(product, issue.getWarehouse());

        // Check if product already in this issue — update quantity if so
        issue.getItems().stream()
                .filter(i -> i.getProduct().getId().equals(productId))
                .findFirst()
                .ifPresentOrElse(
                        existing -> {
                            int updatedQuantity = existing.getQuantityRequested() + quantity;
                            validateRequestedQuantity(inventory, updatedQuantity);
                            existing.setQuantityRequested(updatedQuantity);
                            existing.setQuantityIssued(existing.getQuantityRequested());
                            stockIssueItemRepository.save(existing);
                        },
                        () -> {
                            // Validate stock is available in this warehouse
                            validateRequestedQuantity(inventory, quantity);

                            StockIssueItem item = new StockIssueItem();
                            item.setStockIssue(issue);
                            item.setProduct(product);
                            item.setQuantityRequested(quantity);
                            item.setQuantityIssued(quantity);
                            StockIssueItem savedItem = stockIssueItemRepository.save(item);
                            issue.getItems().add(savedItem);
                        }
                );

        return stockIssueMapper.toResponse(issue);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // STAFF — REMOVE ITEM FROM PENDING ISSUE
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public StockIssueResponse removeItem(Long issueId, Long itemId, String staffEmail) {
        StockIssue issue = getIssueEntity(issueId);
        Users staff = getUser(staffEmail);

        validateStaffOwnsIssue(staff, issue);

        if (issue.getStatus() != StockIssueStatus.DRAFT) {
            throw new BadRequestException("Cannot modify a " + issue.getStatus() + " stock issue");
        }

        StockIssueItem item = stockIssueItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Stock issue item not found: " + itemId));

        if (!item.getStockIssue().getId().equals(issueId)) {
            throw new BadRequestException("This item does not belong to the specified stock issue");
        }

        issue.getItems().removeIf(existing -> existing.getId().equals(itemId));
        stockIssueItemRepository.delete(item);
        return stockIssueMapper.toResponse(issue);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // STAFF — CANCEL ISSUE
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public StockIssueResponse cancelIssue(Long issueId, String staffEmail) {
        StockIssue issue = getIssueEntity(issueId);
        Users staff = getUser(staffEmail);

        validateStaffOwnsIssue(staff, issue);

        if (issue.getStatus() == StockIssueStatus.ISSUED || issue.getStatus() == StockIssueStatus.CANCELLED) {
            throw new BadRequestException("Cannot cancel a " + issue.getStatus() + " stock issue.");
        }

        issue.setStatus(StockIssueStatus.CANCELLED);
        return stockIssueMapper.toResponse(stockIssueRepository.save(issue));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // MANAGER — APPROVE ISSUE
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public StockIssueResponse approveIssue(Long issueId, String managerEmail) {
        StockIssue issue = getIssueEntity(issueId);
        Users manager = getUser(managerEmail);

        validateManagerOwnsWarehouse(manager, issue.getWarehouse());

        if (issue.getStatus() != StockIssueStatus.PENDING) {
            throw new BadRequestException("Only PENDING stock issues can be approved. Current: " + issue.getStatus());
        }

        if (issue.getItems() == null || issue.getItems().isEmpty()) {
            throw new BadRequestException("Cannot approve a stock issue with no items");
        }

        issue.setStatus(StockIssueStatus.APPROVED);
        issue.setApprovedBy(manager);
        issue.setApprovedAt(LocalDateTime.now());
        return stockIssueMapper.toResponse(stockIssueRepository.save(issue));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // MANAGER — REJECT ISSUE
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public StockIssueResponse rejectIssue(Long issueId, String managerEmail, StockIssueRejectRequest request) {
        StockIssue issue = getIssueEntity(issueId);
        Users manager = getUser(managerEmail);

        validateManagerOwnsWarehouse(manager, issue.getWarehouse());

        if (issue.getStatus() != StockIssueStatus.PENDING) {
            throw new BadRequestException("Only PENDING stock issues can be rejected. Current: " + issue.getStatus());
        }

        issue.setStatus(StockIssueStatus.REJECTED);
        issue.setRejectionReason(request.getReason());
        issue.setApprovedBy(manager);
        issue.setApprovedAt(LocalDateTime.now());
        return stockIssueMapper.toResponse(stockIssueRepository.save(issue));
    }

    @Override
    @Transactional
    public StockIssueResponse submitForReview(Long issueId, String staffEmail) {
        StockIssue issue = getIssueEntity(issueId);
        Users staff = getUser(staffEmail);

        validateStaffOwnsIssue(staff, issue);

        if (issue.getStatus() != StockIssueStatus.DRAFT) {
            throw new BadRequestException("Only DRAFT stock issues can be submitted. Current: " + issue.getStatus());
        }

        if (issue.getItems() == null || issue.getItems().isEmpty()) {
            throw new BadRequestException("Cannot submit a stock issue with no items. Please add at least one product.");
        }

        for (StockIssueItem item : issue.getItems()) {
            Inventory inventory = inventoryService.getOrCreateInventory(item.getProduct(), issue.getWarehouse());
            validateRequestedQuantity(inventory, item.getQuantityRequested());
        }

        issue.setStatus(StockIssueStatus.PENDING);
        return stockIssueMapper.toResponse(stockIssueRepository.save(issue));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // STAFF — EXECUTE ISSUE (deducts stock + logs movement)
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public StockIssueResponse issueStock(Long issueId, String staffEmail) {
        StockIssue issue = getIssueEntity(issueId);
        Users staff = getUser(staffEmail);

        if (staff.getWarehouse() == null || !staff.getWarehouse().getId().equals(issue.getWarehouse().getId())) {
            throw new ForbiddenOperationException("You can only execute issues from your own warehouse");
        }
        if (issue.getStatus() != StockIssueStatus.APPROVED) {
            throw new BadRequestException("Only APPROVED stock issues can be executed. Current: " + issue.getStatus());
        }

        // Deduct stock for every item
        for (StockIssueItem item : issue.getItems()) {
            Inventory inventory = inventoryService.getOrCreateInventory(item.getProduct(), issue.getWarehouse());
            inventoryService.validateStockAvailability(inventory, item.getQuantityIssued());
            inventoryService.deductStock(inventory, item.getQuantityIssued());

            stockMovementService.log(
                    inventory,
                    StockMovementType.OUT,
                    item.getQuantityIssued(),
                    issue.getId(),
                    StockMovementReferenceType.STOCK_ISSUE,
                    "Stock Issue: " + issue.getIssueNumber(),
                    staff
            );

            // After deduction — check low stock and auto-draft PO if needed
            Inventory fresh = inventoryService.getOrCreateInventory(item.getProduct(), issue.getWarehouse());
            if (fresh.getAvailableQuantity() <= item.getProduct().getReorderLevel()) {
                purchaseOrderService.autoDraftPOIfLowStock(item.getProduct(), issue.getWarehouse());
            }
        }

        issue.setStatus(StockIssueStatus.ISSUED);
        issue.setIssuedAt(LocalDateTime.now());
        return stockIssueMapper.toResponse(stockIssueRepository.save(issue));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // READ
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    public List<StockIssueResponse> getIssuesCreatedBy(String staffEmail) {
        Users staff = getUser(staffEmail);
        return stockIssueRepository.findByIssuedByOrderByCreatedAtDesc(staff).stream()
                .map(stockIssueMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<StockIssueResponse> getPendingIssuesForWarehouse(String managerEmail) {
        Users manager = getUser(managerEmail);
        if (manager.getWarehouse() == null) throw new BadRequestException("Manager is not assigned to a warehouse");
        return stockIssueRepository.findByWarehouseAndStatusOrderByCreatedAtDesc(manager.getWarehouse(), StockIssueStatus.PENDING).stream()
                .map(stockIssueMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<StockIssueResponse> getAllIssuesForWarehouse(String managerEmail) {
        Users manager = getUser(managerEmail);
        if (manager.getWarehouse() == null) throw new BadRequestException("Manager is not assigned to a warehouse");
        return stockIssueRepository.findByWarehouseOrderByCreatedAtDesc(manager.getWarehouse()).stream()
                .map(stockIssueMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public StockIssueResponse getIssueById(Long issueId) {
        return stockIssueMapper.toResponse(getIssueEntity(issueId));
    }

    @Override
    public StockIssue getIssueEntity(Long issueId) {
        return stockIssueRepository.findById(issueId)
                .orElseThrow(() -> new ResourceNotFoundException("Stock issue not found: " + issueId));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PRIVATE HELPERS
    // ─────────────────────────────────────────────────────────────────────────
    private Users getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    private void validateStaffOwnsIssue(Users staff, StockIssue issue) {
        if (!issue.getIssuedBy().getId().equals(staff.getId())) {
            throw new ForbiddenOperationException("You can only modify your own stock issues");
        }
    }

    private void validateManagerOwnsWarehouse(Users manager, Warehouse warehouse) {
        if (manager.getWarehouse() == null || !manager.getWarehouse().getId().equals(warehouse.getId())) {
            throw new ForbiddenOperationException("This stock issue does not belong to your warehouse");
        }
    }

    private void validateRequestedQuantity(Inventory inventory, int requestedQuantity) {
        int availableQuantity = inventory.getAvailableQuantity();
        if (requestedQuantity > availableQuantity) {
            throw new BadRequestException(
                    "Quantity can't be greater than available stock. Available: " + availableQuantity +
                            ", Requested: " + requestedQuantity
            );
        }
    }

    private String generateIssueNumber() {
        long count = stockIssueRepository.count() + 1;
        return "SI-" + String.format("%06d", count);
    }
}