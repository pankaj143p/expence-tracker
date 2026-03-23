package com.expense.controller;

import com.expense.dto.AnalyticsDTO;
import com.expense.dto.ExpenseDTO;
import com.expense.service.ExpenseService;
import com.expense.service.ExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;
    private final ExportService exportService;

    // Natural language input
    @PostMapping("/nl")
    public ResponseEntity<ExpenseDTO.Response> processNL(
            @AuthenticationPrincipal UserDetails user,
            @RequestBody ExpenseDTO.NLRequest req) {
        return ResponseEntity.ok(expenseService.processNaturalLanguage(user.getUsername(), req.getText()));
    }

    // Manual input
    @PostMapping
    public ResponseEntity<ExpenseDTO.Response> addManual(
            @AuthenticationPrincipal UserDetails user,
            @RequestBody ExpenseDTO.Request req) {
        return ResponseEntity.ok(expenseService.addManual(user.getUsername(), req));
    }

    @GetMapping
    public ResponseEntity<List<ExpenseDTO.Response>> getAll(@AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(expenseService.getAll(user.getUsername()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal UserDetails user, @PathVariable Long id) {
        expenseService.delete(user.getUsername(), id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/dashboard")
    public ResponseEntity<AnalyticsDTO> dashboard(
            @AuthenticationPrincipal UserDetails user,
            @RequestParam(defaultValue = "0") int month,
            @RequestParam(defaultValue = "0") int year) {
        int m = month > 0 ? month : LocalDate.now().getMonthValue();
        int y = year > 0 ? year : LocalDate.now().getYear();
        return ResponseEntity.ok(expenseService.getDashboard(user.getUsername(), m, y));
    }

    @GetMapping("/export/csv")
    public ResponseEntity<byte[]> exportCsv(
            @AuthenticationPrincipal UserDetails user,
            @RequestParam(defaultValue = "0") int month,
            @RequestParam(defaultValue = "0") int year) {
        int m = month > 0 ? month : LocalDate.now().getMonthValue();
        int y = year > 0 ? year : LocalDate.now().getYear();
        String csv = exportService.exportCsv(user.getUsername(), m, y);
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=expenses.csv")
            .contentType(MediaType.parseMediaType("text/csv"))
            .body(csv.getBytes());
    }
}
