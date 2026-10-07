package com.nexturn.internmanagement.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserRequestDto(
        @NotBlank String name,
        @NotBlank @Email String email,
        String password,
        @NotBlank String role) {
}
