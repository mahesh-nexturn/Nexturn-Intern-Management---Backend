package com.nexturn.internmanagement.task;

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
public class TaskService {

    private final TaskRepository taskRepository;
    private final InternRepository internRepository;
    private final MentorRepository mentorRepository;

    public List<TaskResponseDto> findAll() {
        return taskRepository.findAll().stream().map(TaskResponseDto::from).toList();
    }

    public List<TaskResponseDto> findByIntern(Long internId) {
        return taskRepository.findByInternId(internId).stream().map(TaskResponseDto::from).toList();
    }

    public List<TaskResponseDto> findByMentor(Long mentorId) {
        return taskRepository.findByMentorId(mentorId).stream().map(TaskResponseDto::from).toList();
    }

    public TaskResponseDto findById(Long id) {
        return TaskResponseDto.from(getEntity(id));
    }

    public Task getEntity(Long id) {
        return taskRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Task not found: " + id));
    }

    public TaskResponseDto create(TaskRequestDto dto) {
        Task task = new Task();
        applyDto(task, dto);
        return TaskResponseDto.from(taskRepository.save(task));
    }

    public TaskResponseDto update(Long id, TaskRequestDto dto) {
        Task task = getEntity(id);
        applyDto(task, dto);
        return TaskResponseDto.from(taskRepository.save(task));
    }

    public void delete(Long id) {
        taskRepository.delete(getEntity(id));
    }

    public TaskStatsDto stats() {
        return new TaskStatsDto(
                taskRepository.countByStatus(TaskStatus.Pending),
                taskRepository.countByStatus(TaskStatus.In_Progress),
                taskRepository.countByStatus(TaskStatus.Completed));
    }

    private void applyDto(Task task, TaskRequestDto dto) {
        Intern intern = internRepository.findById(dto.internId())
                .orElseThrow(() -> new ResourceNotFoundException("Intern not found: " + dto.internId()));
        task.setIntern(intern);
        if (dto.mentorId() != null) {
            Mentor mentor = mentorRepository.findById(dto.mentorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Mentor not found: " + dto.mentorId()));
            task.setMentor(mentor);
        }
        task.setTitle(dto.title());
        task.setDescription(dto.description());
        task.setPriority(Priority.valueOf(dto.priority()));
        task.setDueDate(dto.dueDate());
        task.setStatus(TaskStatus.valueOf((dto.status() == null ? "Pending" : dto.status()).replace(' ', '_')));
        task.setProgress(dto.progress() == null ? 0 : dto.progress());
    }
}
