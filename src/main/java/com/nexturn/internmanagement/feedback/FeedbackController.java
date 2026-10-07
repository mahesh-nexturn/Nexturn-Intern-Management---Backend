package com.nexturn.internmanagement.feedback;

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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/feedback")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<FeedbackResponseDto>> list() {
        return ApiResponse.ok(feedbackService.findAll());
    }

    @GetMapping("/my")
    public ApiResponse<List<FeedbackResponseDto>> my(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok(feedbackService.findByUser(principal.getId()));
    }

    @PostMapping
    public ApiResponse<FeedbackResponseDto> create(
            @AuthenticationPrincipal UserPrincipal principal, @Valid @RequestBody FeedbackRequestDto dto) {
        return ApiResponse.ok(feedbackService.create(principal.getId(), dto), "Feedback submitted");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        feedbackService.delete(id);
        return ApiResponse.ok(null, "Feedback deleted");
    }
}
