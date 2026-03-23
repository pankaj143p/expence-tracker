package com.expense.repository;

import com.expense.model.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findByUserIdOrderByExpenseDateDesc(Long userId);

    List<Expense> findByUserIdAndExpenseDateBetweenOrderByExpenseDateDesc(
            Long userId, LocalDate start, LocalDate end);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.user.id = :userId AND e.expenseDate = :date")
    BigDecimal sumByUserAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.user.id = :userId " +
           "AND MONTH(e.expenseDate) = :month AND YEAR(e.expenseDate) = :year")
    BigDecimal sumByUserAndMonth(@Param("userId") Long userId, @Param("month") int month, @Param("year") int year);

    @Query("SELECT e.category, COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.user.id = :userId " +
           "AND MONTH(e.expenseDate) = :month AND YEAR(e.expenseDate) = :year GROUP BY e.category")
    List<Object[]> categoryBreakdown(@Param("userId") Long userId, @Param("month") int month, @Param("year") int year);

    @Query("SELECT e.expenseDate, COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.user.id = :userId " +
           "AND e.expenseDate BETWEEN :start AND :end GROUP BY e.expenseDate ORDER BY e.expenseDate")
    List<Object[]> dailyTrend(@Param("userId") Long userId, @Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT e.category, COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.user.id = :userId " +
           "AND MONTH(e.expenseDate) = :month AND YEAR(e.expenseDate) = :year GROUP BY e.category ORDER BY SUM(e.amount) DESC")
    List<Object[]> topCategoriesByMonth(@Param("userId") Long userId, @Param("month") int month, @Param("year") int year);

    @Query("SELECT e.expenseType, COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.user.id = :userId " +
           "AND MONTH(e.expenseDate) = :month AND YEAR(e.expenseDate) = :year GROUP BY e.expenseType")
    List<Object[]> needsVsWants(@Param("userId") Long userId, @Param("month") int month, @Param("year") int year);
}
