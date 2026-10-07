package com.nexturn.internmanagement.meeting;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/meetings")
@RequiredArgsConstructor
public class MeetingController {

    private final MeetingService meetingService;
    private final OwnershipService ownershipService;

    @GetMapping
    public ApiResponse<List<MeetingResponseDto>> list(
            @RequestParam(required = false) Long internId,
            @RequestParam(required = false) Long mentorId,
            @AuthenticationPrincipal UserPrincipal principal) {
        if (ownershipService.isIntern(principal)) {
            return ApiResponse.ok(meetingService.findByIntern(ownershipService.currentInternId(principal)));
        }
        if (ownershipService.isMentor(principal)) {
            if (internId != null) {
                return ApiResponse.ok(meetingService.findByIntern(internId));
            }
            Long mentorScope = mentorId != null ? mentorId : ownershipService.currentMentorId(principal);
            ownershipService.assertOwnsMentorResource(principal, mentorScope);
            return ApiResponse.ok(meetingService.findByMentor(mentorScope));
        }
        if (internId != null) {
            return ApiResponse.ok(meetingService.findByIntern(internId));
        }
        if (mentorId != null) {
            return ApiResponse.ok(meetingService.findByMentor(mentorId));
        }
        return ApiResponse.ok(meetingService.findAll());
    }

    @GetMapping("/upcoming")
    public ApiResponse<List<MeetingResponseDto>> upcoming() {
        return ApiResponse.ok(meetingService.upcoming());
    }

    @GetMapping("/{id}")
    public ApiResponse<MeetingResponseDto> get(@PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        MeetingResponseDto dto = meetingService.findById(id);
        ownershipService.assertOwnsMentorResource(principal, dto.mentorId());
        ownershipService.assertOwnsInternResource(principal, dto.internId());
        return ApiResponse.ok(dto);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MENTOR')")
    public ApiResponse<MeetingResponseDto> create(@Valid @RequestBody MeetingRequestDto dto,
            @AuthenticationPrincipal UserPrincipal principal) {
        ownershipService.assertOwnsMentorResource(principal, dto.mentorId());
        return ApiResponse.ok(meetingService.create(dto), "Meeting scheduled");
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MENTOR')")
    public ApiResponse<MeetingResponseDto> update(@PathVariable Long id, @Valid @RequestBody MeetingRequestDto dto,
            @AuthenticationPrincipal UserPrincipal principal) {
        ownershipService.assertOwnsMentorResource(principal, meetingService.findById(id).mentorId());
        ownershipService.assertOwnsMentorResource(principal, dto.mentorId());
        return ApiResponse.ok(meetingService.update(id, dto), "Meeting updated");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MENTOR')")
    public ApiResponse<Void> delete(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        ownershipService.assertOwnsMentorResource(principal, meetingService.findById(id).mentorId());
        meetingService.delete(id);
        return ApiResponse.ok(null, "Meeting deleted");
    }
}
