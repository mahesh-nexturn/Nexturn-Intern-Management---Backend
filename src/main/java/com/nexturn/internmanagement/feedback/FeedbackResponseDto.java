package com.nexturn.internmanagement.feedback;

import java.time.LocalDateTime;

public record FeedbackResponseDto(
        Long id,
        Long fromUserId,
        String fromUserName,
        String subject,
        String message,
        LocalDateTime createdAt) {

    public static FeedbackResponseDto from(Feedback feedback) {
        return new FeedbackResponseDto(
                feedback.getId(),
                feedback.getFromUser().getId(),
                feedback.getFromUser().getName(),
                feedback.getSubject(),
                feedback.getMessage(),
                feedback.getCreatedAt());
    }
}
