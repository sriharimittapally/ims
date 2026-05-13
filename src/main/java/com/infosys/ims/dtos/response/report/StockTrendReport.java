// ═══════════════════════════════════════════════════════════════════
// FILE: dtos/response/report/StockTrendReport.java
// ═══════════════════════════════════════════════════════════════════
package com.infosys.ims.dtos.response.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor
public class StockTrendReport {
    private String warehouseName;   // null means global (admin view)
    private String period;          // e.g. "2026-05-01 to 2026-05-10"
    private long totalUnitsIn;
    private long totalUnitsOut;
    private List<TrendDataPoint> dailyTrend;

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class TrendDataPoint {
        private String date;       // "2026-05-01"
        private long unitsIn;
        private long unitsOut;
        private long net;          // unitsIn - unitsOut
    }
}