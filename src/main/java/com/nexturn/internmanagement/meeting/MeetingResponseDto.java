package com.nexturn.internmanagement.meeting;

import java.time.LocalDate;
import java.time.LocalTime;

public record MeetingResponseDto(Long id, String title, String agenda, Long mentorId, Long internId,
        String internName, LocalDate meetingDate, LocalTime meetingTime, String status) {
    public static MeetingResponseDto from(Meeting m) {
        return new MeetingResponseDto(m.getId(), m.getTitle(), m.getAgenda(),
                m.getMentor() != null ? m.getMentor().getId() : null,
                m.getIntern() != null ? m.getIntern().getId() : null,
                m.getIntern() != null ? m.getIntern().getName() : null,
                m.getMeetingDate(), m.getMeetingTime(), m.getStatus().name());
    }
}
