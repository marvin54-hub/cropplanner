package com.cropplanner.weather;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

/**
 * Mirrors the raw JSON shape returned by api.open-meteo.com/v1/forecast.
 * Field names match Open-Meteo's response keys exactly so Jackson can bind
 * without extra @JsonProperty annotations. This is intentionally separate
 * from our own DailyForecast/CurrentWeather DTOs — those are the stable,
 * farmer-friendly shape we expose; this is the volatile, third-party shape
 * we immediately translate away from.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenMeteoResponse {

    private double latitude;
    private double longitude;

    private CurrentBlock current;
    private DailyBlock daily;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CurrentBlock {
        private double temperature_2m;
        private double apparent_temperature;
        private int relative_humidity_2m;
        private double wind_speed_10m;
        private int weather_code;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DailyBlock {
        private List<String> time;
        private List<Double> temperature_2m_max;
        private List<Double> temperature_2m_min;
        private List<Double> precipitation_sum;
        private List<Integer> precipitation_probability_max;
        private List<Integer> weather_code;
    }
}
