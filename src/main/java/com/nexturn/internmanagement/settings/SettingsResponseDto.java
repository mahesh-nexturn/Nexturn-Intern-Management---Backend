package com.nexturn.internmanagement.settings;

public record SettingsResponseDto(
        Long userId,
        String name,
        String email,
        String role,
        boolean emailNotifications,
        boolean pushNotifications,
        String theme,
        String phone) {

    public static SettingsResponseDto from(UserSettings settings) {
        return new SettingsResponseDto(
                settings.getUser().getId(),
                settings.getUser().getName(),
                settings.getUser().getEmail(),
                settings.getUser().getRole().name(),
                settings.isEmailNotifications(),
                settings.isPushNotifications(),
                settings.getTheme(),
                settings.getPhone());
    }
}
