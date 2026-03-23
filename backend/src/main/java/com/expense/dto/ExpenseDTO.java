package com.expense.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

public class ExpenseDTO {

    @Data
    public static class NLRequest {
        private String text; // "Spent 500 on food today"
    }

    @Data
    public static class Request {
        private BigDecimal amount;
        private String category;
        private String description;
        private LocalDate expenseDate;
    }

    @Data
    public static class Response {
        private Long id;
        private BigDecimal amount;
        private String category;
        private String description;
        private String expenseType;
        private LocalDate expenseDate;
        private String rawInput;
    }

    @Data
    public static class ExtractedExpense {
        private BigDecimal amount;
        private String category;
        private LocalDate date;
        private String expenseType;
        private String description;
    }
}
