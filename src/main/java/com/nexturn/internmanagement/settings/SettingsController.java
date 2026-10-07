package com.nexturn.internmanagement.settings;

import com.nexturn.internmanagement.common.ApiResponse;
import com.nexturn.internmanagement.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
public class SettingsController {

    private final SettingsService settingsService;

    @GetMapping("/me")
    public ApiResponse<SettingsResponseDto> me(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok(settingsService.findByUserId(principal.getId()));
    }

    @PutMapping("/me")
    public ApiResponse<SettingsResponseDto> updateMe(
            @AuthenticationPrincipal UserPrincipal principal, @Valid @RequestBody SettingsRequestDto dto) {
        return ApiResponse.ok(settingsService.update(principal.getId(), dto), "Settings updated");
    }
}
