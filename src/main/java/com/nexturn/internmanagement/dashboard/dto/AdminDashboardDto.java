package com.nexturn.internmanagement.dashboard.dto;

import com.nexturn.internmanagement.announcement.dto.AnnouncementStatsDto;
import com.nexturn.internmanagement.task.dto.TaskStatsDto;
import com.nexturn.internmanagement.training.dto.TrainingStatsDto;

public record AdminDashboardDto(
        long totalInterns,
        long totalMentors,
        long totalUsers,
        TaskStatsDto taskStats,
        TrainingStatsDto trainingStats,
        AnnouncementStatsDto announcementStats,
        long todayPresentCount,
        long upcomingMeetingsCount) {
}
