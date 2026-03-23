package com.expense.agent;

import com.expense.dto.ExpenseDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExtractionAgent implements Agent {

    private final OllamaClient ollamaClient;
    private final ObjectMapper objectMapper;

    private static final String SYSTEM_PROMPT_TEMPLATE = """
        You are an expense extraction assistant. Today's date is %s.
        Extract expense details from natural language.
        Always respond with ONLY valid JSON in this exact format:
        {"amount": 500, "category": "Food", "date": "%s", "description": "lunch"}
        Categories: Food, Transport, Shopping, Entertainment, Health, Utilities, Education, Other
        If date is not mentioned, use today's date which is %s.
        Amount must be a number. No currency symbols in amount field.
        """;

    @Override
    public String getName() { return "ExtractionAgent"; }

    @Override
    public AgentContext process(AgentContext context) {
        if (context.isManualEntry()) {
            context.addMessage(getName(), "SKIPPED", "Manual entry — no extraction needed");
            return context;
        }

        String today = LocalDate.now().toString();
        String systemPrompt = String.format(SYSTEM_PROMPT_TEMPLATE, today, today, today);
        String aiResponse = ollamaClient.prompt(systemPrompt, context.getRawInput());

        if (aiResponse != null) {
            try {
                String json = extractJson(aiResponse);
                JsonNode node = objectMapper.readTree(json);

                ExpenseDTO.ExtractedExpense result = new ExpenseDTO.ExtractedExpense();
                result.setAmount(new BigDecimal(node.get("amount").asText()));
                result.setCategory(node.get("category").asText());
                result.setDescription(node.has("description") ? node.get("description").asText() : context.getRawInput());
                String dateStr = node.has("date") ? node.get("date").asText() : null;
                LocalDate parsedDate = LocalDate.now();
                try {
                    if (dateStr != null && !dateStr.isBlank()) parsedDate = LocalDate.parse(dateStr);
                } catch (Exception ignored) {}
                result.setDate(parsedDate);

                context.setExtractedExpense(result);
                context.addMessage(getName(), "SUCCESS", "AI extracted: ₹" + result.getAmount() + " | " + result.getCategory());
                return context;
            } catch (Exception e) {
                log.warn("AI parsing failed, using regex fallback");
            }
        }

        // Regex fallback
        ExpenseDTO.ExtractedExpense result = regexFallback(context.getRawInput());
        context.setExtractedExpense(result);
        context.addMessage(getName(), "FALLBACK", "Regex extracted: ₹" + result.getAmount() + " | " + result.getCategory());
        return context;
    }

    private String extractJson(String text) {
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start >= 0 && end > start) return text.substring(start, end + 1);
        return text;
    }

    private ExpenseDTO.ExtractedExpense regexFallback(String input) {
        ExpenseDTO.ExtractedExpense result = new ExpenseDTO.ExtractedExpense();
        Pattern amountPattern = Pattern.compile("[₹$Rs.]*\\s*(\\d+(?:\\.\\d{1,2})?)");
        Matcher matcher = amountPattern.matcher(input);
        result.setAmount(matcher.find() ? new BigDecimal(matcher.group(1)) : BigDecimal.ZERO);

        String lower = input.toLowerCase();
        if (lower.matches(".*(food|eat|lunch|dinner|breakfast|restaurant|cafe|coffee|pizza|swiggy|zomato).*"))
            result.setCategory("Food");
        else if (lower.matches(".*(uber|ola|bus|train|metro|fuel|petrol|cab|auto|taxi).*"))
            result.setCategory("Transport");
        else if (lower.matches(".*(amazon|flipkart|shop|cloth|buy|purchase|mall).*"))
            result.setCategory("Shopping");
        else if (lower.matches(".*(movie|netflix|game|sport|gym|entertainment).*"))
            result.setCategory("Entertainment");
        else if (lower.matches(".*(doctor|medicine|hospital|pharmacy|health).*"))
            result.setCategory("Health");
        else if (lower.matches(".*(electricity|water|internet|bill|recharge|mobile).*"))
            result.setCategory("Utilities");
        else if (lower.matches(".*(book|course|school|college|tuition|education).*"))
            result.setCategory("Education");
        else
            result.setCategory("Other");

        result.setDate(LocalDate.now());
        result.setDescription(input);
        return result;
    }
}
