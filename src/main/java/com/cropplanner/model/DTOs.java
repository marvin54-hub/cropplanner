package com.cropplanner.model;

import jakarta.validation.constraints.*;
import lombok.Data;

public class DTOs {

    @Data
    public static class RegisterRequest {
        @NotBlank(message = "Full name is required")
        @Size(min = 2, max = 100)
        private String fullName;

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        private String email;

        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password must be at least 6 characters")
        private String password;

        private String phone;
        private String location;
    }

    @Data
    public static class LoginRequest {
        @NotBlank(message = "Email is required")
        @Email
        private String email;

        @NotBlank(message = "Password is required")
        private String password;
    }

    @Data
    public static class AuthResponse {
        private String status;
        private String message;
        private Long   userId;
        private String fullName;
        private String email;

        public static AuthResponse success(String msg, User user) {
            AuthResponse r = new AuthResponse();
            r.status   = "success";
            r.message  = msg;
            r.userId   = user.getId();
            r.fullName = user.getFullName();
            r.email    = user.getEmail();
            return r;
        }

        public static AuthResponse error(String msg) {
            AuthResponse r = new AuthResponse();
            r.status  = "error";
            r.message = msg;
            return r;
        }
    }

    @Data
    public static class CreateScheduleRequest {
        @NotNull(message = "Crop ID is required")
        private Long cropId;

        @NotNull(message = "Planting date is required")
        private String plantingDate;

        private String notes;
    }

    @Data
    public static class ScheduleResponse {
        private Long   id;
        private String cropName;
        private String cropSeason;
        private int    growthDays;
        private String plantingDate;
        private String expectedHarvestDate;
        private String notes;
        private String status;

        public static ScheduleResponse from(PlantingSchedule s) {
            ScheduleResponse r = new ScheduleResponse();
            r.id                  = s.getId();
            r.cropName            = s.getCrop().getName();
            r.cropSeason          = s.getCrop().getSeason();
            r.growthDays          = s.getCrop().getGrowthDurationDays();
            r.plantingDate        = s.getPlantingDate().toString();
            r.expectedHarvestDate = s.getExpectedHarvestDate().toString();
            r.notes               = s.getNotes();
            r.status              = s.getStatus();
            return r;
        }
    }

    @Data
    public static class UpdateStatusRequest {
        @NotNull
        private Long   id;
        @NotBlank
        private String status;
    }

    @Data
    public static class DashboardStats {
        private long   totalSchedules;
        private long   upcomingHarvests;
        private String currentSeason;
        private String farmerName;
        private String farmerLocation;
    }

    @Data
    public static class ApiResult {
        private String status;
        private String message;
        private Object data;

        public static ApiResult ok(String msg)              { return of("success", msg, null); }
        public static ApiResult ok(String msg, Object data) { return of("success", msg, data); }
        public static ApiResult err(String msg)             { return of("error",   msg, null); }

        private static ApiResult of(String s, String m, Object d) {
            ApiResult r = new ApiResult();
            r.status  = s;
            r.message = m;
            r.data    = d;
            return r;
        }
    }
}
