package com.infosys.ims.repository;

import com.infosys.ims.entity.Users;
import com.infosys.ims.entity.Warehouse;
import com.infosys.ims.enums.WarehouseStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {

    Optional<Warehouse> findByName(String name);

    boolean existsByName(String name);

    List<Warehouse> findByStatus(WarehouseStatus status);

    long countByStatus(WarehouseStatus status);

    // Used by UserServiceImpl.assignManagerToWarehouse to check existing assignment
    Optional<Warehouse> findByManager(Users manager);
}