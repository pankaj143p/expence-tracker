package com.expense.service;

import com.expense.model.Expense;
import com.expense.repository.ExpenseRepository;
import com.expense.repository.UserRepository;
import com.opencsv.CSVWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.io.StringWriter;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExportService {

    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;

    public String exportCsv(String username, int month, int year) {
        Long userId = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found")).getId();

        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
        List<Expense> expenses = expenseRepository
            .findByUserIdAndExpenseDateBetweenOrderByExpenseDateDesc(userId, start, end);

        StringWriter sw = new StringWriter();
        try (CSVWriter writer = new CSVWriter(sw)) {
            writer.writeNext(new String[]{"Date", "Amount", "Category", "Type", "Description"});
            expenses.forEach(e -> writer.writeNext(new String[]{
                e.getExpenseDate().toString(),
                e.getAmount().toString(),
                e.getCategory(),
                e.getExpenseType(),
                e.getDescription()
            }));
        } catch (Exception ex) {
            throw new RuntimeException("CSV export failed", ex);
        }
        return sw.toString();
    }
}
