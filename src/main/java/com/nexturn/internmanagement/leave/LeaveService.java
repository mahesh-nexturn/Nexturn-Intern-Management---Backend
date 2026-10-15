package com.nexturn.internmanagement.leave;

import com.nexturn.internmanagement.attendance.Attendance;
import com.nexturn.internmanagement.attendance.AttendanceRepository;
import com.nexturn.internmanagement.attendance.AttendanceStatus;
import com.nexturn.internmanagement.common.ResourceNotFoundException;
import com.nexturn.internmanagement.intern.Intern;
import com.nexturn.internmanagement.intern.InternRepository;
import com.nexturn.internmanagement.leave.dto.LeaveRequestDto;
import com.nexturn.internmanagement.leave.dto.LeaveResponseDto;
import com.nexturn.internmanagement.notification.NotificationRecipient;
import com.nexturn.internmanagement.notification.NotificationService;
import com.nexturn.internmanagement.user.User;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LeaveService {

    private final LeaveRequestRepository leaveRepository;
    private final InternRepository internRepository;
    private final AttendanceRepository attendanceRepository;
    private final NotificationService notificationService;

    public List<LeaveResponseDto> findForIntern(Long internId) {
        return leaveRepository.findByInternIdOrderByCreatedAtDesc(internId).stream()
                .map(LeaveResponseDto::from).toList();
    }

    public List<LeaveResponseDto> findAll(Predicate<Long> internScope) {
        return leaveRepository.findAllByOrderByCreatedAtDesc().stream()
                .filter(l -> internScope.test(l.getIntern().getId()))
                .map(LeaveResponseDto::from).toList();
    }

    @Transactional
    public LeaveResponseDto apply(Long internId, LeaveRequestDto dto) {
        if (dto.toDate().isBefore(dto.fromDate())) {
            throw new IllegalArgumentException("End date cannot be before start date");
        }
        Intern intern = internRepository.findById(internId)
                .orElseThrow(() -> new ResourceNotFoundException("Intern not found: " + internId));
        boolean overlaps = !leaveRepository
                .findByInternIdAndStatusInAndFromDateLessThanEqualAndToDateGreaterThanEqual(internId,
                        List.of(LeaveStatus.Pending, LeaveStatus.Approved), dto.toDate(), dto.fromDate())
                .isEmpty();
        if (overlaps) {
            throw new IllegalArgumentException("You already have a leave request for these dates");
        }

        LeaveRequest leave = leaveRepository.save(LeaveRequest.builder()
                .intern(intern)
                .hr(intern.getHr())
                .leaveType(dto.leaveType())
                .fromDate(dto.fromDate())
                .toDate(dto.toDate())
                .teamEmail(dto.teamEmail())
                .reason(dto.reason())
                .status(LeaveStatus.Pending)
                .createdAt(Instant.now())
                .build());

        if (intern.getUser() != null) {
            notificationService.notify(intern.getUser(), intern.getHr(), NotificationRecipient.HR,
                    "Leave request from " + intern.getName(),
                    intern.getName() + " applied for " + leave.getLeaveType() + " from " + leave.getFromDate()
                            + " to " + leave.getToDate() + ". Please review.", "/attendance");
        }
        return LeaveResponseDto.from(leave);
    }

    @Transactional
    public LeaveResponseDto decide(Long id, boolean approve, User decidedBy) {
        LeaveRequest leave = getEntity(id);
        if (leave.getStatus() != LeaveStatus.Pending) {
            throw new IllegalArgumentException("This request has already been " + leave.getStatus().name().toLowerCase());
        }
        leave.setStatus(approve ? LeaveStatus.Approved : LeaveStatus.Rejected);
        leave.setDecidedAt(Instant.now());
        leaveRepository.save(leave);

        if (approve) {
            markLeaveDays(leave);
        }

        Intern intern = leave.getIntern();
        if (intern.getUser() != null) {
            notificationService.notify(decidedBy, intern.getUser(), NotificationRecipient.Intern,
                    "Leave request " + leave.getStatus().name().toLowerCase(),
                    "Your " + leave.getLeaveType() + " from " + leave.getFromDate() + " to " + leave.getToDate()
                            + " was " + leave.getStatus().name().toLowerCase() + " by " + decidedBy.getName() + ".",
                    "/attendance");
        }
        return LeaveResponseDto.from(leave);
    }

    public LeaveRequest getEntity(Long id) {
        return leaveRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found: " + id));
    }

    /** Weekdays in the range become Leave in attendance; days where the intern already worked stay untouched. */
    private void markLeaveDays(LeaveRequest leave) {
        Intern intern = leave.getIntern();
        for (LocalDate day = leave.getFromDate(); !day.isAfter(leave.getToDate()); day = day.plusDays(1)) {
            final LocalDate d = day;
            if (d.getDayOfWeek() == DayOfWeek.SATURDAY || d.getDayOfWeek() == DayOfWeek.SUNDAY) {
                continue;
            }
            Attendance attendance = attendanceRepository.findByInternIdAndAttendanceDate(intern.getId(), d)
                    .orElseGet(() -> Attendance.builder().intern(intern).attendanceDate(d).build());
            if (attendance.getStatus() == AttendanceStatus.Present || attendance.getCheckInTime() != null) {
                continue;
            }
            attendance.setMentor(intern.getMentor());
            attendance.setStatus(AttendanceStatus.Leave);
            attendanceRepository.save(attendance);
        }
    }
}
