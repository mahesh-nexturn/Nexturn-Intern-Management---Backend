package com.nexturn.internmanagement.mentor;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MentorRepository extends JpaRepository<Mentor, Long> {
    Optional<Mentor> findByUserId(Long userId);

    java.util.List<Mentor> findByHrId(Long hrId);
}
