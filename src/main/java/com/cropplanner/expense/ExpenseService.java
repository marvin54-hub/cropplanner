package com.cropplanner.expense;

import com.cropplanner.exception.ResourceNotFoundException;
import com.cropplanner.expense.ExpenseDTOs.CreateExpenseRequest;
import com.cropplanner.expense.ExpenseDTOs.ExpenseResponse;
import com.cropplanner.expense.ExpenseDTOs.ExpenseSummaryResponse;
import com.cropplanner.model.PlantingSchedule;
import com.cropplanner.model.User;
import com.cropplanner.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final ScheduleRepository scheduleRepository;

    @Transactional
    public ExpenseResponse create(User user, CreateExpenseRequest req) {
        PlantingSchedule schedule = null;
        if (req.getScheduleId() != null) {
            schedule = scheduleRepository.findById(req.getScheduleId())
                    .orElseThrow(() -> new ResourceNotFoundException("Schedule", req.getScheduleId()));
            if (!schedule.getUser().getId().equals(user.getId())) {
                throw new ResourceNotFoundException("Schedule", req.getScheduleId());
            }
        }

        Expense expense = Expense.builder()
                .user(user)
                .schedule(schedule)
                .category(req.getCategory())
                .amount(req.getAmount())
                .expenseDate(LocalDate.parse(req.getExpenseDate()))
                .notes(req.getNotes())
                .build();

        Expense saved = expenseRepository.save(expense);
        log.info("Expense logged — user: {} category: {} amount: {}", user.getEmail(), req.getCategory(), req.getAmount());
        return ExpenseResponse.from(saved);
    }

    public List<ExpenseResponse> getForUser(User user) {
        return expenseRepository.findByUserOrderByExpenseDateDesc(user)
                .stream()
                .map(ExpenseResponse::from)
                .toList();
    }

    public List<ExpenseResponse> getForUserByCategory(User user, ExpenseCategory category) {
        return expenseRepository.findByUserAndCategoryOrderByExpenseDateDesc(user, category)
                .stream()
                .map(ExpenseResponse::from)
                .toList();
    }

    public List<ExpenseResponse> getForSchedule(User user, Long scheduleId) {
        return expenseRepository.findByUserAndSchedule_IdOrderByExpenseDateDesc(user, scheduleId)
                .stream()
                .map(ExpenseResponse::from)
                .toList();
    }

    public ExpenseSummaryResponse getSummary(User user, LocalDate from, LocalDate to) {
        List<ExpenseRepository.CategoryTotal> totals = expenseRepository.sumByCategory(user, from, to);

        Map<ExpenseCategory, Double> byCategory = new EnumMap<>(ExpenseCategory.class);
        for (ExpenseCategory c : ExpenseCategory.values()) {
            byCategory.put(c, 0.0);
        }
        double grandTotal = 0.0;
        for (ExpenseRepository.CategoryTotal t : totals) {
            byCategory.put(t.getCategory(), t.getTotal());
            grandTotal += t.getTotal();
        }

        return ExpenseSummaryResponse.builder()
                .totalSpent(grandTotal)
                .byCategory(byCategory)
                .from(from)
                .to(to)
                .build();
    }

    @Transactional
    public void delete(User user, Long expenseId) {
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense", expenseId));
        if (!expense.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Expense", expenseId);
        }
        expenseRepository.delete(expense);
    }
}
