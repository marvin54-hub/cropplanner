package com.cropplanner.expense;

import com.cropplanner.expense.ExpenseDTOs.*;
import com.cropplanner.model.DTOs.ApiResult;
import com.cropplanner.model.User;
import com.cropplanner.security.SessionUserResolver;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;
    private final SessionUserResolver sessionUserResolver;

    @PostMapping
    public ResponseEntity<ExpenseResponse> create(
            @Valid @RequestBody CreateExpenseRequest req,
            HttpServletRequest request) {

        User user = sessionUserResolver.requireCurrentUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(expenseService.create(user, req));
    }

    @GetMapping
    public ResponseEntity<List<ExpenseResponse>> getAll(
            @RequestParam(required = false) ExpenseCategory category,
            @RequestParam(required = false) Long scheduleId,
            HttpServletRequest request) {

        User user = sessionUserResolver.requireCurrentUser(request);

        if (scheduleId != null) {
            return ResponseEntity.ok(expenseService.getForSchedule(user, scheduleId));
        }
        if (category != null) {
            return ResponseEntity.ok(expenseService.getForUserByCategory(user, category));
        }
        return ResponseEntity.ok(expenseService.getForUser(user));
    }

    @GetMapping("/summary")
    public ResponseEntity<ExpenseSummaryResponse> getSummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            HttpServletRequest request) {

        User user = sessionUserResolver.requireCurrentUser(request);
        return ResponseEntity.ok(expenseService.getSummary(user, from, to));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResult> delete(@PathVariable Long id, HttpServletRequest request) {
        User user = sessionUserResolver.requireCurrentUser(request);
        expenseService.delete(user, id);
        return ResponseEntity.ok(ApiResult.ok("Expense deleted."));
    }
}
