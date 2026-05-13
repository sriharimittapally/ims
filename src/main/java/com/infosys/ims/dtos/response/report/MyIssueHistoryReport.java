// ═══════════════════════════════════════════════════════════════════
// FILE: dtos/response/report/MyIssueHistoryReport.java
// ═══════════════════════════════════════════════════════════════════
package com.infosys.ims.dtos.response.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor
public class MyIssueHistoryReport {
    private String staffName;
    private String warehouseName;
    private long totalIssues;
    private long pendingIssues;
    private long approvedIssues;
    private long issuedIssues;
    private long rejectedIssues;
    private long cancelledIssues;
    private long totalUnitsIssued;
    private List<IssueRow> issues;

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class IssueRow {
        private Long issueId;
        private String issueNumber;
        private String status;
        private int itemCount;
        private long totalUnits;
        private LocalDateTime createdAt;
        private LocalDateTime issuedAt;
        private String note;
    }
}