package com.nexturn.internmanagement.dashboard;

import com.nexturn.internmanagement.common.ApiResponse;
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

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<AdminDashboardDto> admin() {
        return ApiResponse.ok(dashboardService.adminDashboard());
    }

    @GetMapping("/mentor/{mentorId}")
    @PreAuthorize("hasAnyRole('ADMIN','MENTOR')")
    public ApiResponse<MentorDashboardDto> mentor(@PathVariable Long mentorId) {
        return ApiResponse.ok(dashboardService.mentorDashboard(mentorId));
    }

    @GetMapping("/intern/{internId}")
    public ApiResponse<InternDashboardSummaryDto> intern(@PathVariable Long internId) {
        return ApiResponse.ok(dashboardService.internDashboard(internId));
    }
}
