package com.nexturn.internmanagement.notification.dto;

import jakarta.validation.constraints.NotBlank;

public record NotificationRequestDto(@NotBlank String title, @NotBlank String message,
        @NotBlank String recipient) {
}
