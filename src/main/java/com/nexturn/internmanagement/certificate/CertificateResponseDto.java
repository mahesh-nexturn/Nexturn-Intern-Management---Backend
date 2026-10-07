package com.nexturn.internmanagement.certificate;

import java.time.LocalDate;

public record CertificateResponseDto(Long id, Long internId, String internName, Long mentorId,
        String certificateName, String issuedBy, LocalDate issueDate, LocalDate expiryDate, String status) {
    public static CertificateResponseDto from(Certificate c) {
        return new CertificateResponseDto(c.getId(), c.getIntern().getId(), c.getIntern().getName(),
                c.getMentor() != null ? c.getMentor().getId() : null, c.getCertificateName(), c.getIssuedBy(),
                c.getIssueDate(), c.getExpiryDate(), c.getStatus().name());
    }
}
