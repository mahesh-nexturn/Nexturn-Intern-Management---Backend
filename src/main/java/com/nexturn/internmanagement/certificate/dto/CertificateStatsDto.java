package com.nexturn.internmanagement.certificate.dto;

public record CertificateStatsDto(long total, long active, long expired, long issuedThisMonth) {
}
