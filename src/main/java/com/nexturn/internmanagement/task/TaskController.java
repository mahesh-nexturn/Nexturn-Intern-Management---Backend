package com.nexturn.internmanagement.task;
import com.nexturn.internmanagement.task.dto.TaskRequestDto;
import com.nexturn.internmanagement.task.dto.TaskResponseDto;
import com.nexturn.internmanagement.task.dto.TaskStatsDto;

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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import com.nexturn.internmanagement.task.dto.TaskWorkUpdateDto;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;
    private final OwnershipService ownershipService;

    @GetMapping
    public ApiResponse<List<TaskResponseDto>> list(
            @RequestParam(required = false) Long internId,
            @RequestParam(required = false) Long mentorId,
            @AuthenticationPrincipal UserPrincipal principal) {
        if (ownershipService.isIntern(principal)) {
            return ApiResponse.ok(taskService.findByIntern(ownershipService.currentInternId(principal)));
        }
        if (ownershipService.isMentor(principal)) {
            if (internId != null) {
                return ApiResponse.ok(taskService.findByIntern(internId));
            }
            Long mentorScope = mentorId != null ? mentorId : ownershipService.currentMentorId(principal);
            ownershipService.assertOwnsMentorResource(principal, mentorScope);
            return ApiResponse.ok(taskService.findByMentor(mentorScope));
        }
        if (internId != null) {
            return ApiResponse.ok(scoped(principal, taskService.findByIntern(internId)));
        }
        if (mentorId != null) {
            return ApiResponse.ok(scoped(principal, taskService.findByMentor(mentorId)));
        }
        return ApiResponse.ok(scoped(principal, taskService.findAll()));
    }

    private List<TaskResponseDto> scoped(UserPrincipal principal, List<TaskResponseDto> tasks) {
        Predicate<Long> scope = ownershipService.internScope(principal);
        return tasks.stream().filter(t -> scope.test(t.internId())).toList();
    }

    @GetMapping("/stats")
    public ApiResponse<TaskStatsDto> stats(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok(taskService.stats(ownershipService.internScope(principal)));
    }

    @GetMapping("/{id}")
    public ApiResponse<TaskResponseDto> get(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        TaskResponseDto dto = taskService.findById(id);
        ownershipService.assertOwnsMentorResource(principal, dto.mentorId());
        ownershipService.assertOwnsInternResource(principal, dto.internId());
        return ApiResponse.ok(dto);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','HR','MENTOR')")
    public ApiResponse<TaskResponseDto> create(@Valid @RequestBody TaskRequestDto dto,
            @AuthenticationPrincipal UserPrincipal principal) {
        ownershipService.assertOwnsMentorResource(principal, dto.mentorId());
        ownershipService.assertOwnsInternResource(principal, dto.internId());
        return ApiResponse.ok(taskService.create(dto), "Task created");
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HR','MENTOR')")
    public ApiResponse<TaskResponseDto> update(@PathVariable Long id, @Valid @RequestBody TaskRequestDto dto,
            @AuthenticationPrincipal UserPrincipal principal) {
        TaskResponseDto existing = taskService.findById(id);
        ownershipService.assertOwnsMentorResource(principal, existing.mentorId());
        ownershipService.assertOwnsInternResource(principal, existing.internId());
        ownershipService.assertOwnsMentorResource(principal, dto.mentorId());
        ownershipService.assertOwnsInternResource(principal, dto.internId());
        return ApiResponse.ok(taskService.update(id, dto), "Task updated");
    }

    @PatchMapping("/{id}/work")
    public ApiResponse<TaskResponseDto> updateWork(@PathVariable Long id,
            @Valid @RequestBody TaskWorkUpdateDto dto, @AuthenticationPrincipal UserPrincipal principal) {
        TaskResponseDto existing = taskService.findById(id);
        ownershipService.assertOwnsMentorResource(principal, existing.mentorId());
        ownershipService.assertOwnsInternResource(principal, existing.internId());
        boolean canEditDescription = true;
        return ApiResponse.ok(taskService.updateWork(id, dto, canEditDescription), "Task updated");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HR','MENTOR')")
    public ApiResponse<Void> delete(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        TaskResponseDto existing = taskService.findById(id);
        ownershipService.assertOwnsMentorResource(principal, existing.mentorId());
        ownershipService.assertOwnsInternResource(principal, existing.internId());
        taskService.delete(id);
        return ApiResponse.ok(null, "Task deleted");
    }
}
