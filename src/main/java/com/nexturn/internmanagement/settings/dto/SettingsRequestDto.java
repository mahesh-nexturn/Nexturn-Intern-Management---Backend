package com.nexturn.internmanagement.settings.dto;

public record SettingsRequestDto(
        boolean emailNotifications,
        boolean pushNotifications,
        String theme,
        String phone) {
}
