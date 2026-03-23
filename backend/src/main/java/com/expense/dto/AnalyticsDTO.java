package com.expense.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class AnalyticsDTO {

    private BigDecimal totalToday;
    private BigDecimal totalThisMonth;
    private BigDecimal dailyLimit;
    private BigDecimal monthlyLimit;
    private Map<String, BigDecimal> categoryBreakdown;
    private List<DailySpend> weeklyTrend;
    private List<String> suggestions;
    private List<String> alerts;
    private Map<String, BigDecimal> needsVsWants;

    @Data
    public static class DailySpend {
        private String date;
        private BigDecimal amount;

        public DailySpend(String date, BigDecimal amount) {
            this.date = date;
            this.amount = amount;
        }
    }
}
