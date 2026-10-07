package com.nexturn.internmanagement.document;

import java.time.LocalDateTime;

public record DocumentResponseDto(Long id, String fileName, String fileUrl, String documentType,
        Long uploadedByUserId, String uploadedByName, LocalDateTime uploadDate) {
    public static DocumentResponseDto from(Document d) {
        return new DocumentResponseDto(d.getId(), d.getFileName(), d.getFileUrl(),
                d.getDocumentType().name().replace('_', ' '), d.getUploadedBy().getId(),
                d.getUploadedBy().getName(), d.getUploadDate());
    }
}
