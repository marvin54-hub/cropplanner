package com.cropplanner.weather;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Current-moment weather snapshot, server-side equivalent of what the dashboard widget shows. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CurrentWeather {
    private double temperatureC;
    private double feelsLikeC;
    private int humidityPct;
    private double windSpeedKmh;
    private int weatherCode;
    private String conditionDescription;
}
