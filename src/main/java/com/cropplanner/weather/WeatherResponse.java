package com.cropplanner.weather;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Full payload returned by GET /api/weather. Combines current conditions,
 * a multi-day rainfall/temperature forecast, and a planting recommendation
 * derived from that forecast (and optionally a specific crop's needs).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeatherResponse {
    private double latitude;
    private double longitude;
    private String locationLabel;
    private CurrentWeather current;
    private List<DailyForecast> dailyForecast;
    private PlantingRecommendation recommendation;
}
