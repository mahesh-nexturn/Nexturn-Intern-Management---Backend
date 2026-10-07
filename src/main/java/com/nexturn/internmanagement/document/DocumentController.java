package com.nexturn.internmanagement.document;

import com.nexturn.internmanagement.common.ApiResponse;
import com.nexturn.internmanagement.security.OwnershipService;
import com.nexturn.internmanagement.security.UserPrincipal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;
    private final OwnershipService ownershipService;

    @GetMapping
    public ApiResponse<List<DocumentResponseDto>> list(@RequestParam(required = false) Long userId,
            @AuthenticationPrincipal UserPrincipal principal) {
        if (ownershipService.isAdmin(principal)) {
            return ApiResponse.ok(userId != null ? documentService.findByUser(userId) : documentService.findAll());
        }
        return ApiResponse.ok(documentService.findByUser(principal.getId()));
    }

    @GetMapping("/latest")
    public ApiResponse<List<DocumentResponseDto>> latest(@RequestParam(defaultValue = "5") int limit) {
        return ApiResponse.ok(documentService.latest(limit));
    }

    @GetMapping("/{id}")
    public ApiResponse<DocumentResponseDto> get(@PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        DocumentResponseDto dto = documentService.findById(id);
        assertOwnsDocument(principal, dto);
        return ApiResponse.ok(dto);
    }

    @PostMapping("/upload")
    public ApiResponse<DocumentResponseDto> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("documentType") String documentType,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok(documentService.upload(file, documentType, principal.getId()), "File uploaded");
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        DocumentResponseDto meta = documentService.findById(id);
        assertOwnsDocument(principal, meta);
        Resource resource = documentService.loadAsResource(id);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + meta.fileName() + "\"")
                .body(resource);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        assertOwnsDocument(principal, documentService.findById(id));
        documentService.delete(id);
        return ApiResponse.ok(null, "Document deleted");
    }

    private void assertOwnsDocument(UserPrincipal principal, DocumentResponseDto dto) {
        if (!ownershipService.isAdmin(principal) && !dto.uploadedByUserId().equals(principal.getId())) {
            throw new AccessDeniedException("You can only access your own documents");
        }
    }
}
