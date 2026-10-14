package com.nexturn.internmanagement.mentor.dto;
import com.nexturn.internmanagement.mentor.Mentor;

public record MentorResponseDto(Long id, String name, String email, String department, String designation,
        String status, Long userId, Long hrId, String hrName) {
    public static MentorResponseDto from(Mentor m) {
        return new MentorResponseDto(m.getId(), m.getName(), m.getEmail(), m.getDepartment(), m.getDesignation(),
                m.getStatus().name(), m.getUser() != null ? m.getUser().getId() : null,
                m.getHr() != null ? m.getHr().getId() : null,
                m.getHr() != null ? m.getHr().getName() : null);
    }
}
