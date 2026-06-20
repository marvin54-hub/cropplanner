package com.cropplanner.farmmap;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Calculates the area of a polygon defined by lat/lon coordinate pairs,
 * using the Shoelace (Gauss's area) formula projected onto a flat earth.
 *
 * Limitations:
 *  - Accurate to roughly ±2% for small plots (< ~5 km²) at latitudes
 *    below 60°. Sufficient for a small-scale farm plot context.
 *  - Not valid for very large areas or plots crossing the antimeridian.
 *  - Uses a single Earth radius (6371 km); does not account for ellipsoid.
 *
 * For South African latitudes (~26°S–34°S) and typical small farm sizes
 * (< 50 ha), this is well within acceptable accuracy.
 */
final class AreaCalculator {

    private static final double EARTH_RADIUS_M = 6_371_000.0;
    private static final double M2_PER_HECTARE = 10_000.0;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private AreaCalculator() {}

    /**
     * Parses a JSON string of [[lat,lon], ...] pairs and returns the
     * enclosed area in hectares. Returns null if the JSON is invalid or
     * the polygon has fewer than 3 points.
     */
    static Double calculateHectares(String coordinatesJson) {
        try {
            List<List<Double>> coords = MAPPER.readValue(
                    coordinatesJson,
                    new TypeReference<List<List<Double>>>() {}
            );
            if (coords == null || coords.size() < 3) return null;
            return shoelaceHectares(coords);
        } catch (Exception e) {
            return null;
        }
    }

    private static double shoelaceHectares(List<List<Double>> coords) {
        // Convert degrees to radians for the reference point
        double refLat = Math.toRadians(coords.get(0).get(0));
        double cosLat = Math.cos(refLat);

        // Project each point from (lat°, lon°) into metres relative to the first point
        double[] x = new double[coords.size()];
        double[] y = new double[coords.size()];
        for (int i = 0; i < coords.size(); i++) {
            double lat = Math.toRadians(coords.get(i).get(0));
            double lon = Math.toRadians(coords.get(i).get(1));
            double refLon = Math.toRadians(coords.get(0).get(1));
            x[i] = (lon - refLon) * EARTH_RADIUS_M * cosLat;
            y[i] = (lat - refLat) * EARTH_RADIUS_M;
        }

        // Shoelace formula
        double area = 0.0;
        int n = x.length;
        for (int i = 0; i < n; i++) {
            int j = (i + 1) % n;
            area += x[i] * y[j];
            area -= x[j] * y[i];
        }
        double areaM2 = Math.abs(area) / 2.0;
        return areaM2 / M2_PER_HECTARE;
    }
}
