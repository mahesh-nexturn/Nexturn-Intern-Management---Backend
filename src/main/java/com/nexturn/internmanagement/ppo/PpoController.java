package com.nexturn.internmanagement.ppo;
import com.nexturn.internmanagement.ppo.dto.PpoRequestDto;
import com.nexturn.internmanagement.ppo.dto.PpoResponseDto;
import com.nexturn.internmanagement.ppo.dto.PpoStatsDto;

import com.nexturn.internmanagement.common.ApiResponse;
import com.nexturn.internmanagement.security.OwnershipService;
import com.nexturn.internmanagement.security.UserPrincipal;
import jakarta.validation.Valid;
import java.util.List;
import java.util.function.Predicate;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ppo")
@RequiredArgsConstructor
public class PpoController {

    private final PpoService ppoService;
    private final OwnershipService ownershipService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','HR','MENTOR')")
    public ApiResponse<List<PpoResponseDto>> list(@RequestParam(required = false) Long mentorId,
            @AuthenticationPrincipal UserPrincipal principal) {
        if (ownershipService.isMentor(principal)) {
            Long mentorScope = mentorId != null ? mentorId : ownershipService.currentMentorId(principal);
            ownershipService.assertOwnsMentorResource(principal, mentorScope);
            return ApiResponse.ok(ppoService.findByMentor(mentorScope));
        }
        Predicate<Long> scope = ownershipService.internScope(principal);
        List<PpoResponseDto> items = mentorId != null ? ppoService.findByMentor(mentorId) : ppoService.findAll();
        return ApiResponse.ok(items.stream().filter(p -> scope.test(p.internId())).toList());
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('ADMIN','HR','MENTOR')")
    public ApiResponse<PpoStatsDto> stats(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok(ppoService.stats(ownershipService.internScope(principal)));
    }

    @GetMapping("/my")
    public ApiResponse<PpoResponseDto> my(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok(ppoService.findByUserId(principal.getId()));
    }

    @GetMapping("/{id}")
    public ApiResponse<PpoResponseDto> get(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        PpoResponseDto dto = ppoService.findById(id);
        ownershipService.assertOwnsMentorResource(principal, dto.mentorId());
        ownershipService.assertOwnsInternResource(principal, dto.internId());
        return ApiResponse.ok(dto);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','HR','MENTOR')")
    public ApiResponse<PpoResponseDto> create(@Valid @RequestBody PpoRequestDto dto,
            @AuthenticationPrincipal UserPrincipal principal) {
        ownershipService.assertOwnsMentorResource(principal, dto.mentorId());
        ownershipService.assertOwnsInternResource(principal, dto.internId());
        return ApiResponse.ok(ppoService.create(dto), "PPO record created");
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HR','MENTOR')")
    public ApiResponse<PpoResponseDto> update(@PathVariable Long id, @Valid @RequestBody PpoRequestDto dto,
            @AuthenticationPrincipal UserPrincipal principal) {
        PpoResponseDto existing = ppoService.findById(id);
        ownershipService.assertOwnsMentorResource(principal, existing.mentorId());
        ownershipService.assertOwnsInternResource(principal, existing.internId());
        ownershipService.assertOwnsMentorResource(principal, dto.mentorId());
        ownershipService.assertOwnsInternResource(principal, dto.internId());
        return ApiResponse.ok(ppoService.update(id, dto), "PPO record updated");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public ApiResponse<Void> delete(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        ownershipService.assertOwnsInternResource(principal, ppoService.findById(id).internId());
        ppoService.delete(id);
        return ApiResponse.ok(null, "PPO record deleted");
    }
}
