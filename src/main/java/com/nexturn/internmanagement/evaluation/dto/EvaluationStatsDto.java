package com.nexturn.internmanagement.evaluation.dto;

public record EvaluationStatsDto(double avgTechnical, double avgCommunication, double avgProblemSolving,
        double avgOverall, long totalEvaluations) {
}
