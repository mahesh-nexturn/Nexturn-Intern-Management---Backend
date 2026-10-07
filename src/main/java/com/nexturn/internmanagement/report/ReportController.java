package com.nexturn.internmanagement.report;

import com.nexturn.internmanagement.common.ApiResponse;
import com.nexturn.internmanagement.security.OwnershipService;
import com.nexturn.internmanagement.security.UserPrincipal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final ReportExportService reportExportService;
    private final OwnershipService ownershipService;

    @GetMapping
    public ApiResponse<List<InternReportDto>> generate(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok(scopedReports(principal));
    }

    @GetMapping("/export")
    @PreAuthorize("hasAnyRole('ADMIN','MENTOR')")
    public ResponseEntity<byte[]> export(@AuthenticationPrincipal UserPrincipal principal) {
        byte[] excel = reportExportService.exportToExcel(scopedReports(principal));
        String filename = "intern-reports.xlsx";
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(filename).build().toString())
                .body(excel);
    }

    @GetMapping("/intern/{internId}")
    public ApiResponse<InternReportDto> generateForIntern(@PathVariable Long internId,
            @AuthenticationPrincipal UserPrincipal principal) {
        InternReportDto report = reportService.generateForIntern(internId);
        ownershipService.assertOwnsMentorResource(principal, report.mentorId());
        ownershipService.assertOwnsInternResource(principal, report.internId());
        return ApiResponse.ok(report);
    }

    private List<InternReportDto> scopedReports(UserPrincipal principal) {
        if (ownershipService.isIntern(principal)) {
            Long ownInternId = ownershipService.currentInternId(principal);
            return ownInternId == null ? List.of() : List.of(reportService.generateForIntern(ownInternId));
        }
        if (ownershipService.isMentor(principal)) {
            return reportService.generateForMentor(ownershipService.currentMentorId(principal));
        }
        return reportService.generate();
    }
}
