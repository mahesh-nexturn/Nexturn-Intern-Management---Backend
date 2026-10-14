package com.nexturn.internmanagement.evaluation;
import com.nexturn.internmanagement.evaluation.dto.EvaluationRequestDto;
import com.nexturn.internmanagement.evaluation.dto.EvaluationResponseDto;
import com.nexturn.internmanagement.evaluation.dto.EvaluationStatsDto;

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
@RequestMapping("/api/evaluations")
@RequiredArgsConstructor
public class EvaluationController {

    private final EvaluationService evaluationService;
    private final OwnershipService ownershipService;

    @GetMapping
    public ApiResponse<List<EvaluationResponseDto>> list(
            @RequestParam(required = false) Long internId,
            @RequestParam(required = false) Long mentorId,
            @AuthenticationPrincipal UserPrincipal principal) {
        if (ownershipService.isIntern(principal)) {
            return ApiResponse.ok(evaluationService.findByIntern(ownershipService.currentInternId(principal)));
        }
        if (ownershipService.isMentor(principal)) {
            if (internId != null) {
                return ApiResponse.ok(evaluationService.findByIntern(internId));
            }
            Long mentorScope = mentorId != null ? mentorId : ownershipService.currentMentorId(principal);
            ownershipService.assertOwnsMentorResource(principal, mentorScope);
            return ApiResponse.ok(evaluationService.findByMentor(mentorScope));
        }
        if (internId != null) {
            return ApiResponse.ok(scoped(principal, evaluationService.findByIntern(internId)));
        }
        if (mentorId != null) {
            return ApiResponse.ok(scoped(principal, evaluationService.findByMentor(mentorId)));
        }
        return ApiResponse.ok(scoped(principal, evaluationService.findAll()));
    }

    private List<EvaluationResponseDto> scoped(UserPrincipal principal, List<EvaluationResponseDto> items) {
        Predicate<Long> scope = ownershipService.internScope(principal);
        return items.stream().filter(e -> scope.test(e.internId())).toList();
    }

    @GetMapping("/stats")
    public ApiResponse<EvaluationStatsDto> stats(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok(evaluationService.stats(ownershipService.internScope(principal)));
    }

    @GetMapping("/{id}")
    public ApiResponse<EvaluationResponseDto> get(@PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        EvaluationResponseDto dto = evaluationService.findById(id);
        ownershipService.assertOwnsMentorResource(principal, dto.mentorId());
        ownershipService.assertOwnsInternResource(principal, dto.internId());
        return ApiResponse.ok(dto);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','HR','MENTOR')")
    public ApiResponse<EvaluationResponseDto> create(@Valid @RequestBody EvaluationRequestDto dto,
            @AuthenticationPrincipal UserPrincipal principal) {
        ownershipService.assertOwnsMentorResource(principal, dto.mentorId());
        ownershipService.assertOwnsInternResource(principal, dto.internId());
        return ApiResponse.ok(evaluationService.create(dto), "Evaluation created");
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HR','MENTOR')")
    public ApiResponse<EvaluationResponseDto> update(@PathVariable Long id,
            @Valid @RequestBody EvaluationRequestDto dto,
            @AuthenticationPrincipal UserPrincipal principal) {
        EvaluationResponseDto existing = evaluationService.findById(id);
        ownershipService.assertOwnsMentorResource(principal, existing.mentorId());
        ownershipService.assertOwnsInternResource(principal, existing.internId());
        ownershipService.assertOwnsMentorResource(principal, dto.mentorId());
        ownershipService.assertOwnsInternResource(principal, dto.internId());
        return ApiResponse.ok(evaluationService.update(id, dto), "Evaluation updated");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HR','MENTOR')")
    public ApiResponse<Void> delete(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        EvaluationResponseDto existing = evaluationService.findById(id);
        ownershipService.assertOwnsMentorResource(principal, existing.mentorId());
        ownershipService.assertOwnsInternResource(principal, existing.internId());
        evaluationService.delete(id);
        return ApiResponse.ok(null, "Evaluation deleted");
    }
}
