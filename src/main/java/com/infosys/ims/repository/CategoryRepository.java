package com.infosys.ims.repository;

import com.infosys.ims.entity.Category;
import com.infosys.ims.enums.CategoryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByName(String name);

    List<Category> findByStatus(CategoryStatus status);

    boolean existsByName(String name);
}