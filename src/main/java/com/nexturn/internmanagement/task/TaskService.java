package com.nexturn.internmanagement.task;
import com.nexturn.internmanagement.task.dto.TaskRequestDto;
import com.nexturn.internmanagement.task.dto.TaskResponseDto;
import com.nexturn.internmanagement.task.dto.TaskStatsDto;
import com.nexturn.internmanagement.task.dto.TaskWorkUpdateDto;

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

    public TaskResponseDto updateWork(Long id, TaskWorkUpdateDto dto, boolean canEditDescription) {
        Task task = getEntity(id);
        if (dto.status() != null && !dto.status().isBlank()) {
            task.setStatus(TaskStatus.valueOf(dto.status().replace(' ', '_')));
        }
        if (dto.progress() != null) {
            task.setProgress(dto.progress());
        }
        if (canEditDescription && dto.description() != null) {
            task.setDescription(dto.description());
        }
        if (canEditDescription && dto.title() != null && !dto.title().isBlank()) {
            task.setTitle(dto.title().trim());
        }
        return TaskResponseDto.from(taskRepository.save(task));
    }

    public void delete(Long id) {
        taskRepository.delete(getEntity(id));
    }

    public TaskStatsDto stats() {
        return stats(id -> true);
    }

    public TaskStatsDto stats(Predicate<Long> internScope) {
        List<Task> scoped = taskRepository.findAll().stream()
                .filter(t -> t.getIntern() != null && internScope.test(t.getIntern().getId())).toList();
        return new TaskStatsDto(
                scoped.stream().filter(t -> t.getStatus() == TaskStatus.Pending).count(),
                scoped.stream().filter(t -> t.getStatus() == TaskStatus.In_Progress).count(),
                scoped.stream().filter(t -> t.getStatus() == TaskStatus.Completed).count());
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
        task.setStatus(TaskStatus.valueOf((dto.status() == null ? "Not_Started" : dto.status()).replace(' ', '_')));
        task.setProgress(dto.progress() == null ? 0 : dto.progress());
    }
}
