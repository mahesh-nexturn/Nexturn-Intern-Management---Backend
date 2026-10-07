package com.nexturn.internmanagement.mentor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record MentorRequestDto(
        @NotBlank String name,
        @NotBlank @Email String email,
        String department,
        String designation,
        String status,
        Long userId) {
}
