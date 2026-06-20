package com.cropplanner.expense;

import com.cropplanner.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findByUserOrderByExpenseDateDesc(User user);

    List<Expense> findByUserAndExpenseDateBetweenOrderByExpenseDateDesc(
            User user, LocalDate from, LocalDate to);

    List<Expense> findByUserAndCategoryOrderByExpenseDateDesc(User user, ExpenseCategory category);

    List<Expense> findByUserAndSchedule_IdOrderByExpenseDateDesc(User user, Long scheduleId);

    @Query("SELECT e.category AS category, SUM(e.amount) AS total " +
           "FROM Expense e WHERE e.user = :user " +
           "AND (:from IS NULL OR e.expenseDate >= :from) " +
           "AND (:to IS NULL OR e.expenseDate <= :to) " +
           "GROUP BY e.category")
    List<CategoryTotal> sumByCategory(@Param("user") User user, @Param("from") LocalDate from, @Param("to") LocalDate to);

    /** Projection interface for the category-breakdown aggregate query above. */
    interface CategoryTotal {
        ExpenseCategory getCategory();
        Double getTotal();
    }
}
