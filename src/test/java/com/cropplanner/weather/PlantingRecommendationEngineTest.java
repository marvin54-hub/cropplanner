package com.cropplanner.weather;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PlantingRecommendationEngineTest {

    private PlantingRecommendationEngine engine;

    @BeforeEach
    void setUp() {
        engine = new PlantingRecommendationEngine();
    }

    private DailyForecast clearDay() {
        return DailyForecast.builder()
                .date("2024-10-01").maxTempC(25).minTempC(15)
                .rainfallMm(0).precipitationProbabilityPct(5)
                .weatherCode(0).conditionDescription("Clear sky").build();
    }

    @Test
    @DisplayName("Clear forecast for 3 days → GOOD_TO_PLANT")
    void clearForecast_goodToPlant() {
        List<DailyForecast> forecast = List.of(clearDay(), clearDay(), clearDay());
        PlantingRecommendation rec = engine.recommend(forecast);
        assertThat(rec.getVerdict()).isEqualTo(PlantingRecommendation.Verdict.GOOD_TO_PLANT);
    }

    @Test
    @DisplayName("Thunderstorm day → DELAY_PLANTING")
    void thunderstormDay_delayPlanting() {
        DailyForecast stormDay = DailyForecast.builder()
                .date("2024-10-01").maxTempC(28).minTempC(18)
                .rainfallMm(15).precipitationProbabilityPct(80)
                .weatherCode(95).conditionDescription("Thunderstorm").build();
        PlantingRecommendation rec = engine.recommend(List.of(stormDay, clearDay(), clearDay()));
        assertThat(rec.getVerdict()).isEqualTo(PlantingRecommendation.Verdict.DELAY_PLANTING);
        assertThat(rec.getReasons()).anyMatch(r -> r.contains("Thunderstorm"));
    }

    @Test
    @DisplayName("Heavy rainfall day (20mm+) → DELAY_PLANTING")
    void heavyRain_delayPlanting() {
        DailyForecast rainyDay = clearDay();
        rainyDay.setRainfallMm(25.0);
        PlantingRecommendation rec = engine.recommend(List.of(rainyDay, clearDay(), clearDay()));
        assertThat(rec.getVerdict()).isEqualTo(PlantingRecommendation.Verdict.DELAY_PLANTING);
        assertThat(rec.getReasons()).anyMatch(r -> r.contains("Heavy rainfall"));
    }

    @Test
    @DisplayName("Near-frost temperature → DELAY_PLANTING")
    void frostRisk_delayPlanting() {
        DailyForecast frostyDay = clearDay();
        frostyDay.setMinTempC(1.0);
        PlantingRecommendation rec = engine.recommend(List.of(frostyDay, clearDay(), clearDay()));
        assertThat(rec.getVerdict()).isEqualTo(PlantingRecommendation.Verdict.DELAY_PLANTING);
    }

    @Test
    @DisplayName("High heat only (35°C+) → PLANT_WITH_CAUTION")
    void highHeatOnly_plantWithCaution() {
        DailyForecast hotDay = clearDay();
        hotDay.setMaxTempC(37.0);
        PlantingRecommendation rec = engine.recommend(List.of(hotDay, clearDay(), clearDay()));
        assertThat(rec.getVerdict()).isEqualTo(PlantingRecommendation.Verdict.PLANT_WITH_CAUTION);
        assertThat(rec.getReasons()).anyMatch(r -> r.contains("35°C+"));
    }
}
