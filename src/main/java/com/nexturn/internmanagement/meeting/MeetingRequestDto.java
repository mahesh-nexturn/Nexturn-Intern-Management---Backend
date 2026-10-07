package com.nexturn.internmanagement.meeting;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

public record MeetingRequestDto(
        @NotBlank String title,
        String agenda,
        Long mentorId,
        @NotNull Long internId,
        @NotNull LocalDate meetingDate,
        @NotNull LocalTime meetingTime,
        String status) {
}
