package com.nexturn.internmanagement.feedback.dto;

import jakarta.validation.constraints.NotBlank;

public record FeedbackRequestDto(String subject, @NotBlank String message) {
}
