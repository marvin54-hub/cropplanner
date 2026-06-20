package com.cropplanner.inventory;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class InventoryDTOs {

    @Data
    public static class CreateInventoryItemRequest {
        @NotNull(message = "Item type is required")
        private InventoryItemType type;

        @NotBlank(message = "Item name is required")
        private String name;

        @NotNull(message = "Quantity is required")
        @DecimalMin(value = "0.0", message = "Quantity cannot be negative")
        private Double quantity;

        @NotBlank(message = "Unit is required (e.g. kg, litres, bags, units)")
        private String unit;

        private Double lowStockThreshold;
        private String notes;
    }

    @Data
    public static class AdjustQuantityRequest {
        @NotNull(message = "Delta is required (positive to add stock, negative to use stock)")
        private Double delta;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InventoryItemResponse {
        private Long id;
        private InventoryItemType type;
        private String name;
        private double quantity;
        private String unit;
        private Double lowStockThreshold;
        private boolean lowStock;
        private String notes;

        public static InventoryItemResponse from(InventoryItem item) {
            boolean low = item.getLowStockThreshold() != null && item.getQuantity() <= item.getLowStockThreshold();
            return InventoryItemResponse.builder()
                    .id(item.getId())
                    .type(item.getType())
                    .name(item.getName())
                    .quantity(item.getQuantity())
                    .unit(item.getUnit())
                    .lowStockThreshold(item.getLowStockThreshold())
                    .lowStock(low)
                    .notes(item.getNotes())
                    .build();
        }
    }
}
