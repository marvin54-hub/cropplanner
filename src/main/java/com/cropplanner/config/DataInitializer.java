package com.cropplanner.config;

import com.cropplanner.model.Crop;
import com.cropplanner.repository.CropRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final CropRepository cropRepository;

    @Override
    public void run(String... args) {
        if (cropRepository.count() == 0) {
            log.info("Seeding crop data...");
            // avgYieldKgPerHectare figures are small-scale-farmer-realistic averages
            // (lower than industrial/commercial-scale yields), used as a starting
            // point for Crop Yield Prediction. Farmers' actual results will vary
            // with soil, irrigation, and practices — these are estimates, not guarantees.
            cropRepository.saveAll(List.of(
                Crop.builder().name("Maize (Corn)")    .season("Summer").growthDurationDays(90) .avgYieldKgPerHectare(3500.0) .description("Staple crop, requires warm weather and moderate water.").build(),
                Crop.builder().name("Tomatoes")         .season("Summer").growthDurationDays(75) .avgYieldKgPerHectare(20000.0).description("Popular vegetable, requires consistent watering.").build(),
                Crop.builder().name("Beans (Dry)")      .season("Summer").growthDurationDays(85) .avgYieldKgPerHectare(1200.0) .description("High-protein legume, improves soil nitrogen.").build(),
                Crop.builder().name("Onions")           .season("Summer").growthDurationDays(120).avgYieldKgPerHectare(15000.0).description("Bulb vegetable widely used in cooking.").build(),
                Crop.builder().name("Butternut Squash") .season("Summer").growthDurationDays(110).avgYieldKgPerHectare(12000.0).description("Warm-season vine crop, high yield.").build(),
                Crop.builder().name("Groundnuts")       .season("Summer").growthDurationDays(130).avgYieldKgPerHectare(1000.0) .description("Oil-seed legume, drought tolerant.").build(),
                Crop.builder().name("Potatoes")         .season("Winter").growthDurationDays(100).avgYieldKgPerHectare(18000.0).description("Root vegetable, grows well in cool climates.").build(),
                Crop.builder().name("Spinach")          .season("Winter").growthDurationDays(45) .avgYieldKgPerHectare(8000.0) .description("Fast-growing leafy green, cool weather crop.").build(),
                Crop.builder().name("Carrots")          .season("Winter").growthDurationDays(70) .avgYieldKgPerHectare(20000.0).description("Root vegetable rich in beta-carotene.").build(),
                Crop.builder().name("Cabbage")          .season("Winter").growthDurationDays(80) .avgYieldKgPerHectare(25000.0).description("Leafy vegetable, thrives in cool weather.").build(),
                Crop.builder().name("Wheat")            .season("Winter").growthDurationDays(120).avgYieldKgPerHectare(2500.0) .description("Grows best in cooler climates, well-drained soil.").build()
            ));
            log.info("✅ {} crops seeded.", cropRepository.count());
        } else {
            log.info("✅ Crops already exist ({}).", cropRepository.count());
        }
    }
}
