package com.cropplanner.notification;

import com.cropplanner.model.PlantingSchedule;
import com.cropplanner.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/**
 * A farmer-created reminder for things the system can't derive on its own —
 * mainly fertilizer application dates and pest inspection check-ins.
 * Planting and harvest reminders don't need a row here: those are derived
 * live from PlantingSchedule dates by ReminderService, since that data
 * already exists and would otherwise duplicate/desync.
 */
@Entity
@Table(name = "custom_reminders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"user", "schedule"})
public class CustomReminder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id")
    private PlantingSchedule schedule; // optional — a reminder can be general or tied to a specific crop cycle

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReminderType type;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Builder.Default
    @Column(nullable = false)
    private boolean completed = false;

    @Column(length = 255)
    private String notes;
}
