package com.cropplanner.controller;

import com.cropplanner.model.DTOs.*;
import com.cropplanner.model.PlantingSchedule;
import com.cropplanner.model.User;
import com.cropplanner.security.SessionUserResolver;
import com.cropplanner.service.ScheduleService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Thin HTTP layer over ScheduleService. Ownership checks, harvest-date
 * computation, and dashboard aggregation now live in the service —
 * this class just resolves the session user and maps requests/responses.
 */
@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;
    private final SessionUserResolver sessionUserResolver;

    @GetMapping
    public ResponseEntity<List<ScheduleResponse>> getSchedules(HttpServletRequest request) {
        User user = sessionUserResolver.requireCurrentUser(request);
        return ResponseEntity.ok(scheduleService.getForUser(user));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardStats> getDashboard(HttpServletRequest request) {
        User user = sessionUserResolver.requireCurrentUser(request);
        return ResponseEntity.ok(scheduleService.getDashboardStats(user));
    }

    @PostMapping
    public ResponseEntity<ApiResult> create(
            @Valid @RequestBody CreateScheduleRequest req,
            HttpServletRequest request) {

        User user = sessionUserResolver.requireCurrentUser(request);
        PlantingSchedule schedule = scheduleService.create(user, req);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResult.ok(
                        "Schedule created! Harvest: " + schedule.getExpectedHarvestDate(),
                        schedule.getExpectedHarvestDate().toString()));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResult> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStatusRequest req,
            HttpServletRequest request) {

        User user = sessionUserResolver.requireCurrentUser(request);
        scheduleService.updateStatus(user, id, req.getStatus());
        return ResponseEntity.ok(ApiResult.ok("Status updated to: " + req.getStatus()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResult> delete(@PathVariable Long id, HttpServletRequest request) {
        User user = sessionUserResolver.requireCurrentUser(request);
        scheduleService.delete(user, id);
        return ResponseEntity.ok(ApiResult.ok("Schedule deleted."));
    }
}
