package com.cropplanner.pesthealth;

import java.util.Map;

/**
 * A small, fixed knowledge base of common pests/diseases and general
 * treatment guidance. Deliberately static and conservative rather than
 * AI-generated per request — agronomic/treatment advice is the kind of
 * thing that should come from a vetted, reviewable source, not a
 * potentially-hallucinated one. Names are matched case-insensitively;
 * anything not in the list gets a generic "consult an advisor" response
 * rather than guessed advice.
 */
final class TreatmentKnowledgeBase {

    private TreatmentKnowledgeBase() {}

    private static final Map<String, String> RECOMMENDATIONS = Map.ofEntries(
        Map.entry("aphids", "Spray with insecticidal soap or neem oil, focusing on the undersides of leaves. " +
                "Encourage natural predators like ladybirds. Isolate heavily infested plants if possible."),
        Map.entry("whitefly", "Use yellow sticky traps to monitor numbers, and apply insecticidal soap or neem oil " +
                "early in the morning. Remove and destroy heavily infested leaves."),
        Map.entry("cutworms", "Hand-pick at dusk when active, or place cardboard collars around seedling stems. " +
                "Tilling soil before planting helps expose larvae to predators."),
        Map.entry("bollworm", "Monitor with pheromone traps. Remove and destroy affected fruit/bolls promptly to " +
                "prevent spread. Rotate crops to break the pest's life cycle."),
        Map.entry("late blight", "Remove and destroy affected leaves immediately. Avoid overhead watering — water at " +
                "the base instead. Apply a copper-based fungicide and improve airflow between plants."),
        Map.entry("early blight", "Remove lower, older leaves showing symptoms. Mulch around plants to reduce soil " +
                "splash, and rotate crops yearly to prevent the fungus building up in the soil."),
        Map.entry("powdery mildew", "Improve air circulation by spacing plants further apart. Apply a sulfur-based " +
                "or potassium bicarbonate fungicide. Avoid overhead watering, especially in the evening."),
        Map.entry("downy mildew", "Remove and destroy infected leaves. Water early in the day so foliage dries " +
                "before nightfall. Apply a copper-based fungicide if symptoms are spreading."),
        Map.entry("root rot", "Improve drainage immediately — root rot is almost always caused by waterlogged " +
                "soil. Avoid replanting in the same spot next season."),
        Map.entry("aphid honeydew / sooty mould", "Treat the underlying aphid infestation first (see Aphids); the " +
                "sooty mould will clear once the aphids are controlled."),
        Map.entry("armyworm", "Monitor with pheromone traps and act quickly once detected — armyworms can defoliate " +
                "a field within days. Hand-remove where infestations are small; consider a targeted biopesticide for larger outbreaks.")
    );

    static String lookup(String issueName) {
        if (issueName == null) return genericAdvice();
        String key = issueName.trim().toLowerCase();
        return RECOMMENDATIONS.getOrDefault(key, genericAdvice());
    }

    private static String genericAdvice() {
        return "No specific guidance is available for this issue yet. Isolate affected plants where possible, " +
               "monitor closely, and consider consulting a local agricultural advisor for an accurate diagnosis.";
    }
}
