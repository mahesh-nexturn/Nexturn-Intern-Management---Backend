package com.nexturn.internmanagement.training;
import com.nexturn.internmanagement.training.dto.TrainingRequestDto;
import com.nexturn.internmanagement.training.dto.TrainingResponseDto;
import com.nexturn.internmanagement.training.dto.TrainingStatsDto;

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
@RequestMapping("/api/training")
@RequiredArgsConstructor
public class TrainingController {

    private final TrainingService trainingService;
    private final OwnershipService ownershipService;

    @GetMapping
    public ApiResponse<List<TrainingResponseDto>> list(
            @RequestParam(required = false) Long internId,
            @RequestParam(required = false) Long mentorId,
            @AuthenticationPrincipal UserPrincipal principal) {
        if (ownershipService.isIntern(principal)) {
            return ApiResponse.ok(trainingService.findByIntern(ownershipService.currentInternId(principal)));
        }
        if (ownershipService.isMentor(principal)) {
            if (internId != null) {
                return ApiResponse.ok(trainingService.findByIntern(internId));
            }
            Long mentorScope = mentorId != null ? mentorId : ownershipService.currentMentorId(principal);
            ownershipService.assertOwnsMentorResource(principal, mentorScope);
            return ApiResponse.ok(trainingService.findByMentor(mentorScope));
        }
        if (internId != null) {
            return ApiResponse.ok(scoped(principal, trainingService.findByIntern(internId)));
        }
        if (mentorId != null) {
            return ApiResponse.ok(scoped(principal, trainingService.findByMentor(mentorId)));
        }
        return ApiResponse.ok(scoped(principal, trainingService.findAll()));
    }

    private List<TrainingResponseDto> scoped(UserPrincipal principal, List<TrainingResponseDto> items) {
        Predicate<Long> scope = ownershipService.internScope(principal);
        return items.stream().filter(t -> scope.test(t.internId())).toList();
    }

    @GetMapping("/stats")
    public ApiResponse<TrainingStatsDto> stats(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok(trainingService.stats(ownershipService.internScope(principal)));
    }

    @GetMapping("/{id}")
    public ApiResponse<TrainingResponseDto> get(@PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        TrainingResponseDto dto = trainingService.findById(id);
        ownershipService.assertOwnsMentorResource(principal, dto.mentorId());
        ownershipService.assertOwnsInternResource(principal, dto.internId());
        return ApiResponse.ok(dto);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','HR','MENTOR')")
    public ApiResponse<TrainingResponseDto> create(@Valid @RequestBody TrainingRequestDto dto,
            @AuthenticationPrincipal UserPrincipal principal) {
        ownershipService.assertOwnsMentorResource(principal, dto.mentorId());
        ownershipService.assertOwnsInternResource(principal, dto.internId());
        return ApiResponse.ok(trainingService.create(dto), "Training created");
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HR','MENTOR')")
    public ApiResponse<TrainingResponseDto> update(@PathVariable Long id,
            @Valid @RequestBody TrainingRequestDto dto,
            @AuthenticationPrincipal UserPrincipal principal) {
        TrainingResponseDto existing = trainingService.findById(id);
        ownershipService.assertOwnsMentorResource(principal, existing.mentorId());
        ownershipService.assertOwnsInternResource(principal, existing.internId());
        ownershipService.assertOwnsMentorResource(principal, dto.mentorId());
        ownershipService.assertOwnsInternResource(principal, dto.internId());
        return ApiResponse.ok(trainingService.update(id, dto), "Training updated");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HR','MENTOR')")
    public ApiResponse<Void> delete(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        TrainingResponseDto existing = trainingService.findById(id);
        ownershipService.assertOwnsMentorResource(principal, existing.mentorId());
        ownershipService.assertOwnsInternResource(principal, existing.internId());
        trainingService.delete(id);
        return ApiResponse.ok(null, "Training deleted");
    }
}
