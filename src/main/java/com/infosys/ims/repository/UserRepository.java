package com.infosys.ims.repository;

import com.infosys.ims.entity.Users;
import com.infosys.ims.entity.Warehouse;
import com.infosys.ims.enums.Role;
import com.infosys.ims.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<Users, Long> {

    Optional<Users> findByEmail(String email);

    boolean existsByEmail(String email);

    long countByRole(Role role);

    List<Users> findByRole(Role role);


    @Query("""
        SELECT u FROM Users u
        LEFT JOIN u.warehouse w
        WHERE u.role = :role
        AND (:status IS NULL OR u.status = :status)
        AND (
            :search IS NULL OR :search = '' OR
            LOWER(u.name) LIKE LOWER(CONCAT('%', :search, '%')) OR
            LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR
            LOWER(u.phone) LIKE LOWER(CONCAT('%', :search, '%')) OR
            LOWER(u.userCode) LIKE LOWER(CONCAT('%', :search, '%')) OR
            LOWER(w.name) LIKE LOWER(CONCAT('%', :search, '%'))
        )
        ORDER BY u.name ASC
    """)
    Page<Users> searchByRole(
            @Param("role") Role role,
            @Param("status") UserStatus status,
            @Param("search") String search,
            Pageable pageable);
    // Used by UserServiceImpl.getStaffForMyWarehouse + ReportServiceImpl.getManagerStaffActivityReport
    List<Users> findByWarehouseAndRole(Warehouse warehouse, Role role);

    @Query("""
        SELECT u FROM Users u
        WHERE u.warehouse = :warehouse
        AND u.role = :role
        AND (:status IS NULL OR u.status = :status)
        AND (
            :search IS NULL OR :search = '' OR
            LOWER(u.name) LIKE LOWER(CONCAT('%', :search, '%')) OR
            LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR
            LOWER(u.phone) LIKE LOWER(CONCAT('%', :search, '%')) OR
            LOWER(u.userCode) LIKE LOWER(CONCAT('%', :search, '%'))
        )
        ORDER BY u.name ASC
    """)
    Page<Users> searchByWarehouseAndRole(
            @Param("warehouse") Warehouse warehouse,
            @Param("role") Role role,
            @Param("status") UserStatus status,
            @Param("search") String search,
            Pageable pageable);

    // Used by UserServiceImpl to list all users in a warehouse
    List<Users> findByWarehouse(Warehouse warehouse);

    List<Users> findByStatus(UserStatus status);
}

