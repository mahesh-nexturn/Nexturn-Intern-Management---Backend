package com.nexturn.internmanagement.training;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record TrainingRequestDto(
        @NotBlank String title,
        @NotNull Long internId,
        Long mentorId,
        LocalDate startDate,
        LocalDate endDate,
        String status,
        Short progress) {
}
