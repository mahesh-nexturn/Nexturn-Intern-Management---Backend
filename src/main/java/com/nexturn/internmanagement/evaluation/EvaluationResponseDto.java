package com.nexturn.internmanagement.evaluation;

public record EvaluationResponseDto(Long id, Long internId, String internName, Long mentorId,
        short technicalRating, short communicationRating, short problemSolvingRating, short overallRating,
        String feedback) {
    public static EvaluationResponseDto from(Evaluation e) {
        return new EvaluationResponseDto(e.getId(), e.getIntern().getId(), e.getIntern().getName(),
                e.getMentor() != null ? e.getMentor().getId() : null,
                e.getTechnicalRating(), e.getCommunicationRating(), e.getProblemSolvingRating(),
                e.getOverallRating(), e.getFeedback());
    }
}
