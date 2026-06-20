package com.cropplanner.yieldprediction;

import com.cropplanner.model.PlantingSchedule;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Tracks the yield prediction for a single planting schedule: the farm area
 * the farmer entered, the predicted yield computed from the crop's average
 * yield-per-hectare, and (filled in later, once harvested) the actual yield
 * the farmer reports — enabling predicted-vs-actual comparison over time.
 *
 * One-to-one with PlantingSchedule: a schedule gets at most one yield
 * record, created when the farmer first asks for a prediction.
 */
@Entity
@Table(name = "yield_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class YieldRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false, unique = true)
    private PlantingSchedule schedule;

    @Column(name = "farm_area_hectares", nullable = false)
    private double farmAreaHectares;

    @Column(name = "predicted_yield_kg", nullable = false)
    private double predictedYieldKg;

    /** Null until the farmer records an actual harvest quantity. */
    @Column(name = "actual_yield_kg")
    private Double actualYieldKg;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "actual_recorded_at")
    private LocalDateTime actualRecordedAt;
}
