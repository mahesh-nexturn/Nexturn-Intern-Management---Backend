package com.nexturn.internmanagement.intern;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InternRepository extends JpaRepository<Intern, Long> {
    List<Intern> findByMentorId(Long mentorId);

    List<Intern> findByHrId(Long hrId);

    Optional<Intern> findByUserId(Long userId);
}
