package com.nexturn.internmanagement.dashboard.dto;

public record MentorDashboardDto(
        long internCount,
        long assignedTasks,
        long completedTasks,
        long upcomingMeetings) {
}
