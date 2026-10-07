package com.nexturn.internmanagement.training;

import java.time.LocalDate;

public record TrainingResponseDto(Long id, String title, Long internId, String internName, Long mentorId,
        LocalDate startDate, LocalDate endDate, String status, short progress) {
    public static TrainingResponseDto from(Training t) {
        return new TrainingResponseDto(t.getId(), t.getTitle(), t.getIntern().getId(), t.getIntern().getName(),
                t.getMentor() != null ? t.getMentor().getId() : null, t.getStartDate(), t.getEndDate(),
                t.getStatus().name().replace('_', ' '), t.getProgress());
    }
}
