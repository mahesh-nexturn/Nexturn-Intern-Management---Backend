package com.nexturn.internmanagement.certificate;

import com.nexturn.internmanagement.common.ResourceNotFoundException;
import com.nexturn.internmanagement.intern.Intern;
import com.nexturn.internmanagement.intern.InternRepository;
import com.nexturn.internmanagement.mentor.Mentor;
import com.nexturn.internmanagement.mentor.MentorRepository;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CertificateService {

    private final CertificateRepository certificateRepository;
    private final InternRepository internRepository;
    private final MentorRepository mentorRepository;

    public List<CertificateResponseDto> findAll() {
        return certificateRepository.findAll().stream().map(CertificateResponseDto::from).toList();
    }

    public List<CertificateResponseDto> findByIntern(Long internId) {
        return certificateRepository.findByInternId(internId).stream().map(CertificateResponseDto::from).toList();
    }

    public List<CertificateResponseDto> findByMentor(Long mentorId) {
        return certificateRepository.findByMentorId(mentorId).stream().map(CertificateResponseDto::from).toList();
    }

    public CertificateResponseDto findById(Long id) {
        return CertificateResponseDto.from(getEntity(id));
    }

    public Certificate getEntity(Long id) {
        return certificateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Certificate not found: " + id));
    }

    public CertificateResponseDto create(CertificateRequestDto dto) {
        Certificate certificate = new Certificate();
        applyDto(certificate, dto);
        return CertificateResponseDto.from(certificateRepository.save(certificate));
    }

    public CertificateResponseDto update(Long id, CertificateRequestDto dto) {
        Certificate certificate = getEntity(id);
        applyDto(certificate, dto);
        return CertificateResponseDto.from(certificateRepository.save(certificate));
    }

    public void delete(Long id) {
        certificateRepository.delete(getEntity(id));
    }

    public CertificateStatsDto stats() {
        List<Certificate> all = certificateRepository.findAll();
        long active = all.stream().filter(c -> c.getStatus() == CertificateStatus.Active).count();
        long expired = all.stream().filter(c -> c.getStatus() == CertificateStatus.Expired).count();
        YearMonth currentMonth = YearMonth.now();
        long issuedThisMonth = certificateRepository.countByIssueDateBetween(
                currentMonth.atDay(1), currentMonth.atEndOfMonth());
        return new CertificateStatsDto(all.size(), active, expired, issuedThisMonth);
    }

    private void applyDto(Certificate certificate, CertificateRequestDto dto) {
        Intern intern = internRepository.findById(dto.internId())
                .orElseThrow(() -> new ResourceNotFoundException("Intern not found: " + dto.internId()));
        certificate.setIntern(intern);
        if (dto.mentorId() != null) {
            Mentor mentor = mentorRepository.findById(dto.mentorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Mentor not found: " + dto.mentorId()));
            certificate.setMentor(mentor);
        }
        certificate.setCertificateName(dto.certificateName());
        certificate.setIssuedBy(dto.issuedBy());
        certificate.setIssueDate(dto.issueDate());
        certificate.setExpiryDate(dto.expiryDate());
        certificate.setStatus(CertificateStatus.valueOf(dto.status() == null ? "Active" : dto.status()));
    }
}
