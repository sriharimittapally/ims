package com.infosys.ims.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SupplierDashboardResponse {
    private String companyName;
    private String approvalStatus;

    private long linkedProducts;


    private long totalPOs;
    private long pendingPOs;
    private long acceptedPOs;
    private long shippedPOs;
    private long receivedPOs;
    private long rejectedPOs;
    private BigDecimal totalRevenue;

}

