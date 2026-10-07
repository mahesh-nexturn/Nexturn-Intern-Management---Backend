package com.nexturn.internmanagement.training;

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
public class TrainingService {

    private final TrainingRepository trainingRepository;
    private final InternRepository internRepository;
    private final MentorRepository mentorRepository;

    public List<TrainingResponseDto> findAll() {
        return trainingRepository.findAll().stream().map(TrainingResponseDto::from).toList();
    }

    public List<TrainingResponseDto> findByIntern(Long internId) {
        return trainingRepository.findByInternId(internId).stream().map(TrainingResponseDto::from).toList();
    }

    public List<TrainingResponseDto> findByMentor(Long mentorId) {
        return trainingRepository.findByMentorId(mentorId).stream().map(TrainingResponseDto::from).toList();
    }

    public TrainingResponseDto findById(Long id) {
        return TrainingResponseDto.from(getEntity(id));
    }

    public Training getEntity(Long id) {
        return trainingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Training not found: " + id));
    }

    public TrainingResponseDto create(TrainingRequestDto dto) {
        Training training = new Training();
        applyDto(training, dto);
        return TrainingResponseDto.from(trainingRepository.save(training));
    }

    public TrainingResponseDto update(Long id, TrainingRequestDto dto) {
        Training training = getEntity(id);
        applyDto(training, dto);
        return TrainingResponseDto.from(trainingRepository.save(training));
    }

    public void delete(Long id) {
        trainingRepository.delete(getEntity(id));
    }

    public TrainingStatsDto stats() {
        return new TrainingStatsDto(
                trainingRepository.countByStatus(TrainingStatus.Not_Started),
                trainingRepository.countByStatus(TrainingStatus.In_Progress),
                trainingRepository.countByStatus(TrainingStatus.Completed));
    }

    private void applyDto(Training training, TrainingRequestDto dto) {
        Intern intern = internRepository.findById(dto.internId())
                .orElseThrow(() -> new ResourceNotFoundException("Intern not found: " + dto.internId()));
        training.setIntern(intern);
        if (dto.mentorId() != null) {
            Mentor mentor = mentorRepository.findById(dto.mentorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Mentor not found: " + dto.mentorId()));
            training.setMentor(mentor);
        }
        training.setTitle(dto.title());
        training.setStartDate(dto.startDate());
        training.setEndDate(dto.endDate());
        training.setStatus(TrainingStatus.valueOf((dto.status() == null ? "Not Started" : dto.status())
                .replace(' ', '_')));
        training.setProgress(dto.progress() == null ? 0 : dto.progress());
    }
}
