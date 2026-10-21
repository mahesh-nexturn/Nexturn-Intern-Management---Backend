package com.nexturn.internmanagement.evaluation.dto;

public record EvaluationStatsDto(double avgRequirementUnderstanding, double avgTechnicalKnowledge,
        double avgWorkQuality, double avgTaskCompletion, double avgTaskDemonstration, double avgOverall,
        long totalEvaluations) {
}

