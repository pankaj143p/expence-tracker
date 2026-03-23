package com.expense.repository;

import com.expense.model.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface BudgetRepository extends JpaRepository<Budget, Long> {
    Optional<Budget> findByUserIdAndMonthAndYearAndCategoryIsNull(Long userId, int month, int year);
    Optional<Budget> findByUserIdAndMonthAndYearAndCategory(Long userId, int month, int year, String category);
}
