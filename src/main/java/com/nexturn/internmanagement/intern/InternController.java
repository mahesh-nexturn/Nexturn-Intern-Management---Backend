package com.nexturn.internmanagement.intern;

import com.nexturn.internmanagement.common.ApiResponse;
import com.nexturn.internmanagement.security.OwnershipService;
import com.nexturn.internmanagement.security.UserPrincipal;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/interns")
@RequiredArgsConstructor
public class InternController {

    private final InternService internService;
    private final OwnershipService ownershipService;

    @GetMapping
    public ApiResponse<List<InternResponseDto>> list(@AuthenticationPrincipal UserPrincipal principal) {
        if (ownershipService.isIntern(principal)) {
            Long ownInternId = ownershipService.currentInternId(principal);
            return ApiResponse.ok(ownInternId == null ? List.of() : List.of(internService.findById(ownInternId)));
        }
        if (ownershipService.isMentor(principal)) {
            return ApiResponse.ok(internService.findByMentor(ownershipService.currentMentorId(principal)));
        }
        return ApiResponse.ok(internService.findAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<InternResponseDto> get(@PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        InternResponseDto dto = internService.findById(id);
        ownershipService.assertOwnsMentorResource(principal, dto.mentorId());
        ownershipService.assertOwnsInternResource(principal, dto.id());
        return ApiResponse.ok(dto);
    }

    @GetMapping("/{id}/dashboard")
    public ApiResponse<InternDashboardDto> dashboard(@PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        InternResponseDto dto = internService.findById(id);
        ownershipService.assertOwnsMentorResource(principal, dto.mentorId());
        ownershipService.assertOwnsInternResource(principal, dto.id());
        return ApiResponse.ok(internService.dashboard(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<InternResponseDto> create(@Valid @RequestBody InternRequestDto dto) {
        return ApiResponse.ok(internService.create(dto), "Intern created");
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MENTOR')")
    public ApiResponse<InternResponseDto> update(@PathVariable Long id, @Valid @RequestBody InternRequestDto dto,
            @AuthenticationPrincipal UserPrincipal principal) {
        ownershipService.assertOwnsMentorResource(principal, internService.findById(id).mentorId());
        ownershipService.assertOwnsMentorResource(principal, dto.mentorId());
        return ApiResponse.ok(internService.update(id, dto), "Intern updated");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        internService.delete(id);
        return ApiResponse.ok(null, "Intern deleted");
    }
}
