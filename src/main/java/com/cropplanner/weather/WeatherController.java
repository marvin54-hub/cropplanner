package com.cropplanner.weather;

import com.cropplanner.model.User;
import com.cropplanner.security.SessionUserResolver;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Backend API for Weather Integration: current conditions, multi-day
 * rainfall/temperature forecast, and a planting recommendation derived
 * from that forecast.
 *
 * Three ways to ask for weather, covering the realistic cases:
 *  - by coordinates (the existing dashboard widget already has GPS coords)
 *  - by an arbitrary place name (manual lookup, e.g. checking a different town)
 *  - for "my farm" (uses the logged-in user's saved location, no params needed)
 */
@RestController
@RequestMapping("/api/weather")
@RequiredArgsConstructor
public class WeatherController {

    private final WeatherService weatherService;
    private final SessionUserResolver sessionUserResolver;

    @GetMapping
    public ResponseEntity<WeatherResponse> getWeatherByCoordinates(
            @RequestParam double lat,
            @RequestParam double lon,
            @RequestParam(required = false, defaultValue = "Your Location") String label) {

        return ResponseEntity.ok(weatherService.getWeatherByCoordinates(lat, lon, label));
    }

    @GetMapping("/by-location")
    public ResponseEntity<WeatherResponse> getWeatherByLocationName(@RequestParam String location) {
        return ResponseEntity.ok(weatherService.getWeatherByLocationName(location));
    }

    @GetMapping("/my-farm")
    public ResponseEntity<WeatherResponse> getWeatherForMyFarm(HttpServletRequest request) {
        User user = sessionUserResolver.requireCurrentUser(request);
        return ResponseEntity.ok(weatherService.getWeatherByLocationName(user.getLocation()));
    }
}
