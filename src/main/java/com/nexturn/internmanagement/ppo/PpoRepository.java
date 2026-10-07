package com.nexturn.internmanagement.ppo;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PpoRepository extends JpaRepository<Ppo, Long> {
    List<Ppo> findByMentorId(Long mentorId);

    Optional<Ppo> findByInternId(Long internId);

    long countByStatus(PpoStatus status);
}
