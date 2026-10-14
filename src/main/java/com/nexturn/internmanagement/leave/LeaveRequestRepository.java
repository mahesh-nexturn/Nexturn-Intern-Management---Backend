package com.nexturn.internmanagement.leave;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
    List<LeaveRequest> findByInternIdOrderByCreatedAtDesc(Long internId);

    List<LeaveRequest> findAllByOrderByCreatedAtDesc();

    List<LeaveRequest> findByInternIdAndStatusInAndFromDateLessThanEqualAndToDateGreaterThanEqual(
            Long internId, List<LeaveStatus> statuses, LocalDate to, LocalDate from);
}
