package com.nexturn.internmanagement.task;

import java.time.LocalDate;

public record TaskResponseDto(Long id, String title, String description, Long internId, String internName,
        Long mentorId, String priority, LocalDate dueDate, String status, short progress) {
    public static TaskResponseDto from(Task t) {
        return new TaskResponseDto(
                t.getId(), t.getTitle(), t.getDescription(),
                t.getIntern() != null ? t.getIntern().getId() : null,
                t.getIntern() != null ? t.getIntern().getName() : null,
                t.getMentor() != null ? t.getMentor().getId() : null,
                t.getPriority().name(),
                t.getDueDate(),
                t.getStatus().name().replace('_', ' '),
                t.getProgress());
    }
}
