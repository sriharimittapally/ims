package com.infosys.ims.repository;

import com.infosys.ims.entity.Product;
import com.infosys.ims.entity.ProductSupplier;
import com.infosys.ims.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductSupplierRepository extends JpaRepository<ProductSupplier, Long> {

    List<ProductSupplier> findByProduct(Product product);

    List<ProductSupplier> findBySupplier(Supplier supplier);

    Optional<ProductSupplier> findByProductAndSupplier(Product product, Supplier supplier);

    boolean existsByProductAndSupplier(Product product, Supplier supplier);

    List<ProductSupplier> findByProductAndIsActiveTrue(Product product);

    Optional<ProductSupplier> findByProductAndIsPreferredTrue(Product product);

    long countBySupplier(Supplier supplier);
}