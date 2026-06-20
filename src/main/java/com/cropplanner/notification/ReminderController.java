package com.cropplanner.notification;

import com.cropplanner.model.DTOs.ApiResult;
import com.cropplanner.model.User;
import com.cropplanner.notification.ReminderDTOs.CreateCustomReminderRequest;
import com.cropplanner.notification.ReminderDTOs.ReminderItem;
import com.cropplanner.security.SessionUserResolver;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reminders")
@RequiredArgsConstructor
public class ReminderController {

    private final ReminderService reminderService;
    private final SessionUserResolver sessionUserResolver;

    /** Unified upcoming reminder feed: derived planting/harvest reminders + custom reminders. */
    @GetMapping
    public ResponseEntity<List<ReminderItem>> getUpcoming(
            @RequestParam(required = false) Integer lookaheadDays,
            HttpServletRequest request) {

        User user = sessionUserResolver.requireCurrentUser(request);
        return ResponseEntity.ok(reminderService.getUpcoming(user, lookaheadDays));
    }

    @PostMapping
    public ResponseEntity<ReminderItem> createCustomReminder(
            @Valid @RequestBody CreateCustomReminderRequest req,
            HttpServletRequest request) {

        User user = sessionUserResolver.requireCurrentUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(reminderService.createCustomReminder(user, req));
    }

    @PutMapping("/{id}/complete")
    public ResponseEntity<ApiResult> markComplete(@PathVariable Long id, HttpServletRequest request) {
        User user = sessionUserResolver.requireCurrentUser(request);
        reminderService.markComplete(user, id);
        return ResponseEntity.ok(ApiResult.ok("Reminder marked as complete."));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResult> delete(@PathVariable Long id, HttpServletRequest request) {
        User user = sessionUserResolver.requireCurrentUser(request);
        reminderService.delete(user, id);
        return ResponseEntity.ok(ApiResult.ok("Reminder deleted."));
    }
}
