package com.nexturn.internmanagement.intern;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record InternRequestDto(
        @NotBlank String name,
        @NotBlank @Email String email,
        String department,
        String status,
        Long mentorId,
        Long userId) {
}
