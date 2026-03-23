package com.expense.agent;

import com.expense.dto.ExpenseDTO;
import com.expense.model.User;
import lombok.Data;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
public class AgentContext {

    // --- Input ---
    private String rawInput;           // natural language text
    private User user;
    private boolean manualEntry = false;

    // --- Extraction result (set by ExtractionAgent) ---
    private ExpenseDTO.ExtractedExpense extractedExpense;

    // --- Categorization result (set by CategorizationAgent) ---
    private String validatedCategory;
    private String expenseType;        // NEED or WANT

    // --- Budget result (set by BudgetMonitoringAgent) ---
    private List<String> alerts = new ArrayList<>();
    private BigDecimal todayTotal;
    private BigDecimal monthTotal;

    // --- Analytics result (set by AnalyticsAgent) ---
    private Map<String, BigDecimal> categoryBreakdown = new LinkedHashMap<>();
    private List<Object> weeklyTrend = new ArrayList<>();
    private Map<String, BigDecimal> needsVsWants = new LinkedHashMap<>();

    // --- Recommendations (set by RecommendationAgent) ---
    private List<String> suggestions = new ArrayList<>();

    // --- Agent execution log ---
    private List<AgentMessage> messages = new ArrayList<>();

    public void addMessage(String agentName, String status, String detail) {
        messages.add(new AgentMessage(agentName, status, detail));
    }

    public void addAlert(String alert) {
        alerts.add(alert);
    }
}
