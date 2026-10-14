package com.nexturn.internmanagement.ppo.dto;

public record PpoStatsDto(
        long totalEvaluated,
        long eligible,
        long notEligible,
        long offered) {
}
