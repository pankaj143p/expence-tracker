package com.expense.controller;

import com.expense.model.Budget;
import com.expense.service.BudgetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/budget")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;

    @PostMapping
    public ResponseEntity<Budget> set(
            @AuthenticationPrincipal UserDetails user,
            @RequestBody BudgetService.BudgetRequest req) {
        return ResponseEntity.ok(budgetService.setOverallBudget(user.getUsername(), req));
    }

    @GetMapping
    public ResponseEntity<Budget> get(
            @AuthenticationPrincipal UserDetails user,
            @RequestParam(defaultValue = "0") int month,
            @RequestParam(defaultValue = "0") int year) {
        int m = month > 0 ? month : LocalDate.now().getMonthValue();
        int y = year > 0 ? year : LocalDate.now().getYear();
        return ResponseEntity.ok(budgetService.getBudget(user.getUsername(), m, y));
    }
}
