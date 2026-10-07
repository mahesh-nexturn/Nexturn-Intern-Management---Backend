package com.nexturn.internmanagement.announcement;

import com.nexturn.internmanagement.common.ApiResponse;
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
@RequestMapping("/api/announcements")
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementService announcementService;

    @GetMapping
    public ApiResponse<List<AnnouncementResponseDto>> list(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok(announcementService.findForRole(principal.getUser().getRole()));
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<AnnouncementStatsDto> stats() {
        return ApiResponse.ok(announcementService.stats());
    }

    @GetMapping("/{id}")
    public ApiResponse<AnnouncementResponseDto> get(@PathVariable Long id) {
        return ApiResponse.ok(announcementService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<AnnouncementResponseDto> create(@Valid @RequestBody AnnouncementRequestDto dto,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok(announcementService.create(dto, principal.getId()), "Announcement created");
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<AnnouncementResponseDto> update(@PathVariable Long id,
            @Valid @RequestBody AnnouncementRequestDto dto) {
        return ApiResponse.ok(announcementService.update(id, dto), "Announcement updated");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        announcementService.delete(id);
        return ApiResponse.ok(null, "Announcement deleted");
    }
}
