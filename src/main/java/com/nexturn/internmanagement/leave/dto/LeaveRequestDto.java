package com.nexturn.internmanagement.leave.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record LeaveRequestDto(
        @NotBlank @Size(max = 40) String leaveType,
        @NotNull LocalDate fromDate,
        @NotNull LocalDate toDate,
        @Size(max = 255) String teamEmail,
        @Size(max = 2000) String reason) {
}
