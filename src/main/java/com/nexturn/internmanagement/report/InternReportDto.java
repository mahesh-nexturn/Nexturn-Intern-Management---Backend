package com.nexturn.internmanagement.report;

public record InternReportDto(
        Long internId,
        String internName,
        String department,
        Long mentorId,
        String mentorName,
        long totalTasks,
        long completedTasks,
        double taskCompletionPct,
        long presentDays,
        long totalAttendanceDays,
        double attendancePct,
        long totalTrainings,
        long completedTrainings,
        double trainingCompletionPct,
        Double averageEvaluationScore,
        String ppoStatus) {
}
