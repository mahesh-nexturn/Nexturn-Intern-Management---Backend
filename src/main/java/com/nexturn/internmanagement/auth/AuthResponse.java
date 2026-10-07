package com.nexturn.internmanagement.auth;

public record AuthResponse(String accessToken, String refreshToken, String tokenType, Long userId, String name,
        String email, String role) {
}
