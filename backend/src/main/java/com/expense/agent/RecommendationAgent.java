package com.expense.agent;

import com.expense.repository.ExpenseRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class RecommendationAgent implements Agent {

    private final ExpenseRepository expenseRepository;
    private final OllamaClient ollamaClient;

    private static final String SYSTEM_PROMPT = """
        You are a personal finance advisor. Based on the spending data provided,
        give 3 concise, actionable money-saving suggestions. Be specific and friendly.
        Format: Return only a JSON array of strings. Example: ["suggestion1", "suggestion2", "suggestion3"]
        """;

    @Override
    public String getName() { return "RecommendationAgent"; }

    @Override
    public AgentContext process(AgentContext context) {
        if (context.getUser() == null) {
            context.addMessage(getName(), "SKIPPED", "No user in context");
            return context;
        }

        LocalDate today = LocalDate.now();
        List<Object[]> topCategories = expenseRepository.topCategoriesByMonth(
            context.getUser().getId(), today.getMonthValue(), today.getYear());

        if (topCategories.isEmpty()) {
            context.setSuggestions(List.of("Start adding expenses to get personalized suggestions!"));
            context.addMessage(getName(), "SKIPPED", "No expense data yet");
            return context;
        }

        // Build summary for AI
        StringBuilder summary = new StringBuilder("Monthly spending:\n");
        topCategories.stream().limit(5).forEach(row ->
            summary.append(String.format("- %s: ₹%.0f\n", row[0], row[1])));

        // Try AI
        String aiResponse = ollamaClient.prompt(SYSTEM_PROMPT, summary.toString());
        if (aiResponse != null) {
            try {
                String json = aiResponse.substring(aiResponse.indexOf('['), aiResponse.lastIndexOf(']') + 1);
                List<String> suggestions = new ObjectMapper().readValue(json, List.class);
                if (!suggestions.isEmpty()) {
                    context.setSuggestions(suggestions);
                    context.addMessage(getName(), "SUCCESS", "AI generated " + suggestions.size() + " suggestions");
                    return context;
                }
            } catch (Exception e) {
                log.warn("AI suggestion parsing failed, using rule-based fallback");
            }
        }

        List<String> suggestions = ruleBasedSuggestions(topCategories);
        context.setSuggestions(suggestions);
        context.addMessage(getName(), "FALLBACK", "Rule-based: " + suggestions.size() + " suggestions");
        return context;
    }

    private List<String> ruleBasedSuggestions(List<Object[]> topCategories) {
        List<String> suggestions = new ArrayList<>();
        Map<String, BigDecimal> spending = new LinkedHashMap<>();
        topCategories.forEach(row -> spending.put((String) row[0], (BigDecimal) row[1]));

        if (spending.getOrDefault("Food", BigDecimal.ZERO).compareTo(BigDecimal.valueOf(3000)) > 0)
            suggestions.add("🍽️ High food spend. Consider cooking at home to save up to 60%.");
        if (spending.getOrDefault("Entertainment", BigDecimal.ZERO).compareTo(BigDecimal.valueOf(1000)) > 0)
            suggestions.add("🎬 Review subscriptions and cancel unused ones.");
        if (spending.getOrDefault("Shopping", BigDecimal.ZERO).compareTo(BigDecimal.valueOf(2000)) > 0)
            suggestions.add("🛍️ Apply the 24-hour rule before buying non-essentials.");
        if (spending.getOrDefault("Transport", BigDecimal.ZERO).compareTo(BigDecimal.valueOf(1500)) > 0)
            suggestions.add("🚌 Use public transport or carpool to cut travel costs.");
        if (suggestions.isEmpty())
            suggestions.add("✅ Spending looks balanced. Keep tracking to maintain good habits.");

        return suggestions;
    }
}
