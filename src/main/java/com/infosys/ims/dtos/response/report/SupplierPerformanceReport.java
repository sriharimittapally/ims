// ═══════════════════════════════════════════════════════════════════
// FILE: dtos/response/report/SupplierPerformanceReport.java
// ═══════════════════════════════════════════════════════════════════
package com.infosys.ims.dtos.response.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor
public class SupplierPerformanceReport {
    private List<SupplierRow> suppliers;

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class SupplierRow {
        private Long supplierId;
        private String supplierName;
        private String companyName;
        private int linkedProducts;
        private long totalPOs;
        private long sentPOs;
        private long acceptedPOs;
        private long shippedPOs;
        private long receivedPOs;
        private long rejectedPOs;
        private long pendingPOs;      // SENT + ACCEPTED + SHIPPED
        private double fulfillmentRate; // receivedPOs / totalPOs * 100
        private BigDecimal totalSpend;
    }
}