package com.infosys.ims.dtos.response.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockTrendReport {

    private String from;

    private String to;

    private long totalIn;

    private long totalOut;

    private List<DailyTrend> dailyTrends;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyTrend {

        private String date;

        private long stockIn;

        private long stockOut;

    }

}