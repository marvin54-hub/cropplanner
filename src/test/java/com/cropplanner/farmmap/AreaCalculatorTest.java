package com.cropplanner.farmmap;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AreaCalculatorTest {

    @Test
    @DisplayName("Roughly 1-hectare square near Johannesburg returns ~1.0 ha")
    void squarePlot_returnsApproximatelyOneHectare() {
        // ~100m × ~100m box at latitude -26° (Johannesburg area)
        // 0.001° latitude ≈ 111m, 0.001° longitude at -26° ≈ 99.7m
        String coords = "[[-26.0,28.0],[-26.001,28.0],[-26.001,28.001],[-26.0,28.001]]";
        Double area = AreaCalculator.calculateHectares(coords);
        assertThat(area).isNotNull();
        assertThat(area).isBetween(0.9, 1.2); // roughly 1 ha, tolerance for flat-earth approximation
    }

    @Test
    @DisplayName("Invalid JSON returns null rather than throwing")
    void invalidJson_returnsNull() {
        assertThat(AreaCalculator.calculateHectares("not json")).isNull();
    }

    @Test
    @DisplayName("Fewer than 3 points returns null")
    void twoPoints_returnsNull() {
        assertThat(AreaCalculator.calculateHectares("[[-26.0,28.0],[-26.001,28.0]]")).isNull();
    }

    @Test
    @DisplayName("Larger plot returns proportionally larger area")
    void largerPlot_returnsLargerArea() {
        // 10x the linear size = roughly 100x the area
        String small = "[[-26.0,28.0],[-26.001,28.0],[-26.001,28.001],[-26.0,28.001]]";
        String large = "[[-26.0,28.0],[-26.01,28.0],[-26.01,28.01],[-26.0,28.01]]";
        Double smallArea = AreaCalculator.calculateHectares(small);
        Double largeArea = AreaCalculator.calculateHectares(large);
        assertThat(largeArea).isGreaterThan(smallArea * 90);
    }
}
