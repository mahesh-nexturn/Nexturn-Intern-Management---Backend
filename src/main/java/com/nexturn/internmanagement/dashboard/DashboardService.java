package com.nexturn.internmanagement.dashboard;
import com.nexturn.internmanagement.dashboard.dto.AdminDashboardDto;
import com.nexturn.internmanagement.dashboard.dto.InternDashboardSummaryDto;
import com.nexturn.internmanagement.dashboard.dto.MentorDashboardDto;

import com.nexturn.internmanagement.announcement.AnnouncementService;
import com.nexturn.internmanagement.attendance.AttendanceRepository;
import com.nexturn.internmanagement.attendance.AttendanceStatus;
import com.nexturn.internmanagement.intern.InternRepository;
import com.nexturn.internmanagement.meeting.MeetingRepository;
import com.nexturn.internmanagement.mentor.MentorRepository;
import com.nexturn.internmanagement.task.TaskRepository;
import com.nexturn.internmanagement.task.TaskService;
import com.nexturn.internmanagement.task.dto.TaskStatsDto;
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

    /** Same shape as the admin dashboard but limited to the HR's own mentors and interns. */
    public AdminDashboardDto hrDashboard(Long hrId) {
        java.util.Set<Long> internIds = internRepository.findByHrId(hrId).stream()
                .map(i -> i.getId()).collect(java.util.stream.Collectors.toSet());
        long totalMentors = mentorRepository.findByHrId(hrId).size();
        long todayPresentCount = attendanceRepository.findAll().stream()
                .filter(a -> a.getIntern() != null && internIds.contains(a.getIntern().getId())
                        && LocalDate.now().equals(a.getAttendanceDate())
                        && a.getStatus() == AttendanceStatus.Present)
                .count();
        long upcomingMeetings = meetingRepository.findAll().stream()
                .filter(m -> m.getIntern() != null && internIds.contains(m.getIntern().getId())
                        && !m.getMeetingDate().isBefore(LocalDate.now())
                        && m.getStatus() == com.nexturn.internmanagement.meeting.MeetingStatus.Scheduled)
                .count();
        return new AdminDashboardDto(
                internIds.size(),
                totalMentors,
                internIds.size() + totalMentors,
                taskService.stats(internIds::contains),
                trainingService.stats(internIds::contains),
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
