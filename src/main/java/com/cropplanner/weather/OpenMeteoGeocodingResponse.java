package com.cropplanner.weather;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

/**
 * Mirrors geocoding-api.open-meteo.com/v1/search, used to resolve a free-text
 * location name (e.g. a farmer's saved "location" field, or "Soweto") into
 * coordinates the forecast endpoint needs. Also free, no API key.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenMeteoGeocodingResponse {

    private List<Result> results;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Result {
        private String name;
        private double latitude;
        private double longitude;
        private String country;
        private String admin1;
    }
}
