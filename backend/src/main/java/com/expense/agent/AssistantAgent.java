package com.expense.agent;

import com.expense.repository.ExpenseRepository;
import com.expense.repository.BudgetRepository;
import com.expense.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class AssistantAgent implements Agent {

    private final OllamaClient ollamaClient;
    private final ExpenseRepository expenseRepository;
    private final BudgetRepository budgetRepository;
    private final UserRepository userRepository;

    private static final String SYSTEM_PROMPT = """
        You are a friendly and smart personal finance assistant called "ExpenseAI Assistant".
        You help users understand their spending, answer finance questions, and give budgeting advice.
        Keep responses concise (2-4 sentences max), friendly, and actionable.
        If asked about the user's spending data, use the context provided.
        Always respond in the same language the user writes in.
        """;

    @Override
    public String getName() { return "AssistantAgent"; }

    @Override
    public AgentContext process(AgentContext context) {
        // Not used in pipeline — called directly via controller
        return context;
    }

    public String chat(String username, String userMessage) {
        // Build spending context to give AI awareness of user's data
        String spendingContext = buildSpendingContext(username);
        String fullPrompt = SYSTEM_PROMPT + "\n\nUser's current financial context:\n" + spendingContext;

        String response = ollamaClient.prompt(fullPrompt, userMessage);

        if (response != null && !response.isBlank()) {
            return response;
        }

        // Fallback: rule-based responses for common questions
        return ruleFallback(userMessage);
    }

    private String buildSpendingContext(String username) {
        try {
            Long userId = userRepository.findByUsername(username).get().getId();
            LocalDate today = LocalDate.now();
            int month = today.getMonthValue();
            int year = today.getYear();

            BigDecimal todayTotal = expenseRepository.sumByUserAndDate(userId, today);
            BigDecimal monthTotal = expenseRepository.sumByUserAndMonth(userId, month, year);

            StringBuilder ctx = new StringBuilder();
            ctx.append("- Today's spending: ₹").append(todayTotal).append("\n");
            ctx.append("- This month's spending: ₹").append(monthTotal).append("\n");

            List<Object[]> topCats = expenseRepository.topCategoriesByMonth(userId, month, year);
            if (!topCats.isEmpty()) {
                ctx.append("- Top categories this month:\n");
                topCats.stream().limit(3).forEach(row ->
                    ctx.append("  * ").append(row[0]).append(": ₹").append(row[1]).append("\n"));
            }

            budgetRepository.findByUserIdAndMonthAndYearAndCategoryIsNull(userId, month, year)
                .ifPresent(b -> {
                    ctx.append("- Daily limit: ₹").append(b.getDailyLimit()).append("\n");
                    ctx.append("- Monthly limit: ₹").append(b.getMonthlyLimit()).append("\n");
                });

            return ctx.toString();
        } catch (Exception e) {
            return "No spending data available yet.";
        }
    }

    private String ruleFallback(String message) {
        String lower = message.toLowerCase();
        if (lower.contains("budget"))
            return "💡 A good budget follows the 50/30/20 rule: 50% needs, 30% wants, 20% savings. Set your limits in the Budget section!";
        if (lower.contains("save") || lower.contains("saving"))
            return "💰 Start by tracking every expense. Small daily savings add up — even ₹50/day = ₹18,000/year!";
        if (lower.contains("food") || lower.contains("eat"))
            return "🍽️ Food is often the biggest expense. Try meal prepping and limiting food delivery to 2x/week.";
        if (lower.contains("invest"))
            return "📈 Before investing, build an emergency fund of 3-6 months expenses. Then consider SIPs in mutual funds.";
        if (lower.contains("debt") || lower.contains("loan"))
            return "🏦 Pay high-interest debt first (credit cards). Then tackle lower-interest loans. Avoid new debt while paying off old.";
        return "🤖 I'm here to help with your finances! Ask me about budgeting, saving, spending habits, or anything money-related.";
    }
}
