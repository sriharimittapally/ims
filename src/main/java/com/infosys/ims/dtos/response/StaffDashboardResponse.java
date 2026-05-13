package com.infosys.ims.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StaffDashboardResponse {
    private Long id;
    private String warehouseName;
    private long myTotalIssues;
    private long myPendingIssues;
    private long myIssuedIssues;
    private long pendingPOsToReceive;
}