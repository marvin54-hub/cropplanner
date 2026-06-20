package com.cropplanner.pesthealth;

import com.cropplanner.pesthealth.HealthEnums.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class HealthDTOs {

    @Data
    public static class CreateHealthReportRequest {
        @NotNull(message = "Schedule ID is required")
        private Long scheduleId;

        @NotNull(message = "Issue type (PEST or DISEASE) is required")
        private IssueType issueType;

        @NotBlank(message = "A name for the pest/disease is required")
        private String name;

        @NotNull(message = "Severity is required")
        private Severity severity;

        private String description;
    }

    @Data
    public static class UpdateReportStatusRequest {
        @NotNull(message = "Status is required")
        private ReportStatus status;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HealthReportResponse {
        private Long id;
        private Long scheduleId;
        private String cropName;
        private IssueType issueType;
        private String name;
        private Severity severity;
        private ReportStatus status;
        private String description;
        private String treatmentRecommendation;
        private String createdAt;

        public static HealthReportResponse from(HealthReport r) {
            return HealthReportResponse.builder()
                    .id(r.getId())
                    .scheduleId(r.getSchedule().getId())
                    .cropName(r.getSchedule().getCrop().getName())
                    .issueType(r.getIssueType())
                    .name(r.getName())
                    .severity(r.getSeverity())
                    .status(r.getStatus())
                    .description(r.getDescription())
                    .treatmentRecommendation(r.getTreatmentRecommendation())
                    .createdAt(r.getCreatedAt() != null ? r.getCreatedAt().toString() : null)
                    .build();
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CropHealthSummary {
        private Long scheduleId;
        private String cropName;
        private CropHealthStatus status;
        private int openReportCount;
    }
}
