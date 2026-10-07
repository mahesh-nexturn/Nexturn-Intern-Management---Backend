package com.nexturn.internmanagement.announcement;

import java.time.LocalDate;

public record AnnouncementResponseDto(Long id, String title, String description, Long createdByUserId,
        LocalDate publishDate, LocalDate expiryDate, String priority, String targetAudience) {
    public static AnnouncementResponseDto from(Announcement a) {
        return new AnnouncementResponseDto(a.getId(), a.getTitle(), a.getDescription(), a.getCreatedBy().getId(),
                a.getPublishDate(), a.getExpiryDate(), a.getPriority().name(), a.getTargetAudience().name());
    }
}
