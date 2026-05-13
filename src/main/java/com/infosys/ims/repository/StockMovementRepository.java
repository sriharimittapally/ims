package com.infosys.ims.repository;

import com.infosys.ims.entity.Product;
import com.infosys.ims.entity.StockMovement;
import com.infosys.ims.entity.Warehouse;
import com.infosys.ims.enums.StockMovementReferenceType;
import com.infosys.ims.enums.StockMovementType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {

    // ── Basic finders ─────────────────────────────────────────────────────
    List<StockMovement> findByProduct(Product product);

    List<StockMovement> findByWarehouse(Warehouse warehouse);

    List<StockMovement> findByProductAndWarehouse(Product product, Warehouse warehouse);

    List<StockMovement> findByType(StockMovementType type);

    List<StockMovement> findByReferenceIdAndReferenceType(
            Long referenceId, StockMovementReferenceType referenceType);

    // ── WAREHOUSE-SCOPED queries (Manager sees only his warehouse) ─────────
    @Query("""
        SELECT m FROM StockMovement m
        WHERE m.warehouse.id = :warehouseId
        ORDER BY m.createdAt DESC
    """)
    List<StockMovement> findByWarehouseId(@Param("warehouseId") Long warehouseId);

    @Query("""
        SELECT m FROM StockMovement m
        WHERE m.warehouse.id = :warehouseId
        AND m.product.id = :productId
        ORDER BY m.createdAt DESC
    """)
    List<StockMovement> findByWarehouseIdAndProductId(
            @Param("warehouseId") Long warehouseId,
            @Param("productId") Long productId);

    @Query("""
        SELECT m FROM StockMovement m
        WHERE m.warehouse.id = :warehouseId
        AND m.type = :type
        ORDER BY m.createdAt DESC
    """)
    List<StockMovement> findByWarehouseIdAndType(
            @Param("warehouseId") Long warehouseId,
            @Param("type") StockMovementType type);

    // ── Date-range queries (for trends) ───────────────────────────────────
    List<StockMovement> findByCreatedAtBetween(LocalDateTime from, LocalDateTime to);

    @Query("""
        SELECT m FROM StockMovement m
        WHERE m.warehouse.id = :warehouseId
        AND m.createdAt BETWEEN :from AND :to
        ORDER BY m.createdAt ASC
    """)
    List<StockMovement> findByWarehouseAndDateRange(
            @Param("warehouseId") Long warehouseId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);

    @Query("""
        SELECT m FROM StockMovement m
        WHERE m.createdAt BETWEEN :from AND :to
        ORDER BY m.createdAt ASC
    """)
    List<StockMovement> findByDateRange(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);

    // ── Aggregation: total units IN for a warehouse in a date range ────────
    @Query("""
        SELECT COALESCE(SUM(m.quantity), 0)
        FROM StockMovement m
        WHERE m.warehouse.id = :warehouseId
        AND m.type = 'IN'
        AND m.createdAt BETWEEN :from AND :to
    """)
    long sumStockInByWarehouse(
            @Param("warehouseId") Long warehouseId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);

    @Query("""
        SELECT COALESCE(SUM(m.quantity), 0)
        FROM StockMovement m
        WHERE m.warehouse.id = :warehouseId
        AND m.type = 'OUT'
        AND m.createdAt BETWEEN :from AND :to
    """)
    long sumStockOutByWarehouse(
            @Param("warehouseId") Long warehouseId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);

    // ── Top products by movement volume in a warehouse ─────────────────────
    @Query("""
        SELECT m.product.id, m.product.productName, SUM(m.quantity) as total
        FROM StockMovement m
        WHERE m.warehouse.id = :warehouseId
        AND m.type = 'OUT'
        GROUP BY m.product.id, m.product.productName
        ORDER BY total DESC
    """)
    List<Object[]> findTopProductsByOutflow(@Param("warehouseId") Long warehouseId);

    @Query("""
        SELECT m.product.id, m.product.productName, SUM(m.quantity) as total
        FROM StockMovement m
        WHERE m.type = 'OUT'
        GROUP BY m.product.id, m.product.productName
        ORDER BY total DESC
    """)
    List<Object[]> findTopProductsByOutflowGlobal();
}