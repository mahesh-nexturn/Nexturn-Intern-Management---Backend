package com.nexturn.internmanagement.dashboard;

public record InternDashboardSummaryDto(
        long totalTasks,
        long completedTasks,
        long presentDays,
        long totalAttendanceDays,
        long upcomingMeetings) {
}
