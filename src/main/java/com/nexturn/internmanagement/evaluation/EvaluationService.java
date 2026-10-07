package com.nexturn.internmanagement.evaluation;

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
public class EvaluationService {

    private final EvaluationRepository evaluationRepository;
    private final InternRepository internRepository;
    private final MentorRepository mentorRepository;

    public List<EvaluationResponseDto> findAll() {
        return evaluationRepository.findAll().stream().map(EvaluationResponseDto::from).toList();
    }

    public List<EvaluationResponseDto> findByIntern(Long internId) {
        return evaluationRepository.findByInternId(internId).stream().map(EvaluationResponseDto::from).toList();
    }

    public List<EvaluationResponseDto> findByMentor(Long mentorId) {
        return evaluationRepository.findByMentorId(mentorId).stream().map(EvaluationResponseDto::from).toList();
    }

    public EvaluationResponseDto findById(Long id) {
        return EvaluationResponseDto.from(getEntity(id));
    }

    public Evaluation getEntity(Long id) {
        return evaluationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evaluation not found: " + id));
    }

    public EvaluationResponseDto create(EvaluationRequestDto dto) {
        Evaluation evaluation = new Evaluation();
        applyDto(evaluation, dto);
        return EvaluationResponseDto.from(evaluationRepository.save(evaluation));
    }

    public EvaluationResponseDto update(Long id, EvaluationRequestDto dto) {
        Evaluation evaluation = getEntity(id);
        applyDto(evaluation, dto);
        return EvaluationResponseDto.from(evaluationRepository.save(evaluation));
    }

    public void delete(Long id) {
        evaluationRepository.delete(getEntity(id));
    }

    public EvaluationStatsDto stats() {
        List<Evaluation> all = evaluationRepository.findAll();
        if (all.isEmpty()) {
            return new EvaluationStatsDto(0, 0, 0, 0, 0);
        }
        double technical = all.stream().mapToInt(Evaluation::getTechnicalRating).average().orElse(0);
        double communication = all.stream().mapToInt(Evaluation::getCommunicationRating).average().orElse(0);
        double problemSolving = all.stream().mapToInt(Evaluation::getProblemSolvingRating).average().orElse(0);
        double overall = all.stream().mapToInt(Evaluation::getOverallRating).average().orElse(0);
        return new EvaluationStatsDto(technical, communication, problemSolving, overall, all.size());
    }

    private void applyDto(Evaluation evaluation, EvaluationRequestDto dto) {
        Intern intern = internRepository.findById(dto.internId())
                .orElseThrow(() -> new ResourceNotFoundException("Intern not found: " + dto.internId()));
        evaluation.setIntern(intern);
        if (dto.mentorId() != null) {
            Mentor mentor = mentorRepository.findById(dto.mentorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Mentor not found: " + dto.mentorId()));
            evaluation.setMentor(mentor);
        }
        evaluation.setTechnicalRating(dto.technicalRating());
        evaluation.setCommunicationRating(dto.communicationRating());
        evaluation.setProblemSolvingRating(dto.problemSolvingRating());
        evaluation.setOverallRating(dto.overallRating());
        evaluation.setFeedback(dto.feedback());
    }
}
