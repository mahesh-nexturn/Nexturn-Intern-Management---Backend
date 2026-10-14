package com.nexturn.internmanagement.leave.dto;

import com.nexturn.internmanagement.leave.LeaveRequest;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public record LeaveResponseDto(Long id, Long internId, String internName, Long hrId, String hrName,
        String leaveType, LocalDate fromDate, LocalDate toDate, long days, String teamEmail, String reason,
        String status, Instant createdAt, Instant decidedAt) {

    public static LeaveResponseDto from(LeaveRequest l) {
        return new LeaveResponseDto(l.getId(), l.getIntern().getId(), l.getIntern().getName(),
                l.getHr() != null ? l.getHr().getId() : null, l.getHr() != null ? l.getHr().getName() : null,
                l.getLeaveType(), l.getFromDate(), l.getToDate(),
                ChronoUnit.DAYS.between(l.getFromDate(), l.getToDate()) + 1, l.getTeamEmail(), l.getReason(),
                l.getStatus().name(), l.getCreatedAt(), l.getDecidedAt());
    }
}
