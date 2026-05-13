package com.infosys.ims.repository;

import com.infosys.ims.entity.Supplier;
import com.infosys.ims.entity.Users;
import com.infosys.ims.enums.ApprovalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    Optional<Supplier> findByUser(Users user);

    List<Supplier> findByApprovalStatus(ApprovalStatus approvalStatus);

    boolean existsByUser(Users user);

    long countByApprovalStatus(ApprovalStatus status);
}