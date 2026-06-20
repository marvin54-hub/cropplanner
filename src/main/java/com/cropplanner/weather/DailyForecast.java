package com.cropplanner.weather;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * One day's worth of forecast data, as returned within WeatherResponse.
 * Mirrors a slice of Open-Meteo's "daily" block, reshaped into farmer-friendly units.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyForecast {
    private String date;              // ISO yyyy-MM-dd
    private double maxTempC;
    private double minTempC;
    private double rainfallMm;
    private double precipitationProbabilityPct;
    private int weatherCode;
    private String conditionDescription;
}
