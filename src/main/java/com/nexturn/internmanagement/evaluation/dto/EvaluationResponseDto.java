package com.nexturn.internmanagement.evaluation.dto;

import com.nexturn.internmanagement.evaluation.Evaluation;

public record EvaluationResponseDto(Long id, Long internId, String internName, Long mentorId, Long taskId,
        Short requirementUnderstanding, Short technicalKnowledge, Short workQuality, Short taskCompletion,
        Short taskDemonstration, short overallRating, String feedback) {
    public static EvaluationResponseDto from(Evaluation e) {
        return new EvaluationResponseDto(e.getId(), e.getIntern().getId(), e.getIntern().getName(),
                e.getMentor() != null ? e.getMentor().getId() : null, e.getTaskId(),
                e.getRequirementUnderstanding(), e.getTechnicalKnowledge(), e.getWorkQuality(),
                e.getTaskCompletion(), e.getTaskDemonstration(), e.getOverallRating(), e.getFeedback());
    }
}

