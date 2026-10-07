package com.nexturn.internmanagement.evaluation;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record EvaluationRequestDto(
        @NotNull Long internId,
        Long mentorId,
        @Min(0) @Max(10) short technicalRating,
        @Min(0) @Max(10) short communicationRating,
        @Min(0) @Max(10) short problemSolvingRating,
        @Min(0) @Max(10) short overallRating,
        String feedback) {
}
