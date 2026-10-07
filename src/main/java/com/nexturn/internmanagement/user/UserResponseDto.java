package com.nexturn.internmanagement.user;

public record UserResponseDto(Long id, String name, String email, Role role) {
    public static UserResponseDto from(User u) {
        return new UserResponseDto(u.getId(), u.getName(), u.getEmail(), u.getRole());
    }
}
