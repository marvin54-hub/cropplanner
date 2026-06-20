package com.cropplanner.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "crops")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Crop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 50)
    private String season;

    @Column(name = "growth_duration_days", nullable = false)
    private int growthDurationDays;

    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Typical achievable yield for this crop under average conditions,
     * in kilograms per hectare. Used by Crop Yield Prediction to estimate
     * expected harvest quantity from a farm's planted area. Nullable so
     * existing seeded crops don't break; defaults are backfilled by
     * DataInitializer for the seeded set.
     */
    @Column(name = "avg_yield_kg_per_hectare")
    private Double avgYieldKgPerHectare;
}
