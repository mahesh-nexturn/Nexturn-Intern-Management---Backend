package com.nexturn.internmanagement.intern.dto;

import com.nexturn.internmanagement.meeting.dto.MeetingResponseDto;
import com.nexturn.internmanagement.task.dto.TaskResponseDto;
import java.util.List;

public record InternDashboardDto(
        InternResponseDto intern,
        long pendingTasks,
        long inProgressTasks,
        long completedTasks,
        List<TaskResponseDto> recentTasks,
        List<MeetingResponseDto> upcomingMeetings) {
}
