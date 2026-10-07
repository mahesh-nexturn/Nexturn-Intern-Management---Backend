package com.nexturn.internmanagement.notification;

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
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ApiResponse<List<NotificationResponseDto>> list(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok(notificationService.findForRole(principal.getUser().getRole()));
    }

    @GetMapping("/{id}")
    public ApiResponse<NotificationResponseDto> get(@PathVariable Long id) {
        return ApiResponse.ok(notificationService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<NotificationResponseDto> create(@Valid @RequestBody NotificationRequestDto dto,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok(notificationService.create(dto, principal.getId()), "Notification created");
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<NotificationResponseDto> update(@PathVariable Long id,
            @Valid @RequestBody NotificationRequestDto dto) {
        return ApiResponse.ok(notificationService.update(id, dto), "Notification updated");
    }

    @PutMapping("/{id}/mark-read")
    public ApiResponse<NotificationResponseDto> markRead(@PathVariable Long id) {
        return ApiResponse.ok(notificationService.markRead(id), "Notification marked read");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        notificationService.delete(id);
        return ApiResponse.ok(null, "Notification deleted");
    }
}
