package com.nexturn.internmanagement.evaluation;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EvaluationRepository extends JpaRepository<Evaluation, Long> {
    List<Evaluation> findByInternId(Long internId);

    List<Evaluation> findByMentorId(Long mentorId);
}
