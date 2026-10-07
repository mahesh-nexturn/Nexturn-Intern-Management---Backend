package com.nexturn.internmanagement.certificate;

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
@RequestMapping("/api/certificates")
@RequiredArgsConstructor
public class CertificateController {

    private final CertificateService certificateService;
    private final OwnershipService ownershipService;

    @GetMapping
    public ApiResponse<List<CertificateResponseDto>> list(
            @RequestParam(required = false) Long internId,
            @RequestParam(required = false) Long mentorId,
            @AuthenticationPrincipal UserPrincipal principal) {
        if (ownershipService.isIntern(principal)) {
            return ApiResponse.ok(certificateService.findByIntern(ownershipService.currentInternId(principal)));
        }
        if (ownershipService.isMentor(principal)) {
            if (internId != null) {
                return ApiResponse.ok(certificateService.findByIntern(internId));
            }
            Long mentorScope = mentorId != null ? mentorId : ownershipService.currentMentorId(principal);
            ownershipService.assertOwnsMentorResource(principal, mentorScope);
            return ApiResponse.ok(certificateService.findByMentor(mentorScope));
        }
        if (internId != null) {
            return ApiResponse.ok(certificateService.findByIntern(internId));
        }
        if (mentorId != null) {
            return ApiResponse.ok(certificateService.findByMentor(mentorId));
        }
        return ApiResponse.ok(certificateService.findAll());
    }

    @GetMapping("/stats")
    public ApiResponse<CertificateStatsDto> stats() {
        return ApiResponse.ok(certificateService.stats());
    }

    @GetMapping("/{id}")
    public ApiResponse<CertificateResponseDto> get(@PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        CertificateResponseDto dto = certificateService.findById(id);
        ownershipService.assertOwnsMentorResource(principal, dto.mentorId());
        ownershipService.assertOwnsInternResource(principal, dto.internId());
        return ApiResponse.ok(dto);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<CertificateResponseDto> create(@Valid @RequestBody CertificateRequestDto dto) {
        return ApiResponse.ok(certificateService.create(dto), "Certificate created");
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<CertificateResponseDto> update(@PathVariable Long id,
            @Valid @RequestBody CertificateRequestDto dto) {
        return ApiResponse.ok(certificateService.update(id, dto), "Certificate updated");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        certificateService.delete(id);
        return ApiResponse.ok(null, "Certificate deleted");
    }
}
