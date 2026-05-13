// ═══════════════════════════════════════════════════════════════════
// FILE: dtos/response/report/TopProductsReport.java
// ═══════════════════════════════════════════════════════════════════
package com.infosys.ims.dtos.response.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor
public class TopProductsReport {
    private String scope;           // "GLOBAL" or warehouse name
    private List<ProductRow> topMovingProducts;
    private List<ProductRow> slowMovingProducts;

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class ProductRow {
        private Long productId;
        private String sku;
        private String productName;
        private String categoryName;
        private long totalUnitsOut;
        private int currentStock;
        private BigDecimal stockValue;
    }
}