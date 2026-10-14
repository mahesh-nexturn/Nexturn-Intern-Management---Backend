package com.nexturn.internmanagement.attendance.dto;

import java.time.Instant;

/** Today's check-in state; serverTime lets the client run an accurate timer regardless of its own clock. */
public record TodayAttendanceDto(AttendanceResponseDto record, Instant serverTime, long requiredSeconds) {}
