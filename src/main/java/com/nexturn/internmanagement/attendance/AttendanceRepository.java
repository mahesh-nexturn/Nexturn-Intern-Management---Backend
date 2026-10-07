package com.nexturn.internmanagement.attendance;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    List<Attendance> findByInternId(Long internId);

    List<Attendance> findByInternIdAndAttendanceDateBetween(Long internId, LocalDate from, LocalDate to);

    Optional<Attendance> findByInternIdAndAttendanceDate(Long internId, LocalDate date);

    long countByInternIdAndStatus(Long internId, AttendanceStatus status);

    long countByAttendanceDateAndStatus(LocalDate date, AttendanceStatus status);
}
