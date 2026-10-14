package com.nexturn.internmanagement.leave;

import com.nexturn.internmanagement.common.ApiResponse;
import com.nexturn.internmanagement.leave.dto.LeaveRequestDto;
import com.nexturn.internmanagement.leave.dto.LeaveResponseDto;
import com.nexturn.internmanagement.security.OwnershipService;
import com.nexturn.internmanagement.security.UserPrincipal;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/leaves")
@RequiredArgsConstructor
public class LeaveController {

    private final LeaveService leaveService;
    private final OwnershipService ownershipService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','HR','INTERN')")
    public ApiResponse<List<LeaveResponseDto>> list(@AuthenticationPrincipal UserPrincipal principal) {
        if (ownershipService.isIntern(principal)) {
            return ApiResponse.ok(leaveService.findForIntern(ownershipService.currentInternId(principal)));
        }
        return ApiResponse.ok(leaveService.findAll(ownershipService.internScope(principal)));
    }

    @PostMapping
    @PreAuthorize("hasRole('INTERN')")
    public ApiResponse<LeaveResponseDto> apply(@Valid @RequestBody LeaveRequestDto dto,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok(leaveService.apply(ownershipService.currentInternId(principal), dto),
                "Leave request submitted");
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public ApiResponse<LeaveResponseDto> approve(@PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        ownershipService.assertOwnsInternResource(principal, leaveService.getEntity(id).getIntern().getId());
        return ApiResponse.ok(leaveService.decide(id, true, principal.getUser()), "Leave approved");
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public ApiResponse<LeaveResponseDto> reject(@PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        ownershipService.assertOwnsInternResource(principal, leaveService.getEntity(id).getIntern().getId());
        return ApiResponse.ok(leaveService.decide(id, false, principal.getUser()), "Leave rejected");
    }
}
