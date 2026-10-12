package com.nexturn.internmanagement.notification.dto;
import com.nexturn.internmanagement.notification.Notification;

import java.time.LocalDateTime;

public record NotificationResponseDto(Long id, String title, String message, String recipient,
        Long createdByUserId, LocalDateTime notificationDate, String status) {
    public static NotificationResponseDto from(Notification n) {
        return new NotificationResponseDto(n.getId(), n.getTitle(), n.getMessage(), n.getRecipient().name(),
                n.getCreatedBy().getId(), n.getNotificationDate(), n.getStatus().name());
    }
}
