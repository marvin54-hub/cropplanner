package com.cropplanner.admin;

import com.cropplanner.model.DTOs.ScheduleResponse;
import com.cropplanner.model.User;
import com.cropplanner.pesthealth.HealthDTOs.HealthReportResponse;
import com.cropplanner.pesthealth.HealthReportRepository;
import com.cropplanner.repository.ScheduleRepository;
import com.cropplanner.repository.UserRepository;
import com.cropplanner.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Read-only advisor view of farmer data. Access is controlled at the URL
 * level in SecurityConfig (/api/advisor/** requires ADMIN or AGRICULTURAL_ADVISOR).
 * This deliberately exposes no mutation endpoints — advisors can see, not change.
 */
@RestController
@RequestMapping("/api/advisor")
@RequiredArgsConstructor
public class AdvisorController {

    private final UserRepository userRepository;
    private final ScheduleRepository scheduleRepository;
    private final HealthReportRepository healthReportRepository;

    @GetMapping("/farmers")
    public ResponseEntity<List<UserSummary>> getAllFarmers() {
        return ResponseEntity.ok(
                userRepository.findByRole(com.cropplanner.model.Role.FARMER)
                        .stream()
                        .map(UserSummary::from)
                        .toList()
        );
    }

    @GetMapping("/farmers/{farmerId}/schedules")
    public ResponseEntity<List<ScheduleResponse>> getFarmerSchedules(@PathVariable Long farmerId) {
        User farmer = userRepository.findById(farmerId)
                .orElseThrow(() -> new ResourceNotFoundException("User", farmerId));
        return ResponseEntity.ok(
                scheduleRepository.findByUserAndDeletedAtIsNullOrderByPlantingDateDesc(farmer)
                        .stream()
                        .map(ScheduleResponse::from)
                        .toList()
        );
    }

    @GetMapping("/farmers/{farmerId}/health-reports")
    public ResponseEntity<List<HealthReportResponse>> getFarmerHealthReports(@PathVariable Long farmerId) {
        User farmer = userRepository.findById(farmerId)
                .orElseThrow(() -> new ResourceNotFoundException("User", farmerId));
        return ResponseEntity.ok(
                healthReportRepository.findByUserOrderByCreatedAtDesc(farmer)
                        .stream()
                        .map(HealthReportResponse::from)
                        .toList()
        );
    }
}
