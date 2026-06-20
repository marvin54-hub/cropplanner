package com.cropplanner.service;

import com.cropplanner.exception.BusinessRuleException;
import com.cropplanner.exception.ResourceNotFoundException;
import com.cropplanner.model.Crop;
import com.cropplanner.model.DTOs.CreateScheduleRequest;
import com.cropplanner.model.DTOs.DashboardStats;
import com.cropplanner.model.DTOs.ScheduleResponse;
import com.cropplanner.model.PlantingSchedule;
import com.cropplanner.model.User;
import com.cropplanner.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * Owns all PlantingSchedule business rules: creating schedules (including
 * computing the expected harvest date from a crop's growth duration),
 * status transitions, ownership checks, and dashboard aggregation.
 *
 * This used to live directly inside ScheduleController. Pulling it out
 * means the controller is now just HTTP plumbing (parse request -> call
 * service -> map response), and the same rules are reusable — e.g. the
 * upcoming Notifications feature can call getUpcomingHarvests() directly
 * instead of re-deriving the query.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduleService {

    private static final Set<String> VALID_STATUSES = Set.of("Planted", "Growing", "Harvested", "Failed");

    private final ScheduleRepository scheduleRepository;
    private final CropService cropService;

    @Transactional
    @CacheEvict(value = "analyticsReport", key = "#user.id")
    public PlantingSchedule create(User user, CreateScheduleRequest req) {
        Crop crop = cropService.getById(req.getCropId());

        LocalDate plantDate = LocalDate.parse(req.getPlantingDate());
        LocalDate harvestDate = plantDate.plusDays(crop.getGrowthDurationDays());

        PlantingSchedule schedule = PlantingSchedule.builder()
                .user(user)
                .crop(crop)
                .plantingDate(plantDate)
                .expectedHarvestDate(harvestDate)
                .notes(req.getNotes())
                .status("Planted")
                .build();

        PlantingSchedule saved = scheduleRepository.save(schedule);
        log.info("Schedule created — user: {} crop: {}", user.getEmail(), crop.getName());
        return saved;
    }

    public List<ScheduleResponse> getForUser(User user) {
        return scheduleRepository.findByUserAndDeletedAtIsNullOrderByPlantingDateDesc(user)
                .stream()
                .map(ScheduleResponse::from)
                .toList();
    }

    @Transactional
    public PlantingSchedule updateStatus(User user, Long scheduleId, String newStatus) {
        if (!VALID_STATUSES.contains(newStatus)) {
            throw new BusinessRuleException(
                    "Invalid status '" + newStatus + "'. Must be one of: " + VALID_STATUSES);
        }
        PlantingSchedule schedule = getOwnedSchedule(user, scheduleId);
        schedule.setStatus(newStatus);
        return scheduleRepository.save(schedule);
    }

    @Transactional
    @CacheEvict(value = "analyticsReport", key = "#user.id")
    public void delete(User user, Long scheduleId) {
        PlantingSchedule schedule = getOwnedSchedule(user, scheduleId);
        schedule.setDeletedAt(LocalDate.now().atStartOfDay());
        scheduleRepository.save(schedule);
    }

    public DashboardStats getDashboardStats(User user) {
        DashboardStats stats = new DashboardStats();
        stats.setTotalSchedules(scheduleRepository.countByUserAndDeletedAtIsNull(user));
        stats.setUpcomingHarvests(getUpcomingHarvests(user).size());
        stats.setCurrentSeason(getCurrentSeason());
        stats.setFarmerName(user.getFullName());
        stats.setFarmerLocation(user.getLocation() != null ? user.getLocation() : "");
        return stats;
    }

    public List<PlantingSchedule> getUpcomingHarvests(User user) {
        return scheduleRepository
                .findByUserAndStatusNotAndExpectedHarvestDateGreaterThanEqualAndDeletedAtIsNullOrderByExpectedHarvestDateAsc(
                        user, "Harvested", LocalDate.now());
    }

    private PlantingSchedule getOwnedSchedule(User user, Long scheduleId) {
        return scheduleRepository.findByIdAndUser(scheduleId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule", scheduleId));
    }

    private String getCurrentSeason() {
        int month = LocalDate.now().getMonthValue();
        return (month >= 10 || month <= 3) ? "Summer" : "Winter";
    }
}
