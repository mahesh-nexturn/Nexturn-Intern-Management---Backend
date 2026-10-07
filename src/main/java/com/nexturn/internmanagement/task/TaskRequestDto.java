package com.nexturn.internmanagement.task;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record TaskRequestDto(
        @NotBlank String title,
        String description,
        @NotNull Long internId,
        Long mentorId,
        @NotBlank String priority,
        LocalDate dueDate,
        String status,
        Short progress) {
}
