package com.infosys.ims.service.serviceImpl;

import com.infosys.ims.dtos.response.*;
import com.infosys.ims.entity.Supplier;
import com.infosys.ims.entity.Users;
import com.infosys.ims.enums.*;
import com.infosys.ims.exception.BadRequestException;
import com.infosys.ims.exception.ResourceNotFoundException;
import com.infosys.ims.repository.*;
import com.infosys.ims.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final UserRepository userRepository;
    private final WarehouseRepository warehouseRepository;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final SupplierRepository supplierRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final StockIssueRepository stockIssueRepository;
    private final ProductSupplierRepository productSupplierRepository;

    // ── ADMIN dashboard: true global view ─────────────────────────────────
    @Override
    public AdminDashboardResponse getAdminDashboard() {
        BigDecimal totalValue = inventoryRepository.calculateTotalInventoryValue();

        return new AdminDashboardResponse(
                userRepository.count(),
                warehouseRepository.count(),
                warehouseRepository.countByStatus(WarehouseStatus.ACTIVE),
                productRepository.count(),
                supplierRepository.count(),
                supplierRepository.countByApprovalStatus(ApprovalStatus.PENDING),
                // FIX: uses warehouse-scoped JPQL that avoids @Transient
                inventoryRepository.countLowStockProductsGlobal(),
                purchaseOrderRepository.countByStatus(PurchaseOrderStatus.SENT)
                        + purchaseOrderRepository.countByStatus(PurchaseOrderStatus.ACCEPTED),
                totalValue != null ? totalValue : BigDecimal.ZERO
        );
    }

    // ── MANAGER dashboard: HIS warehouse only ─────────────────────────────
    @Override
    public ManagerDashboardResponse getManagerDashboard(String managerEmail) {

        Users manager = getUser(managerEmail);

        if (manager.getWarehouse() == null) {
            throw new BadRequestException(
                    "Manager is not assigned to any warehouse"
            );
        }

        Long warehouseId = manager.getWarehouse().getId();

        long lowStock =
                inventoryRepository.countLowStockByWarehouse(warehouseId);

        long staffCount =
                userRepository
                        .findByWarehouseAndRole(
                                manager.getWarehouse(),
                                Role.STAFF
                        )
                        .size();

        BigDecimal warehouseValue =
                inventoryRepository.calculateWarehouseInventoryValue(
                        warehouseId
                );

        long pendingPOs =
                purchaseOrderRepository.countByWarehouseAndStatus(
                        manager.getWarehouse(),
                        PurchaseOrderStatus.DRAFT
                )
                        +
                        purchaseOrderRepository.countByWarehouseAndStatus(
                                manager.getWarehouse(),
                                PurchaseOrderStatus.SENT
                        );

        long pendingIssues =
                stockIssueRepository
                        .findByWarehouseAndStatus(
                                manager.getWarehouse(),
                                StockIssueStatus.PENDING
                        )
                        .size();

        long totalProducts =
                inventoryRepository.countProductsByWarehouse(
                        warehouseId
                );

        long totalInventoryItems =
                inventoryRepository.countTotalUnitsByWarehouse(
                        warehouseId
                );

        return new ManagerDashboardResponse(
                warehouseId,
                manager.getWarehouse().getName(),

                staffCount,

                totalProducts,
                totalInventoryItems,

                lowStock,
                pendingPOs,
                pendingIssues,

                warehouseValue != null
                        ? warehouseValue
                        : BigDecimal.ZERO
        );
    }
    // ── STAFF dashboard: their own activity in their warehouse ─────────────
    @Override
    public StaffDashboardResponse getStaffDashboard(String staffEmail) {
        Users staff = getUser(staffEmail);

        if (staff.getWarehouse() == null) {
            throw new BadRequestException("Staff is not assigned to any warehouse");
        }

        // Staff only sees THEIR OWN issues and the shipped POs in their warehouse
        long pendingPOs = purchaseOrderRepository.countByWarehouseAndStatus(
                staff.getWarehouse(), PurchaseOrderStatus.SHIPPED);

        return new StaffDashboardResponse(
                staff.getWarehouse().getId(),
                staff.getWarehouse().getName(),
                stockIssueRepository.countByIssuedBy(staff),
                stockIssueRepository.countByIssuedByAndStatus(staff, StockIssueStatus.PENDING),
                stockIssueRepository.countByIssuedByAndStatus(staff, StockIssueStatus.ISSUED),
                pendingPOs
        );
    }

    // ── SUPPLIER dashboard: their own PO and product stats ────────────────
    @Override
    public SupplierDashboardResponse getSupplierDashboard(String supplierEmail) {

        Users user = getUser(supplierEmail);

        Supplier supplier = supplierRepository.findByUser(user)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Supplier profile not found"
                        )
                );
        BigDecimal totalRevenue =
                purchaseOrderRepository
                        .calculateSupplierRevenue(supplier);

        return new SupplierDashboardResponse(

                supplier.getCompanyName(),

                supplier.getApprovalStatus().name(),

                productSupplierRepository.countBySupplier(supplier),

                purchaseOrderRepository.countBySupplier(supplier),

                purchaseOrderRepository.countBySupplierAndStatus(
                        supplier,
                        PurchaseOrderStatus.SENT
                ),

                purchaseOrderRepository.countBySupplierAndStatus(
                        supplier,
                        PurchaseOrderStatus.ACCEPTED
                ),

                purchaseOrderRepository.countBySupplierAndStatus(
                        supplier,
                        PurchaseOrderStatus.SHIPPED
                ),

                purchaseOrderRepository.countBySupplierAndStatus(
                        supplier,
                        PurchaseOrderStatus.RECEIVED
                ),

                purchaseOrderRepository.countBySupplierAndStatus(
                        supplier,
                        PurchaseOrderStatus.REJECTED
                ),

                totalRevenue != null
                        ? totalRevenue
                        : BigDecimal.ZERO
        );
    }
    private Users getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }
}