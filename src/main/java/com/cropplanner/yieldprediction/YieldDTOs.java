package com.cropplanner.yieldprediction;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class YieldDTOs {

    @Data
    public static class PredictYieldRequest {
        @NotNull(message = "Schedule ID is required")
        private Long scheduleId;

        @NotNull(message = "Farm area is required")
        @DecimalMin(value = "0.01", message = "Farm area must be greater than 0")
        private Double farmAreaHectares;
    }

    @Data
    public static class RecordActualYieldRequest {
        @NotNull(message = "Actual yield is required")
        @DecimalMin(value = "0.0", message = "Actual yield cannot be negative")
        private Double actualYieldKg;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class YieldRecordResponse {
        private Long id;
        private Long scheduleId;
        private String cropName;
        private double farmAreaHectares;
        private double predictedYieldKg;
        private Double actualYieldKg;
        private Double variancePct; // (actual - predicted) / predicted * 100, null until actual is recorded

        public static YieldRecordResponse from(YieldRecord r) {
            Double variance = null;
            if (r.getActualYieldKg() != null && r.getPredictedYieldKg() > 0) {
                variance = ((r.getActualYieldKg() - r.getPredictedYieldKg()) / r.getPredictedYieldKg()) * 100.0;
            }
            return YieldRecordResponse.builder()
                    .id(r.getId())
                    .scheduleId(r.getSchedule().getId())
                    .cropName(r.getSchedule().getCrop().getName())
                    .farmAreaHectares(r.getFarmAreaHectares())
                    .predictedYieldKg(r.getPredictedYieldKg())
                    .actualYieldKg(r.getActualYieldKg())
                    .variancePct(variance)
                    .build();
        }
    }
}
