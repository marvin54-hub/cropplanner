package com.cropplanner.notification;

import com.cropplanner.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CustomReminderRepository extends JpaRepository<CustomReminder, Long> {
    List<CustomReminder> findByUserOrderByDueDateAsc(User user);
    List<CustomReminder> findByUserAndCompletedFalseAndDueDateLessThanEqualOrderByDueDateAsc(User user, LocalDate maxDate);
}
