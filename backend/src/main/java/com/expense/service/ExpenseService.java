package com.expense.service;

import com.expense.agent.AgentContext;
import com.expense.agent.AgentOrchestrator;
import com.expense.dto.AnalyticsDTO;
import com.expense.dto.ExpenseDTO;
import com.expense.model.Expense;
import com.expense.model.User;
import com.expense.repository.BudgetRepository;
import com.expense.repository.ExpenseRepository;
import com.expense.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;
    private final BudgetRepository budgetRepository;
    private final AgentOrchestrator orchestrator;

    @Transactional
    public ExpenseDTO.Response processNaturalLanguage(String username, String text) {
        User user = getUser(username);

        // Build context and run expense pipeline (Extract → Categorize → BudgetMonitor)
        AgentContext context = new AgentContext();
        context.setRawInput(text);
        context.setUser(user);
        context = orchestrator.processExpense(context);

        Expense expense = saveExpense(user, context, text);
        return toResponse(expense);
    }

    @Transactional
    public ExpenseDTO.Response addManual(String username, ExpenseDTO.Request req) {
        User user = getUser(username);

        // For manual entry, pre-fill extracted expense and skip extraction
        AgentContext context = new AgentContext();
        context.setUser(user);
        context.setManualEntry(true);

        ExpenseDTO.ExtractedExpense extracted = new ExpenseDTO.ExtractedExpense();
        extracted.setAmount(req.getAmount());
        extracted.setCategory(req.getCategory());
        extracted.setDescription(req.getDescription());
        extracted.setDate(req.getExpenseDate() != null ? req.getExpenseDate() : LocalDate.now());
        context.setExtractedExpense(extracted);

        // Run pipeline (skips extraction, runs Categorize → BudgetMonitor)
        context = orchestrator.processExpense(context);

        Expense expense = saveExpense(user, context, null);
        return toResponse(expense);
    }

    public List<ExpenseDTO.Response> getAll(String username) {
        User user = getUser(username);
        return expenseRepository.findByUserIdOrderByExpenseDateDesc(user.getId())
            .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public AnalyticsDTO getDashboard(String username, int month, int year) {
        User user = getUser(username);

        // Run dashboard pipeline (Analytics → Recommendations)
        AgentContext context = new AgentContext();
        context.setUser(user);
        context = orchestrator.processDashboard(context);

        // Build AnalyticsDTO from context
        AnalyticsDTO dto = new AnalyticsDTO();
        dto.setTotalToday(expenseRepository.sumByUserAndDate(user.getId(), LocalDate.now()));
        dto.setTotalThisMonth(expenseRepository.sumByUserAndMonth(user.getId(), month, year));
        dto.setCategoryBreakdown(context.getCategoryBreakdown());
        dto.setNeedsVsWants(context.getNeedsVsWants());
        dto.setSuggestions(context.getSuggestions());
        dto.setAlerts(context.getAlerts());

        // Map weekly trend
        List<AnalyticsDTO.DailySpend> trend = context.getWeeklyTrend().stream()
            .map(o -> (AnalyticsDTO.DailySpend) o)
            .collect(Collectors.toList());
        dto.setWeeklyTrend(trend);

        // Budget limits for display
        budgetRepository.findByUserIdAndMonthAndYearAndCategoryIsNull(user.getId(), month, year)
            .ifPresent(b -> {
                dto.setDailyLimit(b.getDailyLimit());
                dto.setMonthlyLimit(b.getMonthlyLimit());
            });

        return dto;
    }

    @Transactional
    public void delete(String username, Long expenseId) {
        User user = getUser(username);
        Expense expense = expenseRepository.findById(expenseId)
            .orElseThrow(() -> new RuntimeException("Expense not found"));
        if (!expense.getUser().getId().equals(user.getId()))
            throw new RuntimeException("Unauthorized");
        expenseRepository.delete(expense);
    }

    private Expense saveExpense(User user, AgentContext context, String rawInput) {
        ExpenseDTO.ExtractedExpense extracted = context.getExtractedExpense();
        Expense expense = new Expense();
        expense.setUser(user);
        expense.setAmount(extracted.getAmount() != null ? extracted.getAmount() : BigDecimal.ZERO);
        expense.setCategory(context.getValidatedCategory() != null ? context.getValidatedCategory() : extracted.getCategory());
        expense.setDescription(extracted.getDescription());
        expense.setExpenseDate(extracted.getDate() != null ? extracted.getDate() : LocalDate.now());
        expense.setExpenseType(context.getExpenseType());
        expense.setRawInput(rawInput);
        return expenseRepository.save(expense);
    }

    private User getUser(String username) {
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    private ExpenseDTO.Response toResponse(Expense e) {
        ExpenseDTO.Response r = new ExpenseDTO.Response();
        r.setId(e.getId());
        r.setAmount(e.getAmount());
        r.setCategory(e.getCategory());
        r.setDescription(e.getDescription());
        r.setExpenseType(e.getExpenseType());
        r.setExpenseDate(e.getExpenseDate());
        r.setRawInput(e.getRawInput());
        return r;
    }
}
