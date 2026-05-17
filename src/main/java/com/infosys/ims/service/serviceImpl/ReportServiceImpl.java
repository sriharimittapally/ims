package com.infosys.ims.service.serviceImpl;

import com.infosys.ims.dtos.response.report.*;
import com.infosys.ims.entity.*;
import com.infosys.ims.enums.*;
import com.infosys.ims.exception.BadRequestException;
import com.infosys.ims.exception.ForbiddenOperationException;
import com.infosys.ims.exception.ResourceNotFoundException;
import com.infosys.ims.repository.*;
import com.infosys.ims.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final UserRepository userRepository;
    private final WarehouseRepository warehouseRepository;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final StockMovementRepository stockMovementRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final StockIssueRepository stockIssueRepository;
    private final StockIssueItemRepository stockIssueItemRepository;
    private final SupplierRepository supplierRepository;
    private final ProductSupplierRepository productSupplierRepository;

    private static final DateTimeFormatter DAY_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // =========================================================================
    // ADMIN REPORTS
    // =========================================================================

    @Override
    public InventorySummaryReport getAdminInventorySummary() {

        long totalProducts = productRepository.count();

        long totalQuantity =
                inventoryRepository.countTotalInventoryUnits();

        long totalWarehouses =
                warehouseRepository.count();

        long lowStockCount =
                inventoryRepository.countLowStockProductsGlobal();

        long outOfStockCount =
                inventoryRepository.countOutOfStockProductsGlobal();

        List<Warehouse> warehouses = warehouseRepository.findAll();

        List<InventorySummaryReport.WarehouseInventoryRow> rows =
                warehouses.stream()
                        .map(w -> {

                            BigDecimal value =
                                    inventoryRepository.calculateWarehouseInventoryValue(w.getId());

                            return new InventorySummaryReport.WarehouseInventoryRow(
                                    w.getId(),
                                    w.getName(),
                                    inventoryRepository.countProductsByWarehouse(w.getId()),
                                    inventoryRepository.countTotalUnitsByWarehouse(w.getId()),
                                    inventoryRepository.countLowStockByWarehouse(w.getId()),
                                    value != null ? value : BigDecimal.ZERO
                            );

                        })
                        .collect(Collectors.toList());

        return new InventorySummaryReport(
                totalProducts,
                totalQuantity,
                totalWarehouses,
                lowStockCount,
                outOfStockCount,
                rows
        );
    }
    @Override
    public LowStockAlertReport getAdminLowStockAlerts() {
        List<Inventory> lowStockItems = inventoryRepository.findAllLowStockItems();
        List<LowStockAlertReport.LowStockAlertRow> rows = buildLowStockRows(lowStockItems);
        return new LowStockAlertReport(rows.size(), rows);
    }

    @Override
    public PurchaseOrderReport getAdminPurchaseOrderReport() {
        List<PurchaseOrder> all = purchaseOrderRepository.findAll();
        return buildPOReport(all);
    }

    @Override
    public SupplierPerformanceReport getAdminSupplierPerformanceReport() {
        List<Supplier> suppliers = supplierRepository.findByApprovalStatus(ApprovalStatus.APPROVED);

        List<SupplierPerformanceReport.SupplierRow> rows = suppliers.stream().map(supplier -> {
            long total = purchaseOrderRepository.countBySupplier(supplier);
            long sent = purchaseOrderRepository.countBySupplierAndStatus(
                    supplier,
                    PurchaseOrderStatus.SENT
            );

            long accepted = purchaseOrderRepository.countBySupplierAndStatus(
                    supplier,
                    PurchaseOrderStatus.ACCEPTED
            );

            long shipped = purchaseOrderRepository.countBySupplierAndStatus(
                    supplier,
                    PurchaseOrderStatus.SHIPPED
            );
            long received = purchaseOrderRepository.countFulfilledBySupplier(supplier.getId());
            long rejected = purchaseOrderRepository.countRejectedBySupplier(supplier.getId());
            long pending = purchaseOrderRepository.countBySupplierAndStatus(supplier, PurchaseOrderStatus.SENT)
                    + purchaseOrderRepository.countBySupplierAndStatus(supplier, PurchaseOrderStatus.ACCEPTED)
                    + purchaseOrderRepository.countBySupplierAndStatus(supplier, PurchaseOrderStatus.SHIPPED);
            int linked = (int) productSupplierRepository.countBySupplier(supplier);

            // total spend this supplier has fulfilled
            BigDecimal spend = purchaseOrderRepository.findBySupplierAndStatus(supplier, PurchaseOrderStatus.RECEIVED)
                    .stream().map(PurchaseOrder::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            double rate = total > 0 ? (double) received / total * 100 : 0.0;

            return new SupplierPerformanceReport.SupplierRow(
                    supplier.getId(),
                    supplier.getUser().getName(),
                    supplier.getCompanyName(),
                    linked,
                    total,
                    sent,
                    accepted,
                    shipped,
                    received,
                    rejected,
                    pending,
                    Math.round(rate * 10.0) / 10.0,
                    spend
            );
        }).collect(Collectors.toList());

        return new SupplierPerformanceReport(rows);
    }

    @Override
    public StockTrendReport getAdminStockTrend(LocalDate from, LocalDate to) {
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.plusDays(1).atStartOfDay();

        List<StockMovement> movements = stockMovementRepository.findByDateRange(start, end);
        List<StockTrendReport.DailyTrend> points = buildDailyTrend(movements, from, to);

        long totalIn = movements.stream()
                .filter(m -> m.getType() == StockMovementType.IN)
                .mapToLong(StockMovement::getQuantity).sum();
        long totalOut = movements.stream()
                .filter(m -> m.getType() == StockMovementType.OUT)
                .mapToLong(StockMovement::getQuantity).sum();

        return new StockTrendReport(
                from.toString(),
                to.toString(),
                totalIn,
                totalOut,
                points
        );
    }

    @Override
    public TopProductsReport getAdminTopProducts() {
        List<Object[]> rawTopMovers = stockMovementRepository.findTopProductsByOutflowGlobal();
        return buildTopProductsReport("GLOBAL", rawTopMovers);
    }

    @Override
    public WarehouseStockReport getAdminWarehouseReport(Long warehouseId) {
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found: " + warehouseId));
        return buildWarehouseStockReport(warehouse);
    }

    // =========================================================================
    // MANAGER REPORTS  — strictly his warehouse only
    // =========================================================================

    @Override
    public WarehouseStockReport getManagerWarehouseReport(String managerEmail) {
        Warehouse warehouse = getManagerWarehouse(managerEmail);
        return buildWarehouseStockReport(warehouse);
    }

    @Override
    public LowStockAlertReport getManagerLowStockAlerts(String managerEmail) {
        Warehouse warehouse = getManagerWarehouse(managerEmail);
        // STRICTLY scoped: only items in this warehouse
        List<Inventory> lowStockItems = inventoryRepository.findLowStockByWarehouse(warehouse.getId());
        List<LowStockAlertReport.LowStockAlertRow> rows = buildLowStockRows(lowStockItems);
        return new LowStockAlertReport(rows.size(), rows);
    }

    @Override
    public StockTrendReport getManagerStockTrend(String managerEmail, LocalDate from, LocalDate to) {
        Warehouse warehouse = getManagerWarehouse(managerEmail);
        return buildWarehouseTrend(warehouse, from, to);
    }

    @Override
    public PurchaseOrderReport getManagerPOReport(String managerEmail) {
        Warehouse warehouse = getManagerWarehouse(managerEmail);
        // Only POs belonging to this warehouse
        List<PurchaseOrder> pos = purchaseOrderRepository.findByWarehouse(warehouse);
        return buildPOReport(pos);
    }

    @Override
    public StaffActivityReport getManagerStaffActivityReport(String managerEmail) {
        Warehouse warehouse = getManagerWarehouse(managerEmail);
        List<Users> staff = userRepository.findByWarehouseAndRole(warehouse, Role.STAFF);

        List<StaffActivityReport.StaffRow> rows = staff.stream().map(s -> {
            List<StockIssue> issues = stockIssueRepository.findByIssuedBy(s);

            long pending    = issues.stream().filter(i -> i.getStatus() == StockIssueStatus.PENDING).count();
            long approved   = issues.stream().filter(i -> i.getStatus() == StockIssueStatus.APPROVED).count();
            long issued     = issues.stream().filter(i -> i.getStatus() == StockIssueStatus.ISSUED).count();
            long rejected   = issues.stream().filter(i -> i.getStatus() == StockIssueStatus.REJECTED).count();
            long cancelled  = issues.stream().filter(i -> i.getStatus() == StockIssueStatus.CANCELLED).count();

            long totalUnits = issues.stream()
                    .filter(i -> i.getStatus() == StockIssueStatus.ISSUED)
                    .flatMap(i -> i.getItems().stream())
                    .mapToLong(StockIssueItem::getQuantityIssued)
                    .sum();

            return new StaffActivityReport.StaffRow(
                    s.getId(), s.getName(), s.getUserCode(),
                    issues.size(),
                    pending, approved, issued, rejected, cancelled, totalUnits
            );
        }).collect(Collectors.toList());

        return new StaffActivityReport(warehouse.getId(), warehouse.getName(), rows);
    }

    @Override
    public TopProductsReport getManagerTopProducts(String managerEmail) {
        Warehouse warehouse = getManagerWarehouse(managerEmail);
        List<Object[]> rawTopMovers = stockMovementRepository
                .findTopProductsByOutflow(warehouse.getId());
        return buildTopProductsReport(warehouse.getName(), rawTopMovers);
    }

    // =========================================================================
    // STAFF REPORTS — his warehouse + his own issues only
    // =========================================================================

    @Override
    public MyIssueHistoryReport getMyIssueHistory(String staffEmail) {
        Users staff = getUser(staffEmail);
        if (staff.getWarehouse() == null) {
            throw new BadRequestException("Staff is not assigned to any warehouse");
        }

        List<StockIssue> issues = stockIssueRepository.findByIssuedBy(staff);

        long pending   = issues.stream().filter(i -> i.getStatus() == StockIssueStatus.PENDING).count();
        long approved  = issues.stream().filter(i -> i.getStatus() == StockIssueStatus.APPROVED).count();
        long issued    = issues.stream().filter(i -> i.getStatus() == StockIssueStatus.ISSUED).count();
        long rejected  = issues.stream().filter(i -> i.getStatus() == StockIssueStatus.REJECTED).count();
        long cancelled = issues.stream().filter(i -> i.getStatus() == StockIssueStatus.CANCELLED).count();

        long totalUnits = issues.stream()
                .filter(i -> i.getStatus() == StockIssueStatus.ISSUED)
                .flatMap(i -> i.getItems().stream())
                .mapToLong(StockIssueItem::getQuantityIssued)
                .sum();

        List<MyIssueHistoryReport.IssueRow> rows = issues.stream().map(issue -> {
            int itemCount = issue.getItems() != null ? issue.getItems().size() : 0;
            long units = issue.getItems() != null
                    ? issue.getItems().stream().mapToLong(StockIssueItem::getQuantityIssued).sum() : 0;
            return new MyIssueHistoryReport.IssueRow(
                    issue.getId(), issue.getIssueNumber(),
                    issue.getStatus().name(),
                    itemCount, units,
                    issue.getCreatedAt(), issue.getIssuedAt(),
                    issue.getNote()
            );
        }).collect(Collectors.toList());

        return new MyIssueHistoryReport(
                staff.getName(), staff.getWarehouse().getName(),
                issues.size(),
                pending, approved, issued, rejected, cancelled,
                totalUnits, rows
        );
    }

    @Override
    public StockTrendReport getStaffWarehouseTrend(String staffEmail, LocalDate from, LocalDate to) {
        Users staff = getUser(staffEmail);
        if (staff.getWarehouse() == null) {
            throw new BadRequestException("Staff is not assigned to any warehouse");
        }
        // Staff sees movements for HIS warehouse only — same restriction as manager
        return buildWarehouseTrend(staff.getWarehouse(), from, to);
    }

    // =========================================================================
    // SUPPLIER REPORTS — his own data only
    // =========================================================================

    @Override
    public SupplierPOReport getSupplierPOReport(String supplierEmail) {
        Users user = getUser(supplierEmail);
        Supplier supplier = supplierRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier profile not found"));

        List<PurchaseOrder> all = purchaseOrderRepository.findBySupplier(supplier);

        long sent     = all.stream().filter(p -> p.getStatus() == PurchaseOrderStatus.SENT).count();
        long accepted = all.stream().filter(p -> p.getStatus() == PurchaseOrderStatus.ACCEPTED).count();
        long shipped  = all.stream().filter(p -> p.getStatus() == PurchaseOrderStatus.SHIPPED).count();
        long received = all.stream().filter(p -> p.getStatus() == PurchaseOrderStatus.RECEIVED).count();
        long rejected = all.stream().filter(p -> p.getStatus() == PurchaseOrderStatus.REJECTED).count();

        BigDecimal revenue = all.stream()
                .filter(p -> p.getStatus() == PurchaseOrderStatus.RECEIVED)
                .map(PurchaseOrder::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<SupplierPOReport.PORow> rows = all.stream().map(po ->
                new SupplierPOReport.PORow(
                        po.getId(), po.getPoNumber(), po.getStatus().name(),
                        po.getWarehouse().getName(), po.getTotalAmount(),
                        po.getItems() != null ? po.getItems().size() : 0,
                        po.getCreatedAt(), po.getShippedAt(), po.getReceivedAt()
                )
        ).collect(Collectors.toList());

        return new SupplierPOReport(
                supplier.getCompanyName(),
                all.size(), sent, accepted, shipped, received, rejected,
                revenue, rows
        );
    }

    // =========================================================================
    // PRIVATE HELPERS
    // =========================================================================

    private Warehouse getManagerWarehouse(String managerEmail) {
        Users manager = getUser(managerEmail);
        if (manager.getWarehouse() == null) {
            throw new BadRequestException("Manager is not assigned to any warehouse");
        }
        return manager.getWarehouse();
    }

    private Users getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    /** Build a warehouse-scoped trend report */
    private StockTrendReport buildWarehouseTrend(Warehouse warehouse, LocalDate from, LocalDate to) {
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.plusDays(1).atStartOfDay();

        List<StockMovement> movements = stockMovementRepository
                .findByWarehouseAndDateRange(warehouse.getId(), start, end);

        List<StockTrendReport.DailyTrend> points = buildDailyTrend(movements, from, to);

        long totalIn  = movements.stream()
                .filter(m -> m.getType() == StockMovementType.IN)
                .mapToLong(StockMovement::getQuantity).sum();
        long totalOut = movements.stream()
                .filter(m -> m.getType() == StockMovementType.OUT)
                .mapToLong(StockMovement::getQuantity).sum();

        return new StockTrendReport(
                warehouse.getName(),
                from + " to " + to,
                totalIn, totalOut, points
        );
    }

    /** Aggregate movements into daily IN/OUT data points */
    private List<StockTrendReport.DailyTrend> buildDailyTrend(
            List<StockMovement> movements,
            LocalDate from,
            LocalDate to) {

        Map<String, List<StockMovement>> byDay =
                movements.stream()
                        .collect(Collectors.groupingBy(m ->
                                m.getCreatedAt()
                                        .toLocalDate()
                                        .format(DAY_FMT)));

        List<StockTrendReport.DailyTrend> points =
                new ArrayList<>();

        LocalDate cursor = from;

        while (!cursor.isAfter(to)) {

            String day = cursor.format(DAY_FMT);

            List<StockMovement> dayMoves =
                    byDay.getOrDefault(day, Collections.emptyList());

            long stockIn =
                    dayMoves.stream()
                            .filter(m -> m.getType() == StockMovementType.IN)
                            .mapToLong(StockMovement::getQuantity)
                            .sum();

            long stockOut =
                    dayMoves.stream()
                            .filter(m -> m.getType() == StockMovementType.OUT)
                            .mapToLong(StockMovement::getQuantity)
                            .sum();

            points.add(
                    new StockTrendReport.DailyTrend(
                            day,
                            stockIn,
                            stockOut
                    )
            );

            cursor = cursor.plusDays(1);
        }

        return points;
    }

    /** Build a full WarehouseStockReport for a given warehouse */
    private WarehouseStockReport buildWarehouseStockReport(Warehouse warehouse) {
        List<Inventory> inventories = inventoryRepository.findByWarehouse(warehouse);

        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        LocalDateTime now           = LocalDateTime.now();

        long stockIn30  = stockMovementRepository.sumStockInByWarehouse(
                warehouse.getId(), thirtyDaysAgo, now);
        long stockOut30 = stockMovementRepository.sumStockOutByWarehouse(
                warehouse.getId(), thirtyDaysAgo, now);

        long totalUnits = inventories.stream().mapToLong(Inventory::getQuantity).sum();
        long lowStock   = inventories.stream()
                .filter(i -> i.getAvailableQuantity() <= i.getProduct().getReorderLevel()).count();
        long outOfStock = inventories.stream()
                .filter(i -> i.getAvailableQuantity() <= 0).count();

        BigDecimal value = inventories.stream()
                .map(i -> i.getProduct().getSellingPrice()
                        .multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<WarehouseStockReport.ProductStockRow> productRows = inventories.stream()
                .map(inv -> {
                    Optional<ProductSupplier> preferred =
                            productSupplierRepository.findByProductAndIsPreferredTrue(inv.getProduct());
                    String prefName = preferred.map(ps -> ps.getSupplier().getCompanyName()).orElse(null);

                    return new WarehouseStockReport.ProductStockRow(
                            inv.getProduct().getId(),
                            inv.getProduct().getSku(),
                            inv.getProduct().getProductName(),
                            inv.getProduct().getCategory().getName(),
                            inv.getProduct().getUnit(),
                            inv.getQuantity(),
                            inv.getReservedQuantity(),
                            inv.getAvailableQuantity(),
                            inv.getProduct().getReorderLevel(),
                            inv.getAvailableQuantity() <= inv.getProduct().getReorderLevel(),
                            inv.getAvailableQuantity() <= 0,
                            inv.getProduct().getSellingPrice(),
                            inv.getProduct().getSellingPrice()
                                    .multiply(BigDecimal.valueOf(inv.getQuantity())),
                            prefName
                    );
                })
                .collect(Collectors.toList());

        return new WarehouseStockReport(
                warehouse.getId(),
                warehouse.getName(),
                warehouse.getCity(),
                warehouse.getManager() != null ? warehouse.getManager().getName() : "Unassigned",
                inventories.size(),
                totalUnits,
                lowStock,
                outOfStock,
                value,
                stockIn30,
                stockOut30,
                productRows
        );
    }

    /** Build PO report from a list of POs (works for global or warehouse-scoped) */
    private PurchaseOrderReport buildPOReport(List<PurchaseOrder> pos) {
        long draft     = pos.stream().filter(p -> p.getStatus() == PurchaseOrderStatus.DRAFT).count();
        long sent      = pos.stream().filter(p -> p.getStatus() == PurchaseOrderStatus.SENT).count();
        long accepted  = pos.stream().filter(p -> p.getStatus() == PurchaseOrderStatus.ACCEPTED).count();
        long shipped   = pos.stream().filter(p -> p.getStatus() == PurchaseOrderStatus.SHIPPED).count();
        long received  = pos.stream().filter(p -> p.getStatus() == PurchaseOrderStatus.RECEIVED).count();
        long rejected  = pos.stream().filter(p -> p.getStatus() == PurchaseOrderStatus.REJECTED).count();
        long cancelled = pos.stream().filter(p -> p.getStatus() == PurchaseOrderStatus.CANCELLED).count();

        BigDecimal totalSpend = pos.stream()
                .filter(p -> p.getStatus() == PurchaseOrderStatus.RECEIVED)
                .map(PurchaseOrder::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Group by supplier
        Map<Long, List<PurchaseOrder>> bySupplier = pos.stream()
                .collect(Collectors.groupingBy(p -> p.getSupplier().getId()));

        List<PurchaseOrderReport.POBySupplierRow> supplierRows = bySupplier.entrySet().stream()
                .map(e -> {
                    Supplier sup = e.getValue().get(0).getSupplier();
                    long recv = e.getValue().stream()
                            .filter(p -> p.getStatus() == PurchaseOrderStatus.RECEIVED).count();
                    BigDecimal amt = e.getValue().stream()
                            .filter(p -> p.getStatus() == PurchaseOrderStatus.RECEIVED)
                            .map(PurchaseOrder::getTotalAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    return new PurchaseOrderReport.POBySupplierRow(
                            sup.getId(), sup.getUser().getName(), sup.getCompanyName(),
                            e.getValue().size(), recv, amt
                    );
                }).collect(Collectors.toList());

        // Group by warehouse
        Map<Long, List<PurchaseOrder>> byWarehouse = pos.stream()
                .collect(Collectors.groupingBy(p -> p.getWarehouse().getId()));

        List<PurchaseOrderReport.POByWarehouseRow> warehouseRows = byWarehouse.entrySet().stream()
                .map(e -> {
                    Warehouse wh = e.getValue().get(0).getWarehouse();
                    long recv = e.getValue().stream()
                            .filter(p -> p.getStatus() == PurchaseOrderStatus.RECEIVED).count();
                    BigDecimal amt = e.getValue().stream()
                            .filter(p -> p.getStatus() == PurchaseOrderStatus.RECEIVED)
                            .map(PurchaseOrder::getTotalAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    return new PurchaseOrderReport.POByWarehouseRow(
                            wh.getId(), wh.getName(),
                            e.getValue().size(), recv, amt
                    );
                }).collect(Collectors.toList());

        return new PurchaseOrderReport(
                pos.size(), draft, sent, accepted, shipped,
                received, rejected, cancelled, totalSpend,
                supplierRows, warehouseRows
        );
    }

    /** Build low-stock alert rows from inventory items */
    private List<LowStockAlertReport.LowStockAlertRow> buildLowStockRows(List<Inventory> items) {
        return items.stream().map(inv -> {
            Optional<ProductSupplier> preferred =
                    productSupplierRepository.findByProductAndIsPreferredTrue(inv.getProduct());

            List<PurchaseOrder> activePOs =
                    purchaseOrderRepository.findActiveByProduct(inv.getProduct());
            boolean autoDraftExists = activePOs.stream()
                    .anyMatch(po -> po.getStatus() == PurchaseOrderStatus.DRAFT);

            int deficit = Math.max(0,
                    inv.getProduct().getReorderLevel() - inv.getAvailableQuantity());

            return new LowStockAlertReport.LowStockAlertRow(
                    inv.getProduct().getId(),
                    inv.getProduct().getSku(),
                    inv.getProduct().getProductName(),
                    inv.getProduct().getCategory().getName(),
                    inv.getWarehouse().getId(),
                    inv.getWarehouse().getName(),
                    inv.getQuantity(),
                    inv.getReservedQuantity(),
                    inv.getAvailableQuantity(),
                    inv.getProduct().getReorderLevel(),
                    deficit,
                    preferred.isPresent(),
                    preferred.map(ps -> ps.getSupplier().getCompanyName()).orElse(null),
                    autoDraftExists
            );
        }).collect(Collectors.toList());
    }

    /** Build top/slow products report */
    private TopProductsReport buildTopProductsReport(String scope, List<Object[]> rawTopMovers) {
        List<TopProductsReport.ProductRow> topMoving = rawTopMovers.stream()
                .limit(10)
                .map(row -> {
                    Long productId = (Long) row[0];
                    String name    = (String) row[1];
                    long units     = ((Number) row[2]).longValue();

                    Product product = productRepository.findById(productId).orElse(null);
                    if (product == null) return null;

                    int currentStock = inventoryRepository.findByProduct(product).stream()
                            .mapToInt(Inventory::getQuantity).sum();

                    BigDecimal stockValue = product.getSellingPrice()
                            .multiply(BigDecimal.valueOf(currentStock));

                    return new TopProductsReport.ProductRow(
                            productId, product.getSku(), name,
                            product.getCategory().getName(),
                            units, currentStock, stockValue
                    );
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // Slow movers: products with the least outflow (tail of the list)
        List<TopProductsReport.ProductRow> slowMoving = rawTopMovers.stream()
                .skip(Math.max(0, rawTopMovers.size() - 10))
                .map(row -> {
                    Long productId = (Long) row[0];
                    String name    = (String) row[1];
                    long units     = ((Number) row[2]).longValue();

                    Product product = productRepository.findById(productId).orElse(null);
                    if (product == null) return null;

                    int currentStock = inventoryRepository.findByProduct(product).stream()
                            .mapToInt(Inventory::getQuantity).sum();

                    return new TopProductsReport.ProductRow(
                            productId, product.getSku(), name,
                            product.getCategory().getName(),
                            units, currentStock,
                            product.getSellingPrice().multiply(BigDecimal.valueOf(currentStock))
                    );
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return new TopProductsReport(scope, topMoving, slowMoving);
    }
}