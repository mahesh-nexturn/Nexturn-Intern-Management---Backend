package com.nexturn.internmanagement.intern.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;

public record InternRequestDto(
        @NotBlank String name,
        @NotBlank @Email String email,
        String department,
        @Pattern(regexp = "Active|Inactive|Completed", message = "status must be Active, Inactive or Completed")
        String status,
        Long mentorId,
        Long userId,
        Long hrId,
        LocalDate endDate) {
}
