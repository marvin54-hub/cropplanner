package com.cropplanner.pesthealth;

import com.cropplanner.model.PlantingSchedule;
import com.cropplanner.model.User;
import com.cropplanner.pesthealth.HealthEnums.IssueType;
import com.cropplanner.pesthealth.HealthEnums.ReportStatus;
import com.cropplanner.pesthealth.HealthEnums.Severity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * A single pest or disease incident reported against a planting schedule.
 * A schedule can have multiple reports over its lifetime (e.g. aphids
 * early on, then blight later) — this is a log, not a single status field,
 * so history is preserved.
 */
@Entity
@Table(name = "health_reports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"user", "schedule"})
public class HealthReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    private PlantingSchedule schedule;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private IssueType issueType;

    /** Free-text name of the specific pest/disease, e.g. "Aphids", "Late blight". */
    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Severity severity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ReportStatus status = ReportStatus.REPORTED;

    @Column(columnDefinition = "TEXT")
    private String description;

    /** Recommended treatment, filled by the recommendation lookup when the report is created. */
    @Column(columnDefinition = "TEXT")
    private String treatmentRecommendation;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime resolvedAt;
}
