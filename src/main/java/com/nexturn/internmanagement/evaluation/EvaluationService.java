package com.nexturn.internmanagement.evaluation;
import com.nexturn.internmanagement.evaluation.dto.EvaluationRequestDto;
import com.nexturn.internmanagement.evaluation.dto.EvaluationResponseDto;
import com.nexturn.internmanagement.evaluation.dto.EvaluationStatsDto;

import com.nexturn.internmanagement.common.ResourceNotFoundException;
import com.nexturn.internmanagement.intern.Intern;
import com.nexturn.internmanagement.intern.InternRepository;
import com.nexturn.internmanagement.mentor.Mentor;
import com.nexturn.internmanagement.mentor.MentorRepository;
import java.util.List;
import java.util.function.Predicate;
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
        return stats(id -> true);
    }

    public EvaluationStatsDto stats(Predicate<Long> internScope) {
        List<Evaluation> all = evaluationRepository.findAll().stream()
                .filter(e -> e.getIntern() != null && internScope.test(e.getIntern().getId())).toList();
        if (all.isEmpty()) {
            return new EvaluationStatsDto(0, 0, 0, 0, 0, 0, 0);
        }
        return new EvaluationStatsDto(
                avg(all, Evaluation::getRequirementUnderstanding),
                avg(all, Evaluation::getTechnicalKnowledge),
                avg(all, Evaluation::getWorkQuality),
                avg(all, Evaluation::getTaskCompletion),
                avg(all, Evaluation::getTaskDemonstration),
                all.stream().mapToInt(Evaluation::getOverallRating).average().orElse(0),
                all.size());
    }

    private static double avg(List<Evaluation> items, java.util.function.Function<Evaluation, Short> getter) {
        return items.stream().map(getter).filter(java.util.Objects::nonNull).mapToInt(Short::intValue).average()
                .orElse(0);
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
        evaluation.setTaskId(dto.taskId());
        evaluation.setRequirementUnderstanding(dto.requirementUnderstanding());
        evaluation.setTechnicalKnowledge(dto.technicalKnowledge());
        evaluation.setWorkQuality(dto.workQuality());
        evaluation.setTaskCompletion(dto.taskCompletion());
        evaluation.setTaskDemonstration(dto.taskDemonstration());
        int total = dto.requirementUnderstanding() + dto.technicalKnowledge() + dto.workQuality()
                + dto.taskCompletion() + dto.taskDemonstration();
        evaluation.setOverallRating((short) Math.round(total / 5.0f));
        evaluation.setFeedback(dto.feedback());
    }
}
