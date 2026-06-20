package com.cropplanner.farmmap;

import com.cropplanner.model.PlantingSchedule;
import com.cropplanner.model.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * A named farm plot with a polygon boundary stored as a JSON string of
 * [latitude, longitude] coordinate pairs. Storing as JSON text avoids
 * a PostGIS/spatial-extension dependency while still enabling area
 * calculations in Java via the Shoelace formula.
 *
 * Example coordinates value:
 *   [[-26.1,28.0],[-26.1,28.01],[-26.11,28.01],[-26.11,28.0]]
 *
 * The optional schedule link records which crop is currently planted
 * in this plot, enabling the "crop placement" map view.
 */
@Entity
@Table(name = "farm_plots")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"user", "currentSchedule"})
public class FarmPlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 100)
    private String name;

    /**
     * JSON array of [lat, lon] pairs defining the plot boundary polygon.
     * e.g. "[[-26.1,28.0],[-26.1,28.01],[-26.11,28.01],[-26.11,28.0]]"
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String coordinatesJson;

    /** Pre-computed area in hectares, derived from coordinatesJson on save. */
    @Column(name = "area_hectares")
    private Double areaHectares;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_schedule_id")
    private PlantingSchedule currentSchedule;

    @Column(length = 255)
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
