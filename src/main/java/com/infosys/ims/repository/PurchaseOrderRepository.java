package com.infosys.ims.repository;

import com.infosys.ims.entity.Product;
import com.infosys.ims.entity.PurchaseOrder;
import com.infosys.ims.entity.Supplier;
import com.infosys.ims.entity.Warehouse;
import com.infosys.ims.enums.PurchaseOrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {

    Optional<PurchaseOrder> findByPoNumber(String poNumber);

    List<PurchaseOrder> findBySupplier(Supplier supplier);

    List<PurchaseOrder> findByStatus(PurchaseOrderStatus status);

    List<PurchaseOrder> findByWarehouse(Warehouse warehouse);

    List<PurchaseOrder> findByWarehouseAndStatus(Warehouse warehouse, PurchaseOrderStatus status);

    List<PurchaseOrder> findBySupplierAndStatus(Supplier supplier, PurchaseOrderStatus status);

    // ── Used by auto-draft: check if any active PO exists for a product ────
    @Query("""
        SELECT po FROM PurchaseOrder po
        JOIN po.items i
        WHERE i.product = :product
        AND po.status IN ('DRAFT','SENT','ACCEPTED','SHIPPED')
    """)
    List<PurchaseOrder> findActiveByProduct(@Param("product") Product product);

    // ── Counts: global ─────────────────────────────────────────────────────
    long countByStatus(PurchaseOrderStatus status);

    long countBySupplier(Supplier supplier);

    long countBySupplierAndStatus(Supplier supplier, PurchaseOrderStatus status);

    // ── Counts: warehouse-scoped ───────────────────────────────────────────
    long countByWarehouseAndStatus(Warehouse warehouse, PurchaseOrderStatus status);

    long countByWarehouse(Warehouse warehouse);

    // ── Total spend per warehouse ──────────────────────────────────────────
    @Query("""
        SELECT COALESCE(SUM(po.totalAmount), 0)
        FROM PurchaseOrder po
        WHERE po.warehouse.id = :warehouseId
        AND po.status = 'RECEIVED'
    """)
    BigDecimal calculateTotalSpendByWarehouse(@Param("warehouseId") Long warehouseId);

    @Query("""
        SELECT COALESCE(SUM(po.totalAmount), 0)
        FROM PurchaseOrder po
        WHERE po.status = 'RECEIVED'
    """)
    BigDecimal calculateTotalSpendGlobal();

    // ── Date-range queries ─────────────────────────────────────────────────
    List<PurchaseOrder> findByWarehouseAndCreatedAtBetween(
            Warehouse warehouse, LocalDateTime from, LocalDateTime to);

    List<PurchaseOrder> findByCreatedAtBetween(LocalDateTime from, LocalDateTime to);

    List<PurchaseOrder> findBySupplierAndCreatedAtBetween(
            Supplier supplier, LocalDateTime from, LocalDateTime to);

    // ── Supplier performance: fulfilled on time ────────────────────────────
    @Query("""
        SELECT COUNT(po) FROM PurchaseOrder po
        WHERE po.supplier.id = :supplierId
        AND po.status = 'RECEIVED'
    """)
    long countFulfilledBySupplier(@Param("supplierId") Long supplierId);

    @Query("""
        SELECT COUNT(po) FROM PurchaseOrder po
        WHERE po.supplier.id = :supplierId
        AND po.status = 'REJECTED'
    """)
    long countRejectedBySupplier(@Param("supplierId") Long supplierId);

    // ── Warehouse POs with date range ──────────────────────────────────────
    @Query("""
        SELECT po FROM PurchaseOrder po
        WHERE po.warehouse.id = :warehouseId
        ORDER BY po.createdAt DESC
    """)
    List<PurchaseOrder> findByWarehouseId(@Param("warehouseId") Long warehouseId);


    @Query("""
       SELECT COALESCE(SUM(po.totalAmount), 0)
       FROM PurchaseOrder po
       WHERE po.supplier = :supplier
       AND po.status IN (
            com.infosys.ims.enums.PurchaseOrderStatus.ACCEPTED,
            com.infosys.ims.enums.PurchaseOrderStatus.SHIPPED,
            com.infosys.ims.enums.PurchaseOrderStatus.RECEIVED
       )
       """)
    BigDecimal calculateSupplierRevenue(
            @Param("supplier") Supplier supplier
    );
}