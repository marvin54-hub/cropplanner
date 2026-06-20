package com.cropplanner.farmmap;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class FarmMapDTOs {

    @Data
    public static class CreateFarmPlotRequest {
        @NotBlank(message = "Plot name is required")
        private String name;

        /**
         * JSON array of [latitude, longitude] pairs, minimum 3 points.
         * Example: "[[-26.1,28.0],[-26.1,28.01],[-26.11,28.01],[-26.11,28.0]]"
         */
        @NotBlank(message = "Coordinates are required")
        private String coordinatesJson;

        private Long currentScheduleId; // optional — assigns a crop to this plot
        private String notes;
    }

    @Data
    public static class AssignCropRequest {
        private Long scheduleId; // null to clear the crop assignment
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FarmPlotResponse {
        private Long id;
        private String name;
        private String coordinatesJson;
        private Double areaHectares;
        private Long currentScheduleId;
        private String currentCropName;
        private String notes;

        public static FarmPlotResponse from(FarmPlot p) {
            return FarmPlotResponse.builder()
                    .id(p.getId())
                    .name(p.getName())
                    .coordinatesJson(p.getCoordinatesJson())
                    .areaHectares(p.getAreaHectares())
                    .currentScheduleId(p.getCurrentSchedule() != null ? p.getCurrentSchedule().getId() : null)
                    .currentCropName(p.getCurrentSchedule() != null ? p.getCurrentSchedule().getCrop().getName() : null)
                    .notes(p.getNotes())
                    .build();
        }
    }
}
