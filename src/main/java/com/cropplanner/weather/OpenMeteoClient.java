package com.cropplanner.weather;

import com.cropplanner.exception.BusinessRuleException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Thin wrapper around Open-Meteo's free, no-API-key-required forecast and
 * geocoding endpoints. Kept separate from WeatherService so the "talk to
 * a third party over HTTP" concern is isolated from "turn that data into
 * a farming recommendation" — if Open-Meteo is ever swapped for a paid
 * provider, only this class needs to change.
 */
@Component
@Slf4j
public class OpenMeteoClient {

    private static final String FORECAST_BASE = "https://api.open-meteo.com/v1/forecast";
    private static final String GEOCODE_BASE = "https://geocoding-api.open-meteo.com/v1/search";

    private final RestClient restClient = RestClient.create();

    /**
     * Fetches current conditions plus a 7-day daily forecast for the given
     * coordinates. forecastDays is capped to Open-Meteo's practical range.
     */
    public OpenMeteoResponse getForecast(double latitude, double longitude, int forecastDays) {
        int days = Math.max(1, Math.min(forecastDays, 16));

        String url = FORECAST_BASE
                + "?latitude=" + latitude
                + "&longitude=" + longitude
                + "&current=temperature_2m,relative_humidity_2m,apparent_temperature,weather_code,wind_speed_10m"
                + "&daily=temperature_2m_max,temperature_2m_min,precipitation_sum,precipitation_probability_max,weather_code"
                + "&forecast_days=" + days
                + "&timezone=auto";

        try {
            return restClient.get().uri(url).retrieve().body(OpenMeteoResponse.class);
        } catch (RestClientException ex) {
            log.warn("Open-Meteo forecast request failed for ({}, {}): {}", latitude, longitude, ex.getMessage());
            throw new BusinessRuleException("Weather service is currently unavailable. Please try again shortly.");
        }
    }

    /**
     * Resolves a free-text place name (e.g. "Polokwane") to coordinates.
     * Returns the first/best match, or throws if nothing is found.
     */
    public OpenMeteoGeocodingResponse.Result geocode(String placeName) {
        String url = UriComponentsBuilder.fromHttpUrl(GEOCODE_BASE)
                .queryParam("name", placeName.trim())
                .queryParam("count", 1)
                .toUriString();

        OpenMeteoGeocodingResponse res;
        try {
            res = restClient.get().uri(url).retrieve().body(OpenMeteoGeocodingResponse.class);
        } catch (RestClientException ex) {
            log.warn("Open-Meteo geocoding request failed for '{}': {}", placeName, ex.getMessage());
            throw new BusinessRuleException("Could not resolve that location. Please try a nearby town or city name.");
        }

        if (res == null || res.getResults() == null || res.getResults().isEmpty()) {
            throw new BusinessRuleException(
                    "Could not find a location matching \"" + placeName + "\". Try a nearby town or city name.");
        }
        return res.getResults().get(0);
    }
}
