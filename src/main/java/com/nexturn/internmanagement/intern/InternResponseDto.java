package com.nexturn.internmanagement.intern;

public record InternResponseDto(Long id, String name, String email, String department, String status,
        Long mentorId, String mentorName, Long userId) {
    public static InternResponseDto from(Intern i) {
        return new InternResponseDto(i.getId(), i.getName(), i.getEmail(), i.getDepartment(), i.getStatus(),
                i.getMentor() != null ? i.getMentor().getId() : null,
                i.getMentor() != null ? i.getMentor().getName() : null,
                i.getUser() != null ? i.getUser().getId() : null);
    }
}
