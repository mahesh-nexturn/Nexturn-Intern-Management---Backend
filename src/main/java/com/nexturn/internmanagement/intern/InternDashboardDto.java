package com.nexturn.internmanagement.intern;

import com.nexturn.internmanagement.meeting.MeetingResponseDto;
import com.nexturn.internmanagement.task.TaskResponseDto;
import java.util.List;

public record InternDashboardDto(
        InternResponseDto intern,
        long pendingTasks,
        long inProgressTasks,
        long completedTasks,
        List<TaskResponseDto> recentTasks,
        List<MeetingResponseDto> upcomingMeetings) {
}
