package com.nexturn.internmanagement.evaluation.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record EvaluationRequestDto(
        @NotNull Long internId,
        Long mentorId,
        Long taskId,
        @Min(1) @Max(5) short requirementUnderstanding,
        @Min(1) @Max(5) short technicalKnowledge,
        @Min(1) @Max(5) short workQuality,
        @Min(1) @Max(5) short taskCompletion,
        @Min(1) @Max(5) short taskDemonstration,
        String feedback) {
}

