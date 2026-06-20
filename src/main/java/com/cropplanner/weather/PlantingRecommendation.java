package com.cropplanner.weather;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * The output of the recommendation engine: a simple verdict plus the
 * reasons behind it, so the frontend can show "why" rather than just a flag.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlantingRecommendation {

    public enum Verdict {
        GOOD_TO_PLANT,
        PLANT_WITH_CAUTION,
        DELAY_PLANTING
    }

    private Verdict verdict;
    private String summary;
    private List<String> reasons;
}
