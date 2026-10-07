package com.nexturn.internmanagement.ppo;

import jakarta.validation.constraints.NotNull;

public record PpoRequestDto(
        @NotNull Long internId,
        Long mentorId,
        double attendancePct,
        double trainingCompletionPct,
        double technicalScore,
        double communicationScore,
        double overallScore,
        String mentorRecommendation,
        String hrRecommendation,
        String status) {
}
