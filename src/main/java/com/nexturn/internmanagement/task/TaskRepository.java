package com.nexturn.internmanagement.task;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByInternId(Long internId);

    List<Task> findByMentorId(Long mentorId);

    long countByStatus(TaskStatus status);

    long countByInternIdAndStatus(Long internId, TaskStatus status);
}
