package com.infosys.ims.dtos.response.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor
public class SupplierPOReport {
    private String companyName;
    private long totalPOs;
    private long sentPOs;
    private long acceptedPOs;
    private long shippedPOs;
    private long receivedPOs;
    private long rejectedPOs;
    private BigDecimal totalRevenue;    // total amount of received POs
    private List<PORow> orders;

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class PORow {
        private Long id;
        private String poNumber;
        private String status;
        private String warehouseName;
        private BigDecimal amount;
        private int itemCount;
        private LocalDate expectedDelivery;
        private LocalDateTime createdAt;
        private LocalDateTime shippedAt;
        private LocalDateTime receivedAt;
    }
}