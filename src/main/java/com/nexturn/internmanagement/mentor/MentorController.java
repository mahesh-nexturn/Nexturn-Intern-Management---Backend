package com.nexturn.internmanagement.mentor;
import com.nexturn.internmanagement.mentor.dto.MentorRequestDto;
import com.nexturn.internmanagement.mentor.dto.MentorResponseDto;

import com.nexturn.internmanagement.common.ApiResponse;
import com.nexturn.internmanagement.intern.dto.InternResponseDto;
import com.nexturn.internmanagement.security.OwnershipService;
import com.nexturn.internmanagement.security.UserPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mentors")
@RequiredArgsConstructor
public class MentorController {

    private final MentorService mentorService;
    private final OwnershipService ownershipService;

    @GetMapping
    public ApiResponse<List<MentorResponseDto>> list(@AuthenticationPrincipal UserPrincipal principal) {
        if (ownershipService.isHr(principal)) {
            return ApiResponse.ok(mentorService.findByHr(principal.getId()));
        }
        return ApiResponse.ok(mentorService.findAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<MentorResponseDto> get(@PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        ownershipService.assertHrOwnsMentor(principal, id);
        return ApiResponse.ok(mentorService.findById(id));
    }

    @GetMapping("/{id}/interns")
    public ApiResponse<List<InternResponseDto>> interns(@PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        ownershipService.assertHrOwnsMentor(principal, id);
        return ApiResponse.ok(mentorService.interns(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public ApiResponse<MentorResponseDto> create(@Valid @RequestBody MentorRequestDto dto,
            @AuthenticationPrincipal UserPrincipal principal) {
        if (ownershipService.isHr(principal)) {
            MentorRequestDto own = new MentorRequestDto(dto.name(), dto.email(), dto.department(),
                    dto.designation(), dto.status(), null, null);
            return ApiResponse.ok(mentorService.create(own, principal.getId()), "Mentor created");
        }
        return ApiResponse.ok(mentorService.create(dto, dto.hrId()), "Mentor created");
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public ApiResponse<MentorResponseDto> update(@PathVariable Long id, @Valid @RequestBody MentorRequestDto dto,
            @AuthenticationPrincipal UserPrincipal principal) {
        ownershipService.assertHrOwnsMentor(principal, id);
        if (ownershipService.isHr(principal)) {
            MentorRequestDto own = new MentorRequestDto(dto.name(), dto.email(), dto.department(),
                    dto.designation(), dto.status(), null, null);
            return ApiResponse.ok(mentorService.update(id, own, null), "Mentor updated");
        }
        return ApiResponse.ok(mentorService.update(id, dto, dto.hrId()), "Mentor updated");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public ApiResponse<Void> delete(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        ownershipService.assertHrOwnsMentor(principal, id);
        mentorService.delete(id);
        return ApiResponse.ok(null, "Mentor deleted");
    }
}
