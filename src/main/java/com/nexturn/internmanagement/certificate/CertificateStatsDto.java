package com.nexturn.internmanagement.certificate;

public record CertificateStatsDto(long total, long active, long expired, long issuedThisMonth) {
}
