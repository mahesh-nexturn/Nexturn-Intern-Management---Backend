package com.nexturn.internmanagement.attendance.dto;

import java.util.Map;

public record AttendanceStatsDto(Map<String, Long> counts) {
}
