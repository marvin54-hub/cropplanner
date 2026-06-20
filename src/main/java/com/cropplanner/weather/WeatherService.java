package com.cropplanner.weather;

import com.cropplanner.exception.BusinessRuleException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Orchestrates the weather feature end to end: resolve a location to
 * coordinates if needed, fetch the forecast, translate it into our own
 * DTOs, and attach a planting recommendation.
 */
@Service
@RequiredArgsConstructor
public class WeatherService {

    private static final int DEFAULT_FORECAST_DAYS = 7;

    private final OpenMeteoClient openMeteoClient;
    private final PlantingRecommendationEngine recommendationEngine;

    /** Weather by explicit coordinates — used when the client has GPS (mirrors the existing dashboard widget). */
    public WeatherResponse getWeatherByCoordinates(double latitude, double longitude, String locationLabel) {
        OpenMeteoResponse raw = openMeteoClient.getForecast(latitude, longitude, DEFAULT_FORECAST_DAYS);
        return buildResponse(raw, locationLabel);
    }

    /** Weather by free-text place name — used for a farmer's saved location, or manual lookups. */
    public WeatherResponse getWeatherByLocationName(String locationName) {
        if (locationName == null || locationName.isBlank()) {
            throw new BusinessRuleException("A location name is required to look up weather.");
        }
        OpenMeteoGeocodingResponse.Result place = openMeteoClient.geocode(locationName);
        OpenMeteoResponse raw = openMeteoClient.getForecast(place.getLatitude(), place.getLongitude(), DEFAULT_FORECAST_DAYS);
        return buildResponse(raw, place.getName());
    }

    private WeatherResponse buildResponse(OpenMeteoResponse raw, String locationLabel) {
        if (raw == null || raw.getCurrent() == null || raw.getDaily() == null) {
            throw new BusinessRuleException("Weather service returned an incomplete response. Please try again.");
        }

        CurrentWeather current = toCurrentWeather(raw.getCurrent());
        List<DailyForecast> daily = toDailyForecasts(raw.getDaily());
        PlantingRecommendation recommendation = recommendationEngine.recommend(daily);

        return WeatherResponse.builder()
                .latitude(raw.getLatitude())
                .longitude(raw.getLongitude())
                .locationLabel(locationLabel)
                .current(current)
                .dailyForecast(daily)
                .recommendation(recommendation)
                .build();
    }

    private CurrentWeather toCurrentWeather(OpenMeteoResponse.CurrentBlock c) {
        return CurrentWeather.builder()
                .temperatureC(c.getTemperature_2m())
                .feelsLikeC(c.getApparent_temperature())
                .humidityPct(c.getRelative_humidity_2m())
                .windSpeedKmh(c.getWind_speed_10m())
                .weatherCode(c.getWeather_code())
                .conditionDescription(WeatherCodeTranslator.describe(c.getWeather_code()))
                .build();
    }

    private List<DailyForecast> toDailyForecasts(OpenMeteoResponse.DailyBlock d) {
        List<DailyForecast> result = new ArrayList<>();
        if (d.getTime() == null) return result;

        for (int i = 0; i < d.getTime().size(); i++) {
            int code = safeGetInt(d.getWeather_code(), i, 0);
            result.add(DailyForecast.builder()
                    .date(d.getTime().get(i))
                    .maxTempC(safeGetDouble(d.getTemperature_2m_max(), i, 0.0))
                    .minTempC(safeGetDouble(d.getTemperature_2m_min(), i, 0.0))
                    .rainfallMm(safeGetDouble(d.getPrecipitation_sum(), i, 0.0))
                    .precipitationProbabilityPct(safeGetInt(d.getPrecipitation_probability_max(), i, 0))
                    .weatherCode(code)
                    .conditionDescription(WeatherCodeTranslator.describe(code))
                    .build());
        }
        return result;
    }

    private double safeGetDouble(List<Double> list, int index, double fallback) {
        if (list == null || index >= list.size() || list.get(index) == null) return fallback;
        return list.get(index);
    }

    private int safeGetInt(List<Integer> list, int index, int fallback) {
        if (list == null || index >= list.size() || list.get(index) == null) return fallback;
        return list.get(index);
    }
}
