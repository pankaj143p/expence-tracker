package com.expense.agent;

import org.springframework.stereotype.Component;
import java.util.Set;

@Component
public class CategorizationAgent implements Agent {

    private static final Set<String> VALID_CATEGORIES = Set.of(
        "Food", "Transport", "Shopping", "Entertainment", "Health", "Utilities", "Education", "Other"
    );
    private static final Set<String> NEEDS_CATEGORIES = Set.of(
        "Food", "Transport", "Health", "Utilities", "Education"
    );

    @Override
    public String getName() { return "CategorizationAgent"; }

    @Override
    public AgentContext process(AgentContext context) {
        String rawCategory = context.getExtractedExpense() != null
            ? context.getExtractedExpense().getCategory() : "Other";

        String category = normalize(rawCategory);
        String expenseType = NEEDS_CATEGORIES.contains(category) ? "NEED" : "WANT";

        // Update extracted expense
        if (context.getExtractedExpense() != null) {
            context.getExtractedExpense().setCategory(category);
            context.getExtractedExpense().setExpenseType(expenseType);
        }

        context.setValidatedCategory(category);
        context.setExpenseType(expenseType);
        context.addMessage(getName(), "SUCCESS", category + " → " + expenseType);
        return context;
    }

    private String normalize(String raw) {
        if (raw == null || raw.isBlank()) return "Other";
        String n = raw.trim();
        n = Character.toUpperCase(n.charAt(0)) + n.substring(1).toLowerCase();
        return VALID_CATEGORIES.contains(n) ? n : "Other";
    }
}
