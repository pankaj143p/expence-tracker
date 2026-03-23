package com.expense.service;

import com.expense.model.Budget;
import com.expense.model.User;
import com.expense.repository.BudgetRepository;
import com.expense.repository.UserRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final UserRepository userRepository;

    @Transactional
    public Budget setOverallBudget(String username, BudgetRequest req) {
        User user = getUser(username);
        int month = req.getMonth() > 0 ? req.getMonth() : LocalDate.now().getMonthValue();
        int year = req.getYear() > 0 ? req.getYear() : LocalDate.now().getYear();

        Budget budget = budgetRepository
            .findByUserIdAndMonthAndYearAndCategoryIsNull(user.getId(), month, year)
            .orElse(new Budget());

        budget.setUser(user);
        budget.setDailyLimit(req.getDailyLimit());
        budget.setMonthlyLimit(req.getMonthlyLimit());
        budget.setMonth(month);
        budget.setYear(year);
        return budgetRepository.save(budget);
    }

    public Budget getBudget(String username, int month, int year) {
        User user = getUser(username);
        return budgetRepository.findByUserIdAndMonthAndYearAndCategoryIsNull(user.getId(), month, year)
            .orElse(null);
    }

    private User getUser(String username) {
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @Data
    public static class BudgetRequest {
        private BigDecimal dailyLimit;
        private BigDecimal monthlyLimit;
        private int month;
        private int year;
    }
}
