package com.infosys.ims.repository;

import com.infosys.ims.entity.Inventory;
import com.infosys.ims.entity.Product;
import com.infosys.ims.entity.Warehouse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Query("""
        SELECT i FROM Inventory i
        WHERE i.warehouse = :warehouse
        AND (:lowStockOnly = false OR (i.quantity - i.reservedQuantity) <= i.product.reorderLevel)
        AND (
            :search IS NULL OR :search = '' OR
            LOWER(i.product.productName) LIKE LOWER(CONCAT('%', :search, '%')) OR
            LOWER(i.product.sku) LIKE LOWER(CONCAT('%', :search, '%')) OR
            LOWER(i.product.category.name) LIKE LOWER(CONCAT('%', :search, '%'))
        )
        ORDER BY i.product.productName ASC
    """)
    Page<Inventory> searchByWarehouse(
            @Param("warehouse") Warehouse warehouse,
            @Param("search") String search,
            @Param("lowStockOnly") boolean lowStockOnly,
            Pageable pageable);

    List<Inventory> findByProduct(Product product);

    // Ã¢â€â‚¬Ã¢â€â‚¬ GLOBAL (Admin only) Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬
    // FIX: Never use @Transient fields in JPQL Ã¢â‚¬â€ use the raw DB columns
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


    // Ã¢â€â‚¬Ã¢â€â‚¬ WAREHOUSE-SCOPED (Manager / Staff) Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬
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

