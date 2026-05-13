package com.infosys.ims.dtos.response.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor
public class PurchaseOrderReport {
    private long totalPOs;
    private long draftPOs;
    private long sentPOs;
    private long acceptedPOs;
    private long shippedPOs;
    private long receivedPOs;
    private long rejectedPOs;
    private long cancelledPOs;
    private BigDecimal totalSpend;
    private List<POBySupplierRow> bySupplier;
    private List<POByWarehouseRow> byWarehouse;

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class POBySupplierRow {
        private Long supplierId;
        private String supplierName;
        private String companyName;
        private long totalPOs;
        private long receivedPOs;
        private BigDecimal totalAmount;
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class POByWarehouseRow {
        private Long warehouseId;
        private String warehouseName;
        private long totalPOs;
        private long receivedPOs;
        private BigDecimal totalAmount;
    }
}