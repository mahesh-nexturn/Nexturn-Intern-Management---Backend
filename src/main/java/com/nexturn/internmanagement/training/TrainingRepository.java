package com.nexturn.internmanagement.training;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrainingRepository extends JpaRepository<Training, Long> {
    List<Training> findByInternId(Long internId);

    List<Training> findByMentorId(Long mentorId);

    long countByStatus(TrainingStatus status);
}
