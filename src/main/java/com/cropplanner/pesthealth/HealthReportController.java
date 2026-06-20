package com.cropplanner.pesthealth;

import com.cropplanner.model.User;
import com.cropplanner.pesthealth.HealthDTOs.*;
import com.cropplanner.security.SessionUserResolver;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/health-reports")
@RequiredArgsConstructor
public class HealthReportController {

    private final HealthReportService healthReportService;
    private final SessionUserResolver sessionUserResolver;

    @PostMapping
    public ResponseEntity<HealthReportResponse> create(
            @Valid @RequestBody CreateHealthReportRequest req,
            HttpServletRequest request) {

        User user = sessionUserResolver.requireCurrentUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(healthReportService.create(user, req));
    }

    @GetMapping
    public ResponseEntity<List<HealthReportResponse>> getAll(
            @RequestParam(required = false) Long scheduleId,
            HttpServletRequest request) {

        User user = sessionUserResolver.requireCurrentUser(request);
        if (scheduleId != null) {
            return ResponseEntity.ok(healthReportService.getForSchedule(user, scheduleId));
        }
        return ResponseEntity.ok(healthReportService.getForUser(user));
    }

    @GetMapping("/schedule/{scheduleId}/status")
    public ResponseEntity<CropHealthSummary> getHealthStatus(
            @PathVariable Long scheduleId,
            HttpServletRequest request) {

        User user = sessionUserResolver.requireCurrentUser(request);
        return ResponseEntity.ok(healthReportService.getHealthStatus(user, scheduleId));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<HealthReportResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateReportStatusRequest req,
            HttpServletRequest request) {

        User user = sessionUserResolver.requireCurrentUser(request);
        return ResponseEntity.ok(healthReportService.updateStatus(user, id, req.getStatus()));
    }
}
