package com.cropplanner.inventory;

import com.cropplanner.model.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * A single inventory item a farmer tracks — a seed variety, a fertilizer
 * product, or a piece of equipment. Equipment quantities are typically 1
 * (or a small count); seeds/fertilizer use a unit (kg, litres, bags) so
 * the quantity is meaningful for stock-level tracking.
 */
@Entity
@Table(name = "inventory_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"user"})
public class InventoryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InventoryItemType type;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private double quantity;

    /** Free-text unit, e.g. "kg", "litres", "bags", "units". */
    @Column(nullable = false, length = 20)
    private String unit;

    /** When quantity drops to or below this, the item is flagged as low stock. Nullable = no threshold set. */
    @Column(name = "low_stock_threshold")
    private Double lowStockThreshold;

    @Column(length = 255)
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
