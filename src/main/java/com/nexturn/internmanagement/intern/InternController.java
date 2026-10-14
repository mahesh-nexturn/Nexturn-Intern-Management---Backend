package com.nexturn.internmanagement.intern;
import com.nexturn.internmanagement.intern.dto.InternRequestDto;
import com.nexturn.internmanagement.intern.dto.InternResponseDto;
import com.nexturn.internmanagement.intern.dto.InternDashboardDto;

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
        if (ownershipService.isHr(principal)) {
            return ApiResponse.ok(internService.findByHr(principal.getId()));
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
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public ApiResponse<InternResponseDto> create(@Valid @RequestBody InternRequestDto dto,
            @AuthenticationPrincipal UserPrincipal principal) {
        if (ownershipService.isHr(principal)) {
            ownershipService.assertHrOwnsMentor(principal, dto.mentorId());
            return ApiResponse.ok(internService.create(withoutHrAndUser(dto), principal.getId()), "Intern created");
        }
        return ApiResponse.ok(internService.create(dto, dto.hrId()), "Intern created");
    }

    private InternRequestDto withoutHrAndUser(InternRequestDto dto) {
        return new InternRequestDto(dto.name(), dto.email(), dto.department(), dto.status(), dto.mentorId(),
                null, null);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HR','MENTOR')")
    public ApiResponse<InternResponseDto> update(@PathVariable Long id, @Valid @RequestBody InternRequestDto dto,
            @AuthenticationPrincipal UserPrincipal principal) {
        if (!ownershipService.isHr(principal)) {
            ownershipService.assertOwnsMentorResource(principal, internService.findById(id).mentorId());
        }
        ownershipService.assertOwnsMentorResource(principal, dto.mentorId());
        ownershipService.assertHrOwnsMentor(principal, dto.mentorId());
        if (ownershipService.isHr(principal)) {
            ownershipService.assertOwnsInternResource(principal, id);
            return ApiResponse.ok(internService.update(id, withoutHrAndUser(dto), null), "Intern updated");
        }
        Long hrId = ownershipService.isAdmin(principal) ? dto.hrId() : null;
        return ApiResponse.ok(internService.update(id, dto, hrId), "Intern updated");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public ApiResponse<Void> delete(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        ownershipService.assertOwnsInternResource(principal, id);
        internService.delete(id);
        return ApiResponse.ok(null, "Intern deleted");
    }
}
