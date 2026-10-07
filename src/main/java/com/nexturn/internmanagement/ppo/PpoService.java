package com.nexturn.internmanagement.ppo;

import com.nexturn.internmanagement.common.ResourceNotFoundException;
import com.nexturn.internmanagement.intern.Intern;
import com.nexturn.internmanagement.intern.InternRepository;
import com.nexturn.internmanagement.mentor.Mentor;
import com.nexturn.internmanagement.mentor.MentorRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PpoService {

    private final PpoRepository ppoRepository;
    private final InternRepository internRepository;
    private final MentorRepository mentorRepository;

    public List<PpoResponseDto> findAll() {
        return ppoRepository.findAll().stream().map(PpoResponseDto::from).toList();
    }

    public List<PpoResponseDto> findByMentor(Long mentorId) {
        return ppoRepository.findByMentorId(mentorId).stream().map(PpoResponseDto::from).toList();
    }

    public PpoResponseDto findById(Long id) {
        return PpoResponseDto.from(getEntity(id));
    }

    public PpoResponseDto findByInternId(Long internId) {
        return ppoRepository.findByInternId(internId)
                .map(PpoResponseDto::from)
                .orElseThrow(() -> new ResourceNotFoundException("PPO record not found for intern: " + internId));
    }

    public PpoResponseDto findByUserId(Long userId) {
        Intern intern = internRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Intern not found for user: " + userId));
        return findByInternId(intern.getId());
    }

    public Ppo getEntity(Long id) {
        return ppoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PPO record not found: " + id));
    }

    public PpoResponseDto create(PpoRequestDto dto) {
        Ppo ppo = new Ppo();
        applyDto(ppo, dto);
        return PpoResponseDto.from(ppoRepository.save(ppo));
    }

    public PpoResponseDto update(Long id, PpoRequestDto dto) {
        Ppo ppo = getEntity(id);
        applyDto(ppo, dto);
        return PpoResponseDto.from(ppoRepository.save(ppo));
    }

    public void delete(Long id) {
        ppoRepository.delete(getEntity(id));
    }

    public PpoStatsDto stats() {
        List<Ppo> all = ppoRepository.findAll();
        long eligible = ppoRepository.countByStatus(PpoStatus.Eligible);
        long notEligible = ppoRepository.countByStatus(PpoStatus.Not_Eligible);
        long offered = ppoRepository.countByStatus(PpoStatus.Offered);
        return new PpoStatsDto(all.size(), eligible, notEligible, offered);
    }

    private void applyDto(Ppo ppo, PpoRequestDto dto) {
        Intern intern = internRepository.findById(dto.internId())
                .orElseThrow(() -> new ResourceNotFoundException("Intern not found: " + dto.internId()));
        ppo.setIntern(intern);
        if (dto.mentorId() != null) {
            Mentor mentor = mentorRepository.findById(dto.mentorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Mentor not found: " + dto.mentorId()));
            ppo.setMentor(mentor);
        }
        ppo.setAttendancePct(dto.attendancePct());
        ppo.setTrainingCompletionPct(dto.trainingCompletionPct());
        ppo.setTechnicalScore(dto.technicalScore());
        ppo.setCommunicationScore(dto.communicationScore());
        ppo.setOverallScore(dto.overallScore());
        ppo.setMentorRecommendation(
                dto.mentorRecommendation() == null ? null : RecommendationFlag.valueOf(dto.mentorRecommendation()));
        ppo.setHrRecommendation(
                dto.hrRecommendation() == null ? null : RecommendationFlag.valueOf(dto.hrRecommendation()));
        ppo.setStatus(PpoStatus.valueOf(
                dto.status() == null ? "Eligible" : dto.status().replace(' ', '_')));
    }
}
