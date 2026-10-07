package com.nexturn.internmanagement.attendance;

import java.util.Map;

public record AttendanceStatsDto(Map<String, Long> counts) {
}
