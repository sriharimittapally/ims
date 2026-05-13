package com.infosys.ims.repository;

import com.infosys.ims.entity.StockIssue;
import com.infosys.ims.entity.StockIssueItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockIssueItemRepository extends JpaRepository<StockIssueItem, Long> {

    List<StockIssueItem> findByStockIssue(StockIssue stockIssue);
}