package com.nexturn.internmanagement.dashboard.dto;

public record InternDashboardSummaryDto(
        long totalTasks,
        long completedTasks,
        long presentDays,
        long totalAttendanceDays,
        long upcomingMeetings) {
}
