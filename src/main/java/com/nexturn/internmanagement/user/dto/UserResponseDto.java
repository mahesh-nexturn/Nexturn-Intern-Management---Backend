package com.nexturn.internmanagement.user.dto;
import com.nexturn.internmanagement.user.User;
import com.nexturn.internmanagement.user.Role;

public record UserResponseDto(Long id, String name, String email, Role role) {
    public static UserResponseDto from(User u) {
        return new UserResponseDto(u.getId(), u.getName(), u.getEmail(), u.getRole());
    }
}
