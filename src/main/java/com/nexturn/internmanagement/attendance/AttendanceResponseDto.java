package com.nexturn.internmanagement.attendance;

import java.time.LocalDate;

public record AttendanceResponseDto(Long id, Long internId, String internName, Long mentorId, LocalDate date,
        String status) {
    public static AttendanceResponseDto from(Attendance a) {
        return new AttendanceResponseDto(a.getId(), a.getIntern().getId(), a.getIntern().getName(),
                a.getMentor() != null ? a.getMentor().getId() : null, a.getAttendanceDate(), a.getStatus().name());
    }
}
