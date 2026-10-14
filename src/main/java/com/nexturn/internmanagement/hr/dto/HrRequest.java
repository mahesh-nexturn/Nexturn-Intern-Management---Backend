package com.nexturn.internmanagement.hr.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record HrRequest(@NotBlank String name, @NotBlank @Email String email, String password) {}
