package com.cropplanner.expense;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Map;

public class ExpenseDTOs {

    @Data
    public static class CreateExpenseRequest {
        @NotNull(message = "Category is required")
        private ExpenseCategory category;

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
        private Double amount;

        @NotNull(message = "Expense date is required")
        private String expenseDate; // ISO yyyy-MM-dd, parsed in service

        private Long scheduleId; // optional

        private String notes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExpenseResponse {
        private Long id;
        private ExpenseCategory category;
        private double amount;
        private String expenseDate;
        private Long scheduleId;
        private String cropName; // null if not linked to a schedule
        private String notes;

        public static ExpenseResponse from(Expense e) {
            return ExpenseResponse.builder()
                    .id(e.getId())
                    .category(e.getCategory())
                    .amount(e.getAmount())
                    .expenseDate(e.getExpenseDate().toString())
                    .scheduleId(e.getSchedule() != null ? e.getSchedule().getId() : null)
                    .cropName(e.getSchedule() != null ? e.getSchedule().getCrop().getName() : null)
                    .notes(e.getNotes())
                    .build();
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExpenseSummaryResponse {
        private double totalSpent;
        private Map<ExpenseCategory, Double> byCategory;
        private LocalDate from; // null if unbounded
        private LocalDate to;   // null if unbounded
    }
}
