package com.expense.agent;

import com.expense.dto.AnalyticsDTO;
import com.expense.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Component
@RequiredArgsConstructor
public class AnalyticsAgent implements Agent {

    private final ExpenseRepository expenseRepository;

    @Override
    public String getName() { return "AnalyticsAgent"; }

    @Override
    public AgentContext process(AgentContext context) {
        if (context.getUser() == null) {
            context.addMessage(getName(), "SKIPPED", "No user in context");
            return context;
        }

        Long userId = context.getUser().getId();
        LocalDate today = LocalDate.now();
        int month = today.getMonthValue();
        int year = today.getYear();

        // Category breakdown
        Map<String, BigDecimal> categoryMap = new LinkedHashMap<>();
        expenseRepository.categoryBreakdown(userId, month, year)
            .forEach(row -> categoryMap.put((String) row[0], (BigDecimal) row[1]));
        context.setCategoryBreakdown(categoryMap);

        // Weekly trend
        List<Object> trend = new ArrayList<>();
        expenseRepository.dailyTrend(userId, today.minusDays(6), today)
            .forEach(row -> trend.add(new AnalyticsDTO.DailySpend(row[0].toString(), (BigDecimal) row[1])));
        context.setWeeklyTrend(trend);

        // Needs vs Wants
        Map<String, BigDecimal> needsWants = new LinkedHashMap<>();
        expenseRepository.needsVsWants(userId, month, year)
            .forEach(row -> needsWants.put((String) row[0], (BigDecimal) row[1]));
        context.setNeedsVsWants(needsWants);

        context.addMessage(getName(), "SUCCESS",
            "Categories: " + categoryMap.size() + " | Trend days: " + trend.size());
        return context;
    }
}
