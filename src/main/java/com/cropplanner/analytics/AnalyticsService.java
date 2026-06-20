package com.cropplanner.analytics;

import com.cropplanner.analytics.AnalyticsReport.*;
import com.cropplanner.expense.ExpenseCategory;
import com.cropplanner.expense.ExpenseRepository;
import com.cropplanner.model.PlantingSchedule;
import com.cropplanner.model.User;
import com.cropplanner.repository.ScheduleRepository;
import com.cropplanner.yieldprediction.YieldRecord;
import com.cropplanner.yieldprediction.YieldRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Aggregates data from schedules, expenses, and yield records into a single
 * analytics payload for the dashboard. All reads, no writes.
 *
 * Profit analysis uses a flat 5 ZAR/kg placeholder for estimated revenue —
 * a proper per-crop market price table is the obvious next improvement.
 */
@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private static final double PLACEHOLDER_PRICE_ZAR_PER_KG = 5.0;

    private final ScheduleRepository scheduleRepository;
    private final ExpenseRepository expenseRepository;
    private final YieldRecordRepository yieldRecordRepository;

    @Cacheable(value = "analyticsReport", key = "#user.id")
    public AnalyticsReport buildReport(User user) {
        List<PlantingSchedule> schedules = scheduleRepository.findByUserAndDeletedAtIsNullOrderByPlantingDateDesc(user);
        List<YieldRecord> yieldRecords = yieldRecordRepository.findBySchedule_User(user);
        List<ExpenseRepository.CategoryTotal> expTotals = expenseRepository.sumByCategory(user, null, null);

        return AnalyticsReport.builder()
                .cropPerformance(buildCropPerformance(schedules))
                .yieldStats(buildYieldStats(yieldRecords))
                .seasonalBreakdown(buildSeasonalBreakdown(schedules))
                .profitAnalysis(buildProfitAnalysis(expTotals, yieldRecords))
                .build();
    }

    private CropPerformance buildCropPerformance(List<PlantingSchedule> schedules) {
        Map<String, Long> byStatus = schedules.stream()
                .collect(Collectors.groupingBy(PlantingSchedule::getStatus, Collectors.counting()));

        Map<String, Long> byCrop = schedules.stream()
                .collect(Collectors.groupingBy(s -> s.getCrop().getName(), Collectors.counting()));

        List<CropCount> mostPlanted = byCrop.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .map(e -> new CropCount(e.getKey(), e.getValue()))
                .toList();

        long harvested = byStatus.getOrDefault("Harvested", 0L);
        long failed = byStatus.getOrDefault("Failed", 0L);
        long eligible = schedules.size() - failed;
        double successRate = eligible > 0 ? (harvested * 100.0 / eligible) : 0.0;

        return CropPerformance.builder()
                .mostPlanted(mostPlanted)
                .harvestSuccessRatePct(Math.round(successRate * 10.0) / 10.0)
                .schedulesByStatus(byStatus)
                .build();
    }

    private YieldStats buildYieldStats(List<YieldRecord> records) {
        int withActual = (int) records.stream().filter(r -> r.getActualYieldKg() != null).count();

        OptionalDouble avgVariance = records.stream()
                .filter(r -> r.getActualYieldKg() != null && r.getPredictedYieldKg() > 0)
                .mapToDouble(r -> ((r.getActualYieldKg() - r.getPredictedYieldKg()) / r.getPredictedYieldKg()) * 100.0)
                .average();

        double totalActual = records.stream()
                .filter(r -> r.getActualYieldKg() != null)
                .mapToDouble(YieldRecord::getActualYieldKg)
                .sum();

        double totalPredicted = records.stream()
                .mapToDouble(YieldRecord::getPredictedYieldKg)
                .sum();

        return YieldStats.builder()
                .totalPredictions(records.size())
                .predictionsWithActual(withActual)
                .avgVariancePct(avgVariance.isPresent()
                        ? Math.round(avgVariance.getAsDouble() * 10.0) / 10.0
                        : null)
                .totalActualYieldKg(totalActual)
                .totalPredictedYieldKg(totalPredicted)
                .build();
    }

    private SeasonalBreakdown buildSeasonalBreakdown(List<PlantingSchedule> schedules) {
        long summerCount = schedules.stream()
                .filter(s -> "Summer".equalsIgnoreCase(s.getCrop().getSeason())).count();
        long winterCount = schedules.size() - summerCount;

        long summerHarvested = schedules.stream()
                .filter(s -> "Summer".equalsIgnoreCase(s.getCrop().getSeason())
                        && "Harvested".equals(s.getStatus())).count();
        long winterHarvested = schedules.stream()
                .filter(s -> "Winter".equalsIgnoreCase(s.getCrop().getSeason())
                        && "Harvested".equals(s.getStatus())).count();

        return SeasonalBreakdown.builder()
                .summerScheduleCount(summerCount)
                .winterScheduleCount(winterCount)
                .summerHarvestedCount(summerHarvested)
                .winterHarvestedCount(winterHarvested)
                .build();
    }

    private ProfitAnalysis buildProfitAnalysis(
            List<ExpenseRepository.CategoryTotal> expTotals,
            List<YieldRecord> yieldRecords) {

        Map<ExpenseCategory, Double> byCategory = new EnumMap<>(ExpenseCategory.class);
        for (ExpenseCategory c : ExpenseCategory.values()) byCategory.put(c, 0.0);
        double totalExpenses = 0.0;
        for (ExpenseRepository.CategoryTotal t : expTotals) {
            byCategory.put(t.getCategory(), t.getTotal());
            totalExpenses += t.getTotal();
        }

        double totalActualKg = yieldRecords.stream()
                .filter(r -> r.getActualYieldKg() != null)
                .mapToDouble(YieldRecord::getActualYieldKg)
                .sum();

        double estimatedRevenue = totalActualKg * PLACEHOLDER_PRICE_ZAR_PER_KG;
        double estimatedProfit = estimatedRevenue - totalExpenses;

        return ProfitAnalysis.builder()
                .totalExpenses(totalExpenses)
                .expensesByCategory(byCategory)
                .estimatedRevenueZar(estimatedRevenue)
                .estimatedProfitZar(estimatedProfit)
                .build();
    }
}
