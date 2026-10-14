package com.nexturn.internmanagement.dashboard;
import com.nexturn.internmanagement.dashboard.dto.AdminDashboardDto;
import com.nexturn.internmanagement.dashboard.dto.InternDashboardSummaryDto;
import com.nexturn.internmanagement.dashboard.dto.MentorDashboardDto;

import com.nexturn.internmanagement.common.ApiResponse;
import com.nexturn.internmanagement.security.OwnershipService;
import com.nexturn.internmanagement.security.UserPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final OwnershipService ownershipService;

    @GetMapping("/admin")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public ApiResponse<AdminDashboardDto> admin(@AuthenticationPrincipal UserPrincipal principal) {
        if (ownershipService.isHr(principal)) {
            return ApiResponse.ok(dashboardService.hrDashboard(principal.getId()));
        }
        return ApiResponse.ok(dashboardService.adminDashboard());
    }

    @GetMapping("/mentor/{mentorId}")
    @PreAuthorize("hasAnyRole('ADMIN','HR','MENTOR')")
    public ApiResponse<MentorDashboardDto> mentor(@PathVariable Long mentorId,
            @AuthenticationPrincipal UserPrincipal principal) {
        ownershipService.assertHrOwnsMentor(principal, mentorId);
        return ApiResponse.ok(dashboardService.mentorDashboard(mentorId));
    }

    @GetMapping("/intern/{internId}")
    public ApiResponse<InternDashboardSummaryDto> intern(@PathVariable Long internId,
            @AuthenticationPrincipal UserPrincipal principal) {
        if (ownershipService.isHr(principal) || ownershipService.isIntern(principal)) {
            ownershipService.assertOwnsInternResource(principal, internId);
        }
        return ApiResponse.ok(dashboardService.internDashboard(internId));
    }
}
