package com.infosys.ims.repository;

import com.infosys.ims.entity.Product;
import com.infosys.ims.enums.ProductStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findBySku(String sku);

    boolean existsBySku(String sku);

    boolean existsByProductNameAndCategory_Id(String productName, Long categoryId);

    List<Product> findByStatus(ProductStatus status);

    List<Product> findByCategory_Id(Long categoryId);

    List<Product> findByCategory_IdAndStatus(Long categoryId, ProductStatus status);

    // Used by ProductServiceImpl.getProductsInMyCategories (supplier browses their categories)
    List<Product> findByCategory_IdIn(List<Long> categoryIds);

    long countByCategory_Id(Long categoryId);

    long countByStatus(ProductStatus status);
}