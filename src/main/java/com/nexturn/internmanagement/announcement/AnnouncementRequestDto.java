package com.nexturn.internmanagement.announcement;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

public record AnnouncementRequestDto(
        @NotBlank String title,
        String description,
        LocalDate publishDate,
        LocalDate expiryDate,
        @NotBlank String priority,
        @NotBlank String targetAudience) {
}
