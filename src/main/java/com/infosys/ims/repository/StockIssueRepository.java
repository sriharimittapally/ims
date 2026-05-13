package com.infosys.ims.repository;

import com.infosys.ims.entity.StockIssue;
import com.infosys.ims.entity.Users;
import com.infosys.ims.entity.Warehouse;
import com.infosys.ims.enums.StockIssueStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockIssueRepository extends JpaRepository<StockIssue, Long> {

    Optional<StockIssue> findByIssueNumber(String issueNumber);

    // Used by StockIssueServiceImpl: staff sees only their own issues
    List<StockIssue> findByIssuedBy(Users user);

    List<StockIssue> findByStatus(StockIssueStatus status);

    // Used by StockIssueServiceImpl + DashboardServiceImpl: scoped to warehouse
    List<StockIssue> findByWarehouse(Warehouse warehouse);

    // Used by StockIssueServiceImpl.getPendingIssuesForWarehouse + DashboardServiceImpl
    List<StockIssue> findByWarehouseAndStatus(Warehouse warehouse, StockIssueStatus status);

    long count();

    long countByStatus(StockIssueStatus status);

    // Used by StaffDashboardResponse
    long countByIssuedBy(Users user);

    long countByIssuedByAndStatus(Users user, StockIssueStatus status);

    // Used by DashboardServiceImpl (manager sees count of all issues in his warehouse)
    long countByWarehouse(Warehouse warehouse);

    long countByWarehouseAndStatus(Warehouse warehouse, StockIssueStatus status);
}