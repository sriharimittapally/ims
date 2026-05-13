package com.infosys.ims.repository;

import com.infosys.ims.entity.Users;
import com.infosys.ims.entity.Warehouse;
import com.infosys.ims.enums.Role;
import com.infosys.ims.enums.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<Users, Long> {

    Optional<Users> findByEmail(String email);

    boolean existsByEmail(String email);

    long countByRole(Role role);

    List<Users> findByRole(Role role);

    // Used by UserServiceImpl.getStaffForMyWarehouse + ReportServiceImpl.getManagerStaffActivityReport
    List<Users> findByWarehouseAndRole(Warehouse warehouse, Role role);

    // Used by UserServiceImpl to list all users in a warehouse
    List<Users> findByWarehouse(Warehouse warehouse);

    List<Users> findByStatus(UserStatus status);
}