package com.nexturn.internmanagement.dashboard;

import com.nexturn.internmanagement.announcement.AnnouncementService;
import com.nexturn.internmanagement.attendance.AttendanceRepository;
import com.nexturn.internmanagement.attendance.AttendanceStatus;
import com.nexturn.internmanagement.intern.InternRepository;
import com.nexturn.internmanagement.meeting.MeetingRepository;
import com.nexturn.internmanagement.mentor.MentorRepository;
import com.nexturn.internmanagement.task.TaskRepository;
import com.nexturn.internmanagement.task.TaskService;
import com.nexturn.internmanagement.task.TaskStatsDto;
import com.nexturn.internmanagement.task.TaskStatus;
import com.nexturn.internmanagement.training.TrainingService;
import com.nexturn.internmanagement.user.UserRepository;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UserRepository userRepository;
    private final InternRepository internRepository;
    private final MentorRepository mentorRepository;
    private final TaskRepository taskRepository;
    private final AttendanceRepository attendanceRepository;
    private final MeetingRepository meetingRepository;
    private final TaskService taskService;
    private final TrainingService trainingService;
    private final AnnouncementService announcementService;

    public AdminDashboardDto adminDashboard() {
        long totalInterns = internRepository.count();
        long totalMentors = mentorRepository.count();
        long totalUsers = userRepository.count();
        TaskStatsDto taskStats = taskService.stats();
        long todayPresentCount =
                attendanceRepository.countByAttendanceDateAndStatus(LocalDate.now(), AttendanceStatus.Present);
        long upcomingMeetings = meetingRepository
                .findByMeetingDateGreaterThanEqualAndStatusOrderByMeetingDateAscMeetingTimeAsc(
                        LocalDate.now(), com.nexturn.internmanagement.meeting.MeetingStatus.Scheduled)
                .size();
        return new AdminDashboardDto(
                totalInterns,
                totalMentors,
                totalUsers,
                taskStats,
                trainingService.stats(),
                announcementService.stats(),
                todayPresentCount,
                upcomingMeetings);
    }

    public MentorDashboardDto mentorDashboard(Long mentorId) {
        long internCount = internRepository.findByMentorId(mentorId).size();
        long assignedTasks = taskRepository.findByMentorId(mentorId).size();
        long completedTasks = taskRepository.findByMentorId(mentorId).stream()
                .filter(t -> t.getStatus() == TaskStatus.Completed)
                .count();
        long upcomingMeetings = meetingRepository.findByMentorId(mentorId).stream()
                .filter(m -> !m.getMeetingDate().isBefore(LocalDate.now())
                        && m.getStatus() == com.nexturn.internmanagement.meeting.MeetingStatus.Scheduled)
                .count();
        return new MentorDashboardDto(internCount, assignedTasks, completedTasks, upcomingMeetings);
    }

    public InternDashboardSummaryDto internDashboard(Long internId) {
        long totalTasks = taskRepository.findByInternId(internId).size();
        long completedTasks = taskRepository.countByInternIdAndStatus(internId, TaskStatus.Completed);
        long presentDays = attendanceRepository.countByInternIdAndStatus(internId, AttendanceStatus.Present);
        long totalAttendance = attendanceRepository.findByInternId(internId).size();
        long upcomingMeetings = meetingRepository.findByInternId(internId).stream()
                .filter(m -> !m.getMeetingDate().isBefore(LocalDate.now())
                        && m.getStatus() == com.nexturn.internmanagement.meeting.MeetingStatus.Scheduled)
                .count();
        return new InternDashboardSummaryDto(totalTasks, completedTasks, presentDays, totalAttendance, upcomingMeetings);
    }
}
