package com.cropplanner.analytics;

import com.cropplanner.expense.ExpenseCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/** Full analytics payload — one call to /api/analytics returns everything the dashboard needs. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsReport {
    private CropPerformance cropPerformance;
    private YieldStats yieldStats;
    private SeasonalBreakdown seasonalBreakdown;
    private ProfitAnalysis profitAnalysis;

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class CropPerformance {
        /** Number of schedules per crop name, sorted by count desc. */
        private List<CropCount> mostPlanted;
        /** Harvest success rate: (Harvested / total non-Failed) * 100 */
        private double harvestSuccessRatePct;
        /** Counts by status. */
        private Map<String, Long> schedulesByStatus;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class CropCount {
        private String cropName;
        private long count;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class YieldStats {
        private int totalPredictions;
        private int predictionsWithActual;
        /** Average variance % between predicted and actual where both are recorded. */
        private Double avgVariancePct;
        /** Total actual yield recorded across all schedules (kg). */
        private double totalActualYieldKg;
        /** Total predicted yield across all schedules (kg). */
        private double totalPredictedYieldKg;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class SeasonalBreakdown {
        private long summerScheduleCount;
        private long winterScheduleCount;
        private long summerHarvestedCount;
        private long winterHarvestedCount;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ProfitAnalysis {
        private double totalExpenses;
        private Map<ExpenseCategory, Double> expensesByCategory;
        /** Estimated revenue: total actual yield kg * 5 ZAR/kg placeholder price.
         *  A price-per-crop feature would improve this; for now it's a rough proxy. */
        private double estimatedRevenueZar;
        private double estimatedProfitZar;
    }
}
