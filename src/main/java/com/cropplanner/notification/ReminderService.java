package com.cropplanner.notification;

import com.cropplanner.exception.ResourceNotFoundException;
import com.cropplanner.model.PlantingSchedule;
import com.cropplanner.model.User;
import com.cropplanner.notification.ReminderDTOs.CreateCustomReminderRequest;
import com.cropplanner.notification.ReminderDTOs.ReminderItem;
import com.cropplanner.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Builds a unified reminder feed from two sources:
 *  1. Derived reminders — computed live from PlantingSchedule data
 *     (upcoming planting dates, upcoming harvest dates). Nothing is
 *     stored for these; they're always in sync with the schedule.
 *  2. Custom reminders — farmer-created entries for things the system
 *     can't derive (fertilizer applications, pest inspections).
 *
 * "Upcoming" defaults to the next 7 days but is configurable per call,
 * since different farmers may want a shorter or longer lookahead.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReminderService {

    private static final int DEFAULT_LOOKAHEAD_DAYS = 7;

    private final ScheduleRepository scheduleRepository;
    private final CustomReminderRepository customReminderRepository;

    public List<ReminderItem> getUpcoming(User user, Integer lookaheadDays) {
        int days = (lookaheadDays != null && lookaheadDays > 0) ? lookaheadDays : DEFAULT_LOOKAHEAD_DAYS;
        LocalDate today = LocalDate.now();
        LocalDate horizon = today.plusDays(days);

        List<ReminderItem> items = new ArrayList<>();
        items.addAll(derivePlantingAndHarvestReminders(user, today, horizon));
        items.addAll(getCustomReminderItems(user, today, horizon));

        return items.stream()
                .sorted(Comparator.comparing(ReminderItem::getDueDate))
                .toList();
    }

    private List<ReminderItem> derivePlantingAndHarvestReminders(User user, LocalDate today, LocalDate horizon) {
        List<ReminderItem> items = new ArrayList<>();

        for (PlantingSchedule s : scheduleRepository.findByUserAndDeletedAtIsNullOrderByPlantingDateDesc(user)) {
            // Planting reminder: the schedule's planting date hasn't arrived yet.
            // Note: schedules default to status "Planted" on creation regardless of
            // whether plantingDate is in the future, so this reminder is really
            // "you said you'd plant this" rather than tracking a separate planned state.
            if (!s.getPlantingDate().isBefore(today) && !s.getPlantingDate().isAfter(horizon)) {
                items.add(ReminderItem.builder()
                        .type(ReminderType.PLANTING)
                        .title("Plant " + s.getCrop().getName())
                        .dueDate(s.getPlantingDate().toString())
                        .overdue(false)
                        .completed(false)
                        .scheduleId(s.getId())
                        .cropName(s.getCrop().getName())
                        .derived(true)
                        .build());
            }

            // Harvest reminder: upcoming expected harvest, not yet marked Harvested.
            if (!"Harvested".equals(s.getStatus())
                    && !s.getExpectedHarvestDate().isAfter(horizon)) {
                boolean overdue = s.getExpectedHarvestDate().isBefore(today);
                items.add(ReminderItem.builder()
                        .type(ReminderType.HARVEST)
                        .title("Harvest " + s.getCrop().getName())
                        .dueDate(s.getExpectedHarvestDate().toString())
                        .overdue(overdue)
                        .completed(false)
                        .scheduleId(s.getId())
                        .cropName(s.getCrop().getName())
                        .derived(true)
                        .build());
            }
        }
        return items;
    }

    private List<ReminderItem> getCustomReminderItems(User user, LocalDate today, LocalDate horizon) {
        return customReminderRepository.findByUserOrderByDueDateAsc(user)
                .stream()
                .filter(r -> !r.isCompleted() && !r.getDueDate().isAfter(horizon))
                .map(r -> ReminderItem.builder()
                        .id(r.getId())
                        .type(r.getType())
                        .title(r.getTitle())
                        .dueDate(r.getDueDate().toString())
                        .overdue(r.getDueDate().isBefore(today))
                        .completed(r.isCompleted())
                        .scheduleId(r.getSchedule() != null ? r.getSchedule().getId() : null)
                        .cropName(r.getSchedule() != null ? r.getSchedule().getCrop().getName() : null)
                        .derived(false)
                        .build())
                .toList();
    }

    @Transactional
    public ReminderItem createCustomReminder(User user, CreateCustomReminderRequest req) {
        PlantingSchedule schedule = null;
        if (req.getScheduleId() != null) {
            schedule = scheduleRepository.findById(req.getScheduleId())
                    .orElseThrow(() -> new ResourceNotFoundException("Schedule", req.getScheduleId()));
            if (!schedule.getUser().getId().equals(user.getId())) {
                throw new ResourceNotFoundException("Schedule", req.getScheduleId());
            }
        }

        CustomReminder reminder = CustomReminder.builder()
                .user(user)
                .schedule(schedule)
                .type(req.getType())
                .title(req.getTitle())
                .dueDate(LocalDate.parse(req.getDueDate()))
                .notes(req.getNotes())
                .build();

        CustomReminder saved = customReminderRepository.save(reminder);
        log.info("Custom reminder created — user: {} type: {} due: {}", user.getEmail(), req.getType(), req.getDueDate());

        return ReminderItem.builder()
                .id(saved.getId())
                .type(saved.getType())
                .title(saved.getTitle())
                .dueDate(saved.getDueDate().toString())
                .overdue(saved.getDueDate().isBefore(LocalDate.now()))
                .completed(saved.isCompleted())
                .scheduleId(schedule != null ? schedule.getId() : null)
                .cropName(schedule != null ? schedule.getCrop().getName() : null)
                .derived(false)
                .build();
    }

    @Transactional
    public void markComplete(User user, Long reminderId) {
        CustomReminder reminder = customReminderRepository.findById(reminderId)
                .orElseThrow(() -> new ResourceNotFoundException("Reminder", reminderId));
        if (!reminder.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Reminder", reminderId);
        }
        reminder.setCompleted(true);
        customReminderRepository.save(reminder);
    }

    @Transactional
    public void delete(User user, Long reminderId) {
        CustomReminder reminder = customReminderRepository.findById(reminderId)
                .orElseThrow(() -> new ResourceNotFoundException("Reminder", reminderId));
        if (!reminder.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Reminder", reminderId);
        }
        customReminderRepository.delete(reminder);
    }
}
