package com.cropplanner.pesthealth;

import com.cropplanner.exception.ResourceNotFoundException;
import com.cropplanner.model.PlantingSchedule;
import com.cropplanner.model.User;
import com.cropplanner.pesthealth.HealthDTOs.*;
import com.cropplanner.pesthealth.HealthEnums.CropHealthStatus;
import com.cropplanner.pesthealth.HealthEnums.ReportStatus;
import com.cropplanner.pesthealth.HealthEnums.Severity;
import com.cropplanner.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class HealthReportService {

    private final HealthReportRepository healthReportRepository;
    private final ScheduleRepository scheduleRepository;

    @Transactional
    public HealthReportResponse create(User user, CreateHealthReportRequest req) {
        PlantingSchedule schedule = getOwnedSchedule(user, req.getScheduleId());

        HealthReport report = HealthReport.builder()
                .user(user)
                .schedule(schedule)
                .issueType(req.getIssueType())
                .name(req.getName())
                .severity(req.getSeverity())
                .description(req.getDescription())
                .treatmentRecommendation(TreatmentKnowledgeBase.lookup(req.getName()))
                .build();

        HealthReport saved = healthReportRepository.save(report);
        log.info("Health report created — user: {} schedule: {} issue: {} severity: {}",
                user.getEmail(), schedule.getId(), req.getName(), req.getSeverity());
        return HealthReportResponse.from(saved);
    }

    public List<HealthReportResponse> getForUser(User user) {
        return healthReportRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(HealthReportResponse::from)
                .toList();
    }

    public List<HealthReportResponse> getForSchedule(User user, Long scheduleId) {
        PlantingSchedule schedule = getOwnedSchedule(user, scheduleId);
        return healthReportRepository.findBySchedule(schedule)
                .stream()
                .map(HealthReportResponse::from)
                .toList();
    }

    @Transactional
    public HealthReportResponse updateStatus(User user, Long reportId, ReportStatus newStatus) {
        HealthReport report = getOwnedReport(user, reportId);
        report.setStatus(newStatus);
        if (newStatus == ReportStatus.RESOLVED) {
            report.setResolvedAt(LocalDateTime.now());
        }
        return HealthReportResponse.from(healthReportRepository.save(report));
    }

    /** Derives an overall crop health status for a schedule from its currently-open reports. */
    public CropHealthSummary getHealthStatus(User user, Long scheduleId) {
        PlantingSchedule schedule = getOwnedSchedule(user, scheduleId);
        List<HealthReport> openReports = healthReportRepository
                .findByScheduleAndStatusNot(schedule, ReportStatus.RESOLVED);

        CropHealthStatus status;
        if (openReports.isEmpty()) {
            status = CropHealthStatus.HEALTHY;
        } else if (openReports.stream().anyMatch(r -> r.getSeverity() == Severity.HIGH)) {
            status = CropHealthStatus.AT_RISK;
        } else {
            status = CropHealthStatus.UNDER_TREATMENT;
        }

        return CropHealthSummary.builder()
                .scheduleId(schedule.getId())
                .cropName(schedule.getCrop().getName())
                .status(status)
                .openReportCount(openReports.size())
                .build();
    }

    private PlantingSchedule getOwnedSchedule(User user, Long scheduleId) {
        PlantingSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule", scheduleId));
        if (!schedule.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Schedule", scheduleId);
        }
        return schedule;
    }

    private HealthReport getOwnedReport(User user, Long reportId) {
        HealthReport report = healthReportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Health report", reportId));
        if (!report.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Health report", reportId);
        }
        return report;
    }
}
