package com.infosys.ims.service.serviceImpl;

import com.infosys.ims.dtos.request.PurchaseOrderItemRequest;
import com.infosys.ims.dtos.request.PurchaseOrderRejectionRequest;
import com.infosys.ims.dtos.request.PurchaseOrderRequest;
import com.infosys.ims.dtos.response.PurchaseOrderResponse;
import com.infosys.ims.entity.*;
import com.infosys.ims.enums.ApprovalStatus;
import com.infosys.ims.enums.PurchaseOrderStatus;
import com.infosys.ims.enums.StockMovementReferenceType;
import com.infosys.ims.enums.StockMovementType;
import com.infosys.ims.exception.BadRequestException;
import com.infosys.ims.exception.ForbiddenOperationException;
import com.infosys.ims.exception.ResourceNotFoundException;
import com.infosys.ims.mapper.PurchaseOrderMapper;
import com.infosys.ims.repository.*;
import com.infosys.ims.service.InventoryService;
import com.infosys.ims.service.PurchaseOrderService;
import com.infosys.ims.service.StockMovementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PurchaseOrderServiceImpl implements PurchaseOrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final ProductRepository productRepository;
    private final ProductSupplierRepository productSupplierRepository;
    private final SupplierRepository supplierRepository;
    private final WarehouseRepository warehouseRepository;
    private final UserRepository userRepository;
    private final InventoryService inventoryService;
    private final StockMovementService stockMovementService;
    private final PurchaseOrderMapper purchaseOrderMapper;

    // ─────────────────────────────────────────────────────────────────────────
    // MANAGER — CREATE PO
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public PurchaseOrderResponse createPO(
            String managerEmail,
            PurchaseOrderRequest request
    ) {

        Users manager = getUser(managerEmail);

        if (manager.getWarehouse() == null) {

            throw new BadRequestException(
                    "Manager is not assigned to any warehouse"
            );

        }

        Warehouse warehouse = manager.getWarehouse();

        Supplier supplier = supplierRepository
                .findById(request.getSupplierId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Supplier not found: "
                                        + request.getSupplierId()
                        )
                );

        if (supplier.getApprovalStatus() != ApprovalStatus.APPROVED) {

            throw new BadRequestException(
                    "Supplier is not approved"
            );

        }

        PurchaseOrder po = new PurchaseOrder();

        po.setPoNumber(generateTemporaryPONumber());

        po.setSupplier(supplier);

        po.setWarehouse(warehouse);

        po.setCreatedBy(manager);

        po.setNote(request.getNote());

        List<PurchaseOrderItem> items =
                buildItems(
                        po,
                        request.getItems(),
                        supplier
                );

        BigDecimal total = items.stream()
                .map(PurchaseOrderItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        po.setTotalAmount(total);

        po.setItems(items);

        return purchaseOrderMapper.mapToResponse(saveWithFinalPONumber(po));

    }
    // ─────────────────────────────────────────────────────────────────────────
    // SYSTEM — AUTO-DRAFT PO ON LOW STOCK
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public void autoDraftPOIfLowStock(Product product, Warehouse warehouse) {
        // Do not create duplicate active POs for this product in this warehouse.
        if (!purchaseOrderRepository.findActiveByProductAndWarehouse(product, warehouse).isEmpty()) return;

        // Only auto-draft if a preferred supplier exists
        productSupplierRepository.findByProductAndIsPreferredTrue(product).ifPresent(ps -> {
            if (ps.getSupplier().getApprovalStatus() != ApprovalStatus.APPROVED) return;

            int orderQty = product.getReorderLevel() * 2; // order 2× the reorder level

            PurchaseOrder po = new PurchaseOrder();
            po.setPoNumber(generateTemporaryPONumber());
            po.setSupplier(ps.getSupplier());
            po.setWarehouse(warehouse);
            po.setCreatedBy(null); // SYSTEM
            po.setNote("AUTO-DRAFT: stock fell below reorder level (" + product.getReorderLevel() + ")");

            PurchaseOrderItem item = new PurchaseOrderItem();
            item.setPurchaseOrder(po);
            item.setProduct(product);
            item.setQuantity(orderQty);
            item.setPurchasePrice(ps.getPurchasePrice());
            item.setLineTotal(ps.getPurchasePrice().multiply(BigDecimal.valueOf(orderQty)));

            po.setTotalAmount(item.getLineTotal());
            po.setItems(List.of(item));

            saveWithFinalPONumber(po);
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // MANAGER — SEND PO TO SUPPLIER
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public PurchaseOrderResponse sendPO(Long poId, String managerEmail) {
        PurchaseOrder po = getPOEntity(poId);
        Users manager = getUser(managerEmail);

        validateManagerOwnsWarehouse(manager, po.getWarehouse());

        if (po.getStatus() != PurchaseOrderStatus.DRAFT) {
            throw new BadRequestException("Only DRAFT purchase orders can be sent. Current: " + po.getStatus());
        }

        po.setStatus(PurchaseOrderStatus.SENT);
        po.setSentAt(LocalDateTime.now());
        return purchaseOrderMapper.mapToResponse(purchaseOrderRepository.save(po));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // STAFF — RECEIVE PO (adds stock to inventory)
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public PurchaseOrderResponse receivePO(Long poId, String staffEmail) {
        PurchaseOrder po = getPOEntity(poId);
        Users staff = getUser(staffEmail);

        if (staff.getWarehouse() == null || !staff.getWarehouse().getId().equals(po.getWarehouse().getId())) {
            throw new ForbiddenOperationException("You can only receive POs for your own warehouse");
        }
        if (po.getStatus() != PurchaseOrderStatus.SHIPPED) {
            throw new BadRequestException("Only SHIPPED purchase orders can be received. Current: " + po.getStatus());
        }

        // Add stock and log movement for each item
        for (PurchaseOrderItem item : po.getItems()) {
            Inventory inventory = inventoryService.getOrCreateInventory(item.getProduct(), po.getWarehouse());
            inventoryService.addStock(inventory, item.getQuantity());
            stockMovementService.log(
                    inventory,
                    StockMovementType.IN,
                    item.getQuantity(),
                    po.getId(),
                    StockMovementReferenceType.PURCHASE_ORDER,
                    "PO Received: " + po.getPoNumber(),
                    staff
            );
        }

        po.setStatus(PurchaseOrderStatus.RECEIVED);
        po.setReceivedBy(staff);
        po.setReceivedAt(LocalDateTime.now());
        return purchaseOrderMapper.mapToResponse(purchaseOrderRepository.save(po));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // MANAGER/ADMIN — CANCEL PO
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public PurchaseOrderResponse cancelPO(Long poId, String userEmail) {
        PurchaseOrder po = getPOEntity(poId);

        if (po.getStatus() == PurchaseOrderStatus.RECEIVED
                || po.getStatus() == PurchaseOrderStatus.CANCELLED) {
            throw new BadRequestException("Cannot cancel a " + po.getStatus() + " purchase order");
        }

        po.setStatus(PurchaseOrderStatus.CANCELLED);
        return purchaseOrderMapper.mapToResponse(purchaseOrderRepository.save(po));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // READ — ADMIN
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    public List<PurchaseOrderResponse> getAllPOs() {
        return purchaseOrderRepository.findAll().stream()
                .map(purchaseOrderMapper::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<PurchaseOrderResponse> getPOsByStatus(PurchaseOrderStatus status) {
        return purchaseOrderRepository.findByStatus(status).stream()
                .map(purchaseOrderMapper::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<PurchaseOrderResponse> getMyWarehousePOsByStatus(
            String email,
            PurchaseOrderStatus status
    ) {

        Users staff = getUser(email);

        if (staff.getWarehouse() == null) {

            throw new BadRequestException(
                    "Staff is not assigned to any warehouse"
            );

        }

        return purchaseOrderRepository
                .findByWarehouseAndStatusOrderByCreatedAtDesc(
                        staff.getWarehouse(),
                        status
                )
                .stream()
                .map(purchaseOrderMapper::mapToResponse)
                .collect(Collectors.toList());

    }

    @Override
    public List<PurchaseOrderResponse> getMyWarehousePOs(String managerEmail) {
        Users manager = getUser(managerEmail);
        if (manager.getWarehouse() == null) throw new BadRequestException("Manager is not assigned to a warehouse");
        return purchaseOrderRepository.findByWarehouseOrderByCreatedAtDesc(manager.getWarehouse()).stream()
                .map(purchaseOrderMapper::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public PurchaseOrderResponse getPOById(Long poId) {
        return purchaseOrderMapper.mapToResponse(getPOEntity(poId));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // READ — SUPPLIER
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    public List<PurchaseOrderResponse> getMyPOs(String supplierEmail) {

        Supplier supplier = getSupplier(supplierEmail);

        List<PurchaseOrderStatus> visibleStatuses = List.of(
                PurchaseOrderStatus.SENT,
                PurchaseOrderStatus.ACCEPTED,
                PurchaseOrderStatus.SHIPPED,
                PurchaseOrderStatus.RECEIVED,
                PurchaseOrderStatus.REJECTED,
                PurchaseOrderStatus.CANCELLED
        );

        return purchaseOrderRepository
                .findBySupplierAndStatusIn(supplier, visibleStatuses)
                .stream()
                .map(purchaseOrderMapper::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<PurchaseOrderResponse> getMyPOsByStatus(
            String supplierEmail,
            PurchaseOrderStatus status
    ) {

        if (status == PurchaseOrderStatus.DRAFT) {
            throw new ForbiddenOperationException(
                    "Suppliers cannot access draft purchase orders"
            );
        }

        Supplier supplier = getSupplier(supplierEmail);

        return purchaseOrderRepository
                .findBySupplierAndStatus(supplier, status)
                .stream()
                .map(purchaseOrderMapper::mapToResponse)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SUPPLIER — ACCEPT PO
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public PurchaseOrderResponse acceptPO(Long poId, String supplierEmail) {
        PurchaseOrder po = getPOEntity(poId);
        Supplier supplier = getSupplier(supplierEmail);

        validateSupplierOwnsPO(po, supplier);
        if (po.getStatus() != PurchaseOrderStatus.SENT) {
            throw new BadRequestException("Only SENT purchase orders can be accepted. Current: " + po.getStatus());
        }

        po.setStatus(PurchaseOrderStatus.ACCEPTED);
        po.setAcceptedAt(LocalDateTime.now());
        return purchaseOrderMapper.mapToResponse(purchaseOrderRepository.save(po));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SUPPLIER — REJECT PO
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public PurchaseOrderResponse rejectPO(Long poId, String supplierEmail, PurchaseOrderRejectionRequest request) {
        PurchaseOrder po = getPOEntity(poId);
        Supplier supplier = getSupplier(supplierEmail);

        validateSupplierOwnsPO(po, supplier);
        if (po.getStatus() != PurchaseOrderStatus.SENT) {
            throw new BadRequestException("Only SENT purchase orders can be rejected. Current: " + po.getStatus());
        }

        po.setStatus(PurchaseOrderStatus.REJECTED);
        po.setRejectionReason(request.getReason());
        return purchaseOrderMapper.mapToResponse(purchaseOrderRepository.save(po));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SUPPLIER — SHIP PO (dispatch)
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public PurchaseOrderResponse shipPO(Long poId, String supplierEmail) {
        PurchaseOrder po = getPOEntity(poId);
        Supplier supplier = getSupplier(supplierEmail);

        validateSupplierOwnsPO(po, supplier);
        if (po.getStatus() != PurchaseOrderStatus.ACCEPTED) {
            throw new BadRequestException("Only ACCEPTED purchase orders can be shipped. Current: " + po.getStatus());
        }

        po.setStatus(PurchaseOrderStatus.SHIPPED);
        po.setShippedAt(LocalDateTime.now());
        return purchaseOrderMapper.mapToResponse(purchaseOrderRepository.save(po));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // INTERNAL HELPERS
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    public PurchaseOrder getPOEntity(Long poId) {
        return purchaseOrderRepository.findById(poId)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase order not found: " + poId));
    }

    private Users getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    private Supplier getSupplier(String email) {
        Users user = getUser(email);
        return supplierRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier profile not found for: " + email));
    }

    private void validateManagerOwnsWarehouse(Users manager, Warehouse poWarehouse) {
        if (manager.getWarehouse() == null || !manager.getWarehouse().getId().equals(poWarehouse.getId())) {
            throw new ForbiddenOperationException("This PO does not belong to your warehouse");
        }
    }

    private void validateSupplierOwnsPO(PurchaseOrder po, Supplier supplier) {
        if (!po.getSupplier().getId().equals(supplier.getId())) {
            throw new ForbiddenOperationException("This purchase order is not addressed to you");
        }
    }

    private String generateTemporaryPONumber() {
        return "PO-TMP-" + UUID.randomUUID();
    }

    private String generatePONumber(long sequence) {
        return "PO-" + String.format("%06d", sequence);
    }

    private PurchaseOrder saveWithFinalPONumber(PurchaseOrder po) {
        PurchaseOrder saved = purchaseOrderRepository.saveAndFlush(po);

        long sequence = saved.getId();
        String finalNumber = generatePONumber(sequence);

        while (purchaseOrderRepository.findByPoNumber(finalNumber)
                .filter(existing -> !existing.getId().equals(saved.getId()))
                .isPresent()) {
            sequence++;
            finalNumber = generatePONumber(sequence);
        }

        saved.setPoNumber(finalNumber);
        return purchaseOrderRepository.saveAndFlush(saved);
    }

    private List<PurchaseOrderItem> buildItems(PurchaseOrder po, List<PurchaseOrderItemRequest> requests, Supplier supplier) {
        List<PurchaseOrderItem> items = new ArrayList<>();
        for (PurchaseOrderItemRequest req : requests) {
            Product product = productRepository.findById(req.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + req.getProductId()));

            // Look up supplier's purchase price for this product
            ProductSupplier ps = productSupplierRepository
                    .findByProductAndSupplier(product, supplier)
                    .orElseThrow(() -> new BadRequestException(
                            "Supplier '" + supplier.getCompanyName() + "' is not linked to product '" + product.getProductName() + "'"));

            if (!ps.getIsActive()) {
                throw new BadRequestException("Supplier link for product '" + product.getProductName() + "' is inactive");
            }

            PurchaseOrderItem item = new PurchaseOrderItem();
            item.setPurchaseOrder(po);
            item.setProduct(product);
            item.setQuantity(req.getQuantity());
            item.setPurchasePrice(ps.getPurchasePrice());
            item.setLineTotal(ps.getPurchasePrice().multiply(BigDecimal.valueOf(req.getQuantity())));
            items.add(item);
        }
        return items;
    }
}