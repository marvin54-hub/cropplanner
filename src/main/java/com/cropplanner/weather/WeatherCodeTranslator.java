package com.cropplanner.weather;

/**
 * Translates Open-Meteo's WMO weather codes into short descriptions.
 * Deliberately mirrors the thresholds used in app.js's weatherCodeToDesc()
 * so the backend and the existing frontend widget describe the same code
 * the same way.
 */
final class WeatherCodeTranslator {

    private WeatherCodeTranslator() {}

    static String describe(int code) {
        if (code == 0) return "Clear sky";
        if (code <= 2) return "Partly cloudy";
        if (code <= 3) return "Overcast";
        if (code <= 48) return "Foggy";
        if (code <= 57) return "Drizzle";
        if (code <= 67) return "Rain";
        if (code <= 77) return "Snow";
        if (code <= 82) return "Rain showers";
        if (code <= 86) return "Snow showers";
        if (code <= 99) return "Thunderstorm";
        return "Cloudy";
    }

    /** True for codes representing any form of active precipitation. */
    static boolean isPrecipitating(int code) {
        return (code >= 51 && code <= 99);
    }

    /** True for thunderstorm codes specifically — these warrant stronger warnings. */
    static boolean isStorm(int code) {
        return code >= 95 && code <= 99;
    }
}
