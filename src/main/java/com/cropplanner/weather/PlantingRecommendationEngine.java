package com.cropplanner.weather;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Pure logic, no I/O: given a forecast, decide whether conditions favour
 * planting. Kept separate from WeatherService so the recommendation rules
 * can be unit-tested against fixed forecast data without mocking HTTP calls.
 *
 * Rules are intentionally simple and transparent (each one maps to a
 * specific, explainable reason) rather than a black-box score, since
 * farmers need to trust and understand the "why" behind the verdict.
 */
@Component
class PlantingRecommendationEngine {

    private static final double HEAVY_RAIN_THRESHOLD_MM = 20.0;
    private static final double DROUGHT_RISK_MAX_TEMP_C = 35.0;
    private static final double FROST_RISK_MIN_TEMP_C = 2.0;
    private static final int LOOKAHEAD_DAYS_FOR_VERDICT = 3;

    PlantingRecommendation recommend(List<DailyForecast> forecast) {
        List<String> reasons = new ArrayList<>();
        boolean hasStorm = false;
        boolean hasHeavyRain = false;
        boolean hasFrostRisk = false;
        boolean hasDroughtRisk = false;

        int days = Math.min(LOOKAHEAD_DAYS_FOR_VERDICT, forecast.size());
        for (int i = 0; i < days; i++) {
            DailyForecast day = forecast.get(i);

            if (WeatherCodeTranslator.isStorm(day.getWeatherCode())) {
                hasStorm = true;
            }
            if (day.getRainfallMm() >= HEAVY_RAIN_THRESHOLD_MM) {
                hasHeavyRain = true;
            }
            if (day.getMinTempC() <= FROST_RISK_MIN_TEMP_C) {
                hasFrostRisk = true;
            }
            if (day.getMaxTempC() >= DROUGHT_RISK_MAX_TEMP_C) {
                hasDroughtRisk = true;
            }
        }

        if (hasStorm) {
            reasons.add("Thunderstorms are forecast in the next " + days + " days — risk of seedling damage and soil erosion.");
        }
        if (hasHeavyRain) {
            reasons.add("Heavy rainfall (20mm+) expected — newly planted seeds may be washed out or waterlogged.");
        }
        if (hasFrostRisk) {
            reasons.add("Near-freezing temperatures expected overnight — frost risk for young seedlings.");
        }
        if (hasDroughtRisk) {
            reasons.add("High daytime temperatures (35°C+) expected — increased irrigation may be needed if planting now.");
        }

        PlantingRecommendation.Verdict verdict;
        String summary;

        if (hasStorm || hasHeavyRain || hasFrostRisk) {
            verdict = PlantingRecommendation.Verdict.DELAY_PLANTING;
            summary = "Conditions in the next few days carry meaningful risk — consider delaying planting.";
        } else if (hasDroughtRisk) {
            verdict = PlantingRecommendation.Verdict.PLANT_WITH_CAUTION;
            summary = "Planting is possible, but plan for extra irrigation due to high temperatures.";
        } else {
            verdict = PlantingRecommendation.Verdict.GOOD_TO_PLANT;
            summary = "Forecast looks favourable for planting in the next " + days + " days.";
            reasons.add("No significant rainfall, frost, or heat extremes expected.");
        }

        return PlantingRecommendation.builder()
                .verdict(verdict)
                .summary(summary)
                .reasons(reasons)
                .build();
    }
}
