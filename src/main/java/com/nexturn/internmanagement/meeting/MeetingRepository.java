package com.nexturn.internmanagement.meeting;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeetingRepository extends JpaRepository<Meeting, Long> {
    List<Meeting> findByInternId(Long internId);

    List<Meeting> findByMentorId(Long mentorId);

    List<Meeting> findByMeetingDateGreaterThanEqualAndStatusOrderByMeetingDateAscMeetingTimeAsc(LocalDate from,
            MeetingStatus status);
}
