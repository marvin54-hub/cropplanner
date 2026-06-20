package com.cropplanner.pesthealth;

public class HealthEnums {

    public enum IssueType {
        PEST,
        DISEASE
    }

    public enum Severity {
        LOW,
        MEDIUM,
        HIGH
    }

    public enum ReportStatus {
        REPORTED,
        TREATING,
        RESOLVED
    }

    /** Derived overall health for a schedule, based on its open reports. */
    public enum CropHealthStatus {
        HEALTHY,       // no open reports
        UNDER_TREATMENT, // open reports, none high severity
        AT_RISK        // at least one open HIGH severity report
    }
}
