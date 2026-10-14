package com.nexturn.internmanagement.hr.dto;

public record HrResponse(Long id, String name, String email, long mentorCount, long internCount) {}
