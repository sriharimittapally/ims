// ═══════════════════════════════════════════════════════════════════
// FILE: dtos/response/report/StaffActivityReport.java
// ═══════════════════════════════════════════════════════════════════
package com.infosys.ims.dtos.response.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor
public class StaffActivityReport {
    private Long warehouseId;
    private String warehouseName;
    private List<StaffRow> staffActivity;

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class StaffRow {
        private Long staffId;
        private String staffName;
        private String userCode;
        private long totalIssuesCreated;
        private long issuesPending;
        private long issuesApproved;
        private long issuesIssued;
        private long issuesRejected;
        private long issuesCancelled;
        private long totalUnitsIssued;
    }
}