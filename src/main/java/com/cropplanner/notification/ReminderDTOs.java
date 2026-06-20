package com.cropplanner.notification;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class ReminderDTOs {

    @Data
    public static class CreateCustomReminderRequest {
        @NotNull(message = "Reminder type is required")
        private ReminderType type;

        @NotBlank(message = "Title is required")
        private String title;

        @NotNull(message = "Due date is required")
        private String dueDate; // ISO yyyy-MM-dd

        private Long scheduleId; // optional

        private String notes;
    }

    /**
     * Unified shape for both derived reminders (planting/harvest, computed
     * live from schedules — id is null since there's no row to reference)
     * and custom reminders (id present, can be marked complete/deleted).
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReminderItem {
        private Long id;          // null for derived reminders
        private ReminderType type;
        private String title;
        private String dueDate;
        private boolean overdue;
        private boolean completed;
        private Long scheduleId;
        private String cropName;
        private boolean derived;  // true = computed from schedule data, false = a saved CustomReminder
    }
}
