package com.nexturn.internmanagement.attendance;

import com.nexturn.internmanagement.common.ResourceNotFoundException;
import com.nexturn.internmanagement.intern.Intern;
import com.nexturn.internmanagement.intern.InternRepository;
import com.nexturn.internmanagement.mentor.Mentor;
import com.nexturn.internmanagement.mentor.MentorRepository;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
