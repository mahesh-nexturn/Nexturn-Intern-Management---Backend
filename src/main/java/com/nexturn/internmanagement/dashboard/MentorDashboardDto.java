package com.nexturn.internmanagement.dashboard;

public record MentorDashboardDto(
        long internCount,
        long assignedTasks,
        long completedTasks,
        long upcomingMeetings) {
}
