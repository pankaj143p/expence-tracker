package com.expense.agent;

import com.expense.model.Budget;
import com.expense.model.Notification;
import com.expense.repository.BudgetRepository;
import com.expense.repository.ExpenseRepository;
import com.expense.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class BudgetMonitoringAgent implements Agent {

    private final BudgetRepository budgetRepository;
    private final ExpenseRepository expenseRepository;
    private final NotificationRepository notificationRepository;

    @Override
    public String getName() { return "BudgetMonitoringAgent"; }

    @Override
    public AgentContext process(AgentContext context) {
        if (context.getUser() == null) {
            context.addMessage(getName(), "SKIPPED", "No user in context");
            return context;
        }

        LocalDate today = LocalDate.now();
        int month = today.getMonthValue();
        int year = today.getYear();
        Long userId = context.getUser().getId();

        Optional<Budget> budgetOpt = budgetRepository
            .findByUserIdAndMonthAndYearAndCategoryIsNull(userId, month, year);

        if (budgetOpt.isEmpty()) {
            context.addMessage(getName(), "SKIPPED", "No budget set for this month");
            return context;
        }

        Budget budget = budgetOpt.get();
        BigDecimal todayTotal = expenseRepository.sumByUserAndDate(userId, today);
        BigDecimal monthTotal = expenseRepository.sumByUserAndMonth(userId, month, year);

        context.setTodayTotal(todayTotal);
        context.setMonthTotal(monthTotal);

        // Daily limit check
        if (budget.getDailyLimit() != null && todayTotal.compareTo(budget.getDailyLimit()) > 0) {
            String msg = String.format(
                "⚠️ You spent ₹%.0f today, exceeding your daily limit of ₹%.0f.",
                todayTotal, budget.getDailyLimit());
            context.addAlert(msg);
            saveNotification(context, msg, "DAILY_LIMIT");
        }

        // Monthly limit check
        if (budget.getMonthlyLimit() != null) {
            BigDecimal pct = monthTotal.multiply(BigDecimal.valueOf(100))
                .divide(budget.getMonthlyLimit(), 0, java.math.RoundingMode.HALF_UP);

            if (monthTotal.compareTo(budget.getMonthlyLimit()) > 0) {
                String msg = String.format("🚨 Monthly budget exceeded! Spent ₹%.0f of ₹%.0f.",
                    monthTotal, budget.getMonthlyLimit());
                context.addAlert(msg);
                saveNotification(context, msg, "MONTHLY_LIMIT");
            } else if (pct.intValue() >= 80) {
                String msg = String.format("⚠️ %d%% of monthly budget used (₹%.0f of ₹%.0f).",
                    pct.intValue(), monthTotal, budget.getMonthlyLimit());
                context.addAlert(msg);
                saveNotification(context, msg, "MONTHLY_LIMIT");
            }
        }

        context.addMessage(getName(), "SUCCESS",
            "Today: ₹" + todayTotal + " | Month: ₹" + monthTotal + " | Alerts: " + context.getAlerts().size());
        return context;
    }

    private void saveNotification(AgentContext context, String message, String type) {
        Notification n = new Notification();
        n.setUser(context.getUser());
        n.setMessage(message);
        n.setType(type);
        notificationRepository.save(n);
    }
}
