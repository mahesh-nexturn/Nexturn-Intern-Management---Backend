package com.nexturn.internmanagement.certificate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record CertificateRequestDto(
        @NotNull Long internId,
        Long mentorId,
        @NotBlank String certificateName,
        String issuedBy,
        LocalDate issueDate,
        LocalDate expiryDate,
        String status) {
}
