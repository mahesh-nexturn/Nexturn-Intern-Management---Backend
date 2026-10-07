package com.nexturn.internmanagement.certificate;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CertificateRepository extends JpaRepository<Certificate, Long> {
    List<Certificate> findByInternId(Long internId);

    List<Certificate> findByMentorId(Long mentorId);

    long countByIssueDateBetween(LocalDate from, LocalDate to);
}
