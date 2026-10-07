package com.nexturn.internmanagement.evaluation;

public record EvaluationStatsDto(double avgTechnical, double avgCommunication, double avgProblemSolving,
        double avgOverall, long totalEvaluations) {
}
