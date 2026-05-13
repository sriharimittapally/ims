package com.infosys.ims.service;

import com.infosys.ims.dtos.request.StockIssueRejectRequest;
import com.infosys.ims.dtos.response.StockIssueResponse;
import com.infosys.ims.entity.StockIssue;

import java.util.List;

public interface StockIssueService {

    StockIssueResponse createIssue(String staffEmail, String note);

    StockIssueResponse addItem(Long issueId, Long productId, int quantity, String staffEmail);

    StockIssueResponse removeItem(Long issueId, Long itemId, String staffEmail);

    StockIssueResponse cancelIssue(Long issueId, String staffEmail);

    /** Manager approves the stock issue */
    StockIssueResponse approveIssue(Long issueId, String managerEmail);

    /** Manager rejects the stock issue */
    StockIssueResponse rejectIssue(Long issueId, String managerEmail, StockIssueRejectRequest request);

    /** Staff executes approved issue — deducts stock */
    StockIssueResponse issueStock(Long issueId, String staffEmail);

    List<StockIssueResponse> getIssuesCreatedBy(String staffEmail);

    List<StockIssueResponse> getPendingIssuesForWarehouse(String managerEmail);

    List<StockIssueResponse> getAllIssuesForWarehouse(String managerEmail);

    StockIssueResponse getIssueById(Long issueId);

    StockIssue getIssueEntity(Long issueId);
}