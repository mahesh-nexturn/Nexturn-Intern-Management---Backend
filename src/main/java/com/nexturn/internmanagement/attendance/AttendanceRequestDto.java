package com.nexturn.internmanagement.attendance;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record AttendanceRequestDto(
        @NotNull Long internId,
        Long mentorId,
        @NotNull LocalDate attendanceDate,
        @NotBlank String status) {
}
