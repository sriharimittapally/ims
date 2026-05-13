package com.infosys.ims.mapper;

import com.infosys.ims.dtos.response.StockIssueItemResponse;
import com.infosys.ims.dtos.response.StockIssueResponse;
import com.infosys.ims.entity.StockIssue;
import com.infosys.ims.entity.StockIssueItem;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class StockIssueMapper {

    public StockIssueResponse toResponse(StockIssue issue) {
        StockIssueResponse response = new StockIssueResponse();
        response.setId(issue.getId());
        response.setIssueNumber(issue.getIssueNumber());
        response.setWarehouseId(issue.getWarehouse().getId());
        response.setWarehouseName(issue.getWarehouse().getName());
        response.setWarehouseCity(issue.getWarehouse().getCity());
        // FIX: enum → String
        response.setStatus(issue.getStatus().name());
        response.setIssuedByName(issue.getIssuedBy().getName());
        if (issue.getApprovedBy() != null) {
            response.setApprovedByName(issue.getApprovedBy().getName());
        }
        response.setNote(issue.getNote());
        response.setRejectionReason(issue.getRejectionReason());
        response.setCreatedAt(issue.getCreatedAt());
        response.setApprovedAt(issue.getApprovedAt());
        response.setIssuedAt(issue.getIssuedAt());

        List itemResponses = new ArrayList<>();
        if (issue.getItems() != null) {
            for (StockIssueItem item : issue.getItems()) {
                itemResponses.add(toItemResponse(item));
            }
        }
        response.setItems(itemResponses);
        return response;
    }

    private StockIssueItemResponse toItemResponse(StockIssueItem item) {
        StockIssueItemResponse response = new StockIssueItemResponse();
        response.setId(item.getId());
        response.setProductId(item.getProduct().getId());
        response.setProductName(item.getProduct().getProductName());
        response.setSku(item.getProduct().getSku());
        response.setCategoryName(item.getProduct().getCategory().getName());
        response.setQuantityRequested(item.getQuantityRequested());
        response.setQuantityIssued(item.getQuantityIssued());
        return response;
    }
}