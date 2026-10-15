package com.nexturn.internmanagement.intern.dto;
import com.nexturn.internmanagement.intern.Intern;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record InternResponseDto(Long id, String name, String email, String department, String status,
        Long mentorId, String mentorName, Long userId, LocalDateTime createTime, LocalDateTime updateTime,
        Long hrId, String hrName, LocalDate endDate) {
    public static InternResponseDto from(Intern i) {
        String status = i.getStatus();
        if ("Active".equals(status) && i.getEndDate() != null && i.getEndDate().isBefore(LocalDate.now())) {
            status = "Completed";
        }
        return new InternResponseDto(i.getId(), i.getName(), i.getEmail(), i.getDepartment(), status,
                i.getMentor() != null ? i.getMentor().getId() : null,
                i.getMentor() != null ? i.getMentor().getName() : null,
                i.getUser() != null ? i.getUser().getId() : null,
                i.getCreatedAt(),
                i.getUpdatedAt(),
                i.getHr() != null ? i.getHr().getId() : null,
                i.getHr() != null ? i.getHr().getName() : null,
                i.getEndDate());
    }
}
