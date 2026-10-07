package com.nexturn.internmanagement.ppo;

public record PpoStatsDto(
        long totalEvaluated,
        long eligible,
        long notEligible,
        long offered) {
}
