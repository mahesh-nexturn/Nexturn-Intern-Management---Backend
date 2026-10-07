package com.nexturn.internmanagement.settings;

public record SettingsRequestDto(
        boolean emailNotifications,
        boolean pushNotifications,
        String theme,
        String phone) {
}
