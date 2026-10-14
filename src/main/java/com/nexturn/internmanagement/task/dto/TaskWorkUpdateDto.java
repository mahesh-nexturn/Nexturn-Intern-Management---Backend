package com.nexturn.internmanagement.task.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record TaskWorkUpdateDto(
        String status,
        @Min(0) @Max(100) Short progress,
        @Size(max = 5000) String description,
        @Size(max = 200) String title) {
}
