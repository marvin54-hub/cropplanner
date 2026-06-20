package com.cropplanner.service;

import com.cropplanner.exception.BusinessRuleException;
import com.cropplanner.exception.ResourceNotFoundException;
import com.cropplanner.model.*;
import com.cropplanner.model.DTOs.CreateScheduleRequest;
import com.cropplanner.repository.ScheduleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScheduleServiceTest {

    @Mock private ScheduleRepository scheduleRepository;
    @Mock private CropService cropService;
    @InjectMocks private ScheduleService scheduleService;

    private User user;
    private Crop crop;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).fullName("Thabo Nkosi").email("thabo@farm.za").role(Role.FARMER).build();
        crop = Crop.builder().id(10L).name("Maize").season("Summer").growthDurationDays(90)
                .avgYieldKgPerHectare(3500.0).build();
    }

    @Test
    @DisplayName("create() calculates harvest date from crop growth duration")
    void create_calculatesHarvestDateFromGrowthDuration() {
        CreateScheduleRequest req = new CreateScheduleRequest();
        req.setCropId(10L);
        req.setPlantingDate("2024-10-01");
        req.setNotes("First plot");

        when(cropService.getById(10L)).thenReturn(crop);
        when(scheduleRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PlantingSchedule saved = scheduleService.create(user, req);

        assertThat(saved.getExpectedHarvestDate()).isEqualTo(LocalDate.of(2024, 12, 30));
        assertThat(saved.getStatus()).isEqualTo("Planted");
        assertThat(saved.getUser()).isEqualTo(user);
    }

    @Test
    @DisplayName("updateStatus() rejects invalid status values")
    void updateStatus_rejectsInvalidStatus() {
        PlantingSchedule schedule = PlantingSchedule.builder().id(5L).user(user).crop(crop)
                .plantingDate(LocalDate.now()).expectedHarvestDate(LocalDate.now().plusDays(90))
                .status("Planted").build();

        when(scheduleRepository.findByIdAndUser(5L, user)).thenReturn(Optional.of(schedule));

        assertThatThrownBy(() -> scheduleService.updateStatus(user, 5L, "Sprouting"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Invalid status");
    }

    @Test
    @DisplayName("updateStatus() accepts valid status 'Harvested'")
    void updateStatus_acceptsValidStatus() {
        PlantingSchedule schedule = PlantingSchedule.builder().id(5L).user(user).crop(crop)
                .plantingDate(LocalDate.now()).expectedHarvestDate(LocalDate.now().plusDays(90))
                .status("Planted").build();

        when(scheduleRepository.findByIdAndUser(5L, user)).thenReturn(Optional.of(schedule));
        when(scheduleRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PlantingSchedule result = scheduleService.updateStatus(user, 5L, "Harvested");

        assertThat(result.getStatus()).isEqualTo("Harvested");
    }

    @Test
    @DisplayName("delete() sets deletedAt for soft-delete (not a hard delete)")
    void delete_softDeletesSetsDeletedAt() {
        PlantingSchedule schedule = PlantingSchedule.builder().id(5L).user(user).crop(crop)
                .plantingDate(LocalDate.now()).expectedHarvestDate(LocalDate.now().plusDays(90))
                .status("Planted").build();

        when(scheduleRepository.findByIdAndUser(5L, user)).thenReturn(Optional.of(schedule));
        when(scheduleRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        scheduleService.delete(user, 5L);

        assertThat(schedule.getDeletedAt()).isNotNull();
        verify(scheduleRepository, never()).delete(any(PlantingSchedule.class));
    }

    @Test
    @DisplayName("getOwnedSchedule returns 404 for another user's schedule ID")
    void getOwnedSchedule_throwsForWrongUser() {
        when(scheduleRepository.findByIdAndUser(99L, user)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> scheduleService.updateStatus(user, 99L, "Harvested"))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
