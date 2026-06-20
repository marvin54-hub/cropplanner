package com.cropplanner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class CropPlannerApplication {

    public static void main(String[] args) {
        SpringApplication.run(CropPlannerApplication.class, args);
        System.out.println("\n================================================");
        System.out.println("  🌾  CropPlanner is RUNNING!");
        System.out.println("  👉  Open: http://localhost:8080");
        System.out.println("================================================\n");
    }
}
