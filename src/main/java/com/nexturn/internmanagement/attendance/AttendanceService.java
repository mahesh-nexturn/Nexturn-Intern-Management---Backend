package com.nexturn.internmanagement.attendance;
import com.nexturn.internmanagement.attendance.dto.AttendanceRequestDto;
import com.nexturn.internmanagement.attendance.dto.AttendanceResponseDto;
import com.nexturn.internmanagement.attendance.dto.AttendanceStatsDto;
import com.nexturn.internmanagement.attendance.dto.TodayAttendanceDto;

import com.nexturn.internmanagement.common.ResourceNotFoundException;
import com.nexturn.internmanagement.intern.Intern;
import com.nexturn.internmanagement.intern.InternRepository;
import com.nexturn.internmanagement.mentor.Mentor;
import com.nexturn.internmanagement.mentor.MentorRepository;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final InternRepository internRepository;
    private final MentorRepository mentorRepository;

    public List<AttendanceResponseDto> findByIntern(Long internId) {
        return attendanceRepository.findByInternId(internId).stream().map(AttendanceResponseDto::from).toList();
    }

    public List<AttendanceResponseDto> findAll() {
        return attendanceRepository.findAll().stream().map(AttendanceResponseDto::from).toList();
    }

    public AttendanceResponseDto findById(Long id) {
        return AttendanceResponseDto.from(getEntity(id));
    }

    public Attendance getEntity(Long id) {
        return attendanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance record not found: " + id));
    }

    /** Upsert-by-(intern,date) semantics, matching the frontend's current behavior. */
    public AttendanceResponseDto upsert(AttendanceRequestDto dto) {
        Attendance attendance = attendanceRepository
                .findByInternIdAndAttendanceDate(dto.internId(), dto.attendanceDate())
                .orElseGet(Attendance::new);
        applyDto(attendance, dto);
        return AttendanceResponseDto.from(attendanceRepository.save(attendance));
    }

    public AttendanceResponseDto update(Long id, AttendanceRequestDto dto) {
        Attendance attendance = getEntity(id);
        applyDto(attendance, dto);
        return AttendanceResponseDto.from(attendanceRepository.save(attendance));
    }

    public void delete(Long id) {
        attendanceRepository.delete(getEntity(id));
    }

    public List<AttendanceResponseDto> calendar(Long internId, LocalDate from, LocalDate to) {
        return attendanceRepository.findByInternIdAndAttendanceDateBetween(internId, from, to).stream()
                .map(AttendanceResponseDto::from).toList();
    }

    public AttendanceStatsDto stats(Long internId) {
        Map<String, Long> counts = new LinkedHashMap<>();
        Arrays.stream(AttendanceStatus.values())
                .forEach(s -> counts.put(s.name(), attendanceRepository.countByInternIdAndStatus(internId, s)));
        return new AttendanceStatsDto(counts);
    }

    private static final Duration MAX_SESSION = Duration.ofHours(24);

    /** Hours that must be worked in a day for it to count as Present. */
    @Value("${app.attendance.required-hours:3}")
    private long requiredHours;

    private Duration required() {
        return Duration.ofHours(requiredHours);
    }

    /** Ends the open session at {@code end} and recomputes the day's status from total time worked. */
    private void closeSession(Attendance a, Instant end) {
        a.setWorkedSeconds(a.getWorkedSeconds() + Math.max(0, Duration.between(a.getCheckInTime(), end).getSeconds()));
        a.setCheckOutTime(end);
        a.setStatus(a.getWorkedSeconds() >= required().getSeconds() ? AttendanceStatus.Present : AttendanceStatus.Absent);
        attendanceRepository.save(a);
    }

    private void autoCheckOutIfExpired(Attendance a) {
        Instant limit = a.getCheckInTime().plus(MAX_SESSION);
        if (!limit.isAfter(Instant.now())) {
            closeSession(a, limit);
        }
    }

    /** Safety net: closes any session left open for 24 hours, even if the intern never returns. */
    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void autoCheckOutExpiredSessions() {
        attendanceRepository.findByCheckInTimeBeforeAndCheckOutTimeIsNull(Instant.now().minus(MAX_SESSION))
                .forEach(this::autoCheckOutIfExpired);
    }

    @Transactional
    public TodayAttendanceDto today(Long internId) {
        attendanceRepository.findFirstByInternIdAndCheckInTimeIsNotNullAndCheckOutTimeIsNull(internId)
                .ifPresent(this::autoCheckOutIfExpired);
        Attendance record = attendanceRepository
                .findFirstByInternIdAndCheckInTimeIsNotNullAndCheckOutTimeIsNull(internId)
                .or(() -> attendanceRepository.findByInternIdAndAttendanceDate(internId, LocalDate.now()))
                .orElse(null);
        return new TodayAttendanceDto(record == null ? null : AttendanceResponseDto.from(record), Instant.now(),
                required().getSeconds());
    }

    @Transactional
    public TodayAttendanceDto checkIn(Long internId) {
        Intern intern = internRepository.findById(internId)
                .orElseThrow(() -> new ResourceNotFoundException("Intern not found: " + internId));
        attendanceRepository.findFirstByInternIdAndCheckInTimeIsNotNullAndCheckOutTimeIsNull(internId)
                .ifPresent(this::autoCheckOutIfExpired);
        if (attendanceRepository.findFirstByInternIdAndCheckInTimeIsNotNullAndCheckOutTimeIsNull(internId).isPresent()) {
            throw new IllegalArgumentException("Already checked in");
        }
        Attendance attendance = attendanceRepository
                .findByInternIdAndAttendanceDate(internId, LocalDate.now())
                .orElseGet(Attendance::new);
        if (attendance.getStatus() == AttendanceStatus.Leave || attendance.getStatus() == AttendanceStatus.Holiday) {
            throw new IllegalArgumentException("Today is marked as " + attendance.getStatus());
        }
        if (attendance.getId() == null) {
            attendance.setIntern(intern);
            attendance.setMentor(intern.getMentor());
            attendance.setAttendanceDate(LocalDate.now());
            attendance.setStatus(AttendanceStatus.Absent);
        }
        attendance.setCheckInTime(Instant.now());
        attendance.setCheckOutTime(null);
        attendanceRepository.save(attendance);
        return today(internId);
    }

    @Transactional
    public TodayAttendanceDto checkOut(Long internId) {
        attendanceRepository.findFirstByInternIdAndCheckInTimeIsNotNullAndCheckOutTimeIsNull(internId)
                .ifPresent(this::autoCheckOutIfExpired);
        Attendance attendance = attendanceRepository
                .findFirstByInternIdAndCheckInTimeIsNotNullAndCheckOutTimeIsNull(internId)
                .orElseThrow(() -> new IllegalArgumentException("You are not checked in"));
        closeSession(attendance, Instant.now());
        return today(internId);
    }

    private void applyDto(Attendance attendance, AttendanceRequestDto dto) {
        Intern intern = internRepository.findById(dto.internId())
                .orElseThrow(() -> new ResourceNotFoundException("Intern not found: " + dto.internId()));
        attendance.setIntern(intern);
        if (dto.mentorId() != null) {
            Mentor mentor = mentorRepository.findById(dto.mentorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Mentor not found: " + dto.mentorId()));
            attendance.setMentor(mentor);
        }
        attendance.setAttendanceDate(dto.attendanceDate());
        attendance.setStatus(AttendanceStatus.valueOf(dto.status()));
    }
}
