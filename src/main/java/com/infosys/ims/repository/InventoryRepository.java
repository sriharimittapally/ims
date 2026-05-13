package com.infosys.ims.repository;

import com.infosys.ims.entity.Inventory;
import com.infosys.ims.entity.Product;
import com.infosys.ims.entity.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    Optional<Inventory> findByProductAndWarehouse(Product product, Warehouse warehouse);

    List<Inventory> findByWarehouse(Warehouse warehouse);

    List<Inventory> findByProduct(Product product);

    // ── GLOBAL (Admin only) ───────────────────────────────────────────────
    // FIX: Never use @Transient fields in JPQL — use the raw DB columns
    @Query("""
        SELECT i FROM Inventory i
        WHERE (i.quantity - i.reservedQuantity) <= i.product.reorderLevel
        AND i.product.status = 'ACTIVE'
        ORDER BY (i.quantity - i.reservedQuantity) ASC
    """)
    List<Inventory> findAllLowStockItems();

    @Query("""
        SELECT COALESCE(SUM(i.quantity * i.product.sellingPrice), 0)
        FROM Inventory i
        WHERE i.product.status = 'ACTIVE'
    """)
    BigDecimal calculateTotalInventoryValue();

    @Query("""
        SELECT COUNT(DISTINCT i.product.id)
        FROM Inventory i
        WHERE (i.quantity - i.reservedQuantity) <= i.product.reorderLevel
        AND i.product.status = 'ACTIVE'
    """)
    long countLowStockProductsGlobal();


    // ── WAREHOUSE-SCOPED (Manager / Staff) ────────────────────────────────
    @Query("""
        SELECT i FROM Inventory i
        WHERE i.warehouse.id = :warehouseId
        AND (i.quantity - i.reservedQuantity) <= i.product.reorderLevel
        AND i.product.status = 'ACTIVE'
        ORDER BY (i.quantity - i.reservedQuantity) ASC
    """)
    List<Inventory> findLowStockByWarehouse(@Param("warehouseId") Long warehouseId);

    @Query("""
        SELECT COALESCE(SUM(i.quantity * i.product.sellingPrice), 0)
        FROM Inventory i
        WHERE i.warehouse.id = :warehouseId
        AND i.product.status = 'ACTIVE'
    """)
    BigDecimal calculateWarehouseInventoryValue(@Param("warehouseId") Long warehouseId);

    @Query("""
        SELECT COUNT(DISTINCT i.product.id)
        FROM Inventory i
        WHERE i.warehouse.id = :warehouseId
        AND (i.quantity - i.reservedQuantity) <= i.product.reorderLevel
        AND i.product.status = 'ACTIVE'
    """)
    long countLowStockByWarehouse(@Param("warehouseId") Long warehouseId);

    @Query("""
        SELECT COUNT(DISTINCT i.product.id)
        FROM Inventory i
        WHERE i.warehouse.id = :warehouseId
        AND i.product.status = 'ACTIVE'
    """)
    long countProductsByWarehouse(@Param("warehouseId") Long warehouseId);

    @Query("""
        SELECT COALESCE(SUM(i.quantity), 0)
        FROM Inventory i
        WHERE i.warehouse.id = :warehouseId
        AND i.product.status = 'ACTIVE'
    """)
    long countTotalUnitsByWarehouse(@Param("warehouseId") Long warehouseId);

    @Query("""
       SELECT COALESCE(SUM(i.quantity), 0)
       FROM Inventory i
       WHERE i.product.status = 'ACTIVE'
       """)
    long countTotalInventoryUnits();

    @Query("""
       SELECT COUNT(DISTINCT i.product.id)
       FROM Inventory i
       WHERE i.quantity = 0
       AND i.product.status = 'ACTIVE'
       """)
    long countOutOfStockProductsGlobal();
}