package com.nexturn.internmanagement.attendance;

import com.nexturn.internmanagement.common.ApiResponse;
import com.nexturn.internmanagement.security.OwnershipService;
import com.nexturn.internmanagement.security.UserPrincipal;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final OwnershipService ownershipService;

    @GetMapping
    public ApiResponse<List<AttendanceResponseDto>> list(@RequestParam(required = false) Long internId,
            @AuthenticationPrincipal UserPrincipal principal) {
        if (ownershipService.isIntern(principal)) {
            return ApiResponse.ok(attendanceService.findByIntern(ownershipService.currentInternId(principal)));
        }
        return ApiResponse.ok(internId != null ? attendanceService.findByIntern(internId)
                : attendanceService.findAll());
    }

    @GetMapping("/calendar")
    public ApiResponse<List<AttendanceResponseDto>> calendar(
            @RequestParam Long internId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @AuthenticationPrincipal UserPrincipal principal) {
        ownershipService.assertOwnsInternResource(principal, internId);
        return ApiResponse.ok(attendanceService.calendar(internId, from, to));
    }

    @GetMapping("/stats")
    public ApiResponse<AttendanceStatsDto> stats(@RequestParam Long internId,
            @AuthenticationPrincipal UserPrincipal principal) {
        ownershipService.assertOwnsInternResource(principal, internId);
        return ApiResponse.ok(attendanceService.stats(internId));
    }

    @GetMapping("/{id}")
    public ApiResponse<AttendanceResponseDto> get(@PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        AttendanceResponseDto dto = attendanceService.findById(id);
        ownershipService.assertOwnsMentorResource(principal, dto.mentorId());
        ownershipService.assertOwnsInternResource(principal, dto.internId());
        return ApiResponse.ok(dto);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MENTOR')")
    public ApiResponse<AttendanceResponseDto> create(@Valid @RequestBody AttendanceRequestDto dto,
            @AuthenticationPrincipal UserPrincipal principal) {
        ownershipService.assertOwnsMentorResource(principal, dto.mentorId());
        return ApiResponse.ok(attendanceService.upsert(dto), "Attendance recorded");
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MENTOR')")
    public ApiResponse<AttendanceResponseDto> update(@PathVariable Long id,
            @Valid @RequestBody AttendanceRequestDto dto,
            @AuthenticationPrincipal UserPrincipal principal) {
        ownershipService.assertOwnsMentorResource(principal, attendanceService.findById(id).mentorId());
        ownershipService.assertOwnsMentorResource(principal, dto.mentorId());
        return ApiResponse.ok(attendanceService.update(id, dto), "Attendance updated");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MENTOR')")
    public ApiResponse<Void> delete(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        ownershipService.assertOwnsMentorResource(principal, attendanceService.findById(id).mentorId());
        attendanceService.delete(id);
        return ApiResponse.ok(null, "Attendance deleted");
    }
}
