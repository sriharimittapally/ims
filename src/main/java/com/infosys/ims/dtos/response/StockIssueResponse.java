package com.infosys.ims.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockIssueResponse {
    private Long id;
    private String issueNumber;
    private Long warehouseId;
    private String warehouseName;
    private String warehouseCity;
    private String status;
    private String issuedByName;
    private String approvedByName;
    private String note;
    private String rejectionReason;
    private LocalDateTime createdAt;
    private LocalDateTime approvedAt;
    private LocalDateTime issuedAt;
    private List<StockIssueItemResponse> items;
}