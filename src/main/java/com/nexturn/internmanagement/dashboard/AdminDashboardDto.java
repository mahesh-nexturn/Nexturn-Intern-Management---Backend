package com.nexturn.internmanagement.dashboard;

import com.nexturn.internmanagement.announcement.AnnouncementStatsDto;
import com.nexturn.internmanagement.task.TaskStatsDto;
import com.nexturn.internmanagement.training.TrainingStatsDto;

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
