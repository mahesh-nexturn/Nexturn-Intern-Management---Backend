package com.nexturn.internmanagement.meeting;
import com.nexturn.internmanagement.meeting.dto.MeetingRequestDto;
import com.nexturn.internmanagement.meeting.dto.MeetingResponseDto;

import com.nexturn.internmanagement.common.ResourceNotFoundException;
import com.nexturn.internmanagement.intern.Intern;
import com.nexturn.internmanagement.intern.InternRepository;
import com.nexturn.internmanagement.mentor.Mentor;
import com.nexturn.internmanagement.mentor.MentorRepository;
import com.nexturn.internmanagement.notification.NotificationRecipient;
import com.nexturn.internmanagement.notification.NotificationService;
import com.nexturn.internmanagement.user.User;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MeetingService {

    private final MeetingRepository meetingRepository;
    private final InternRepository internRepository;
    private final MentorRepository mentorRepository;
    private final NotificationService notificationService;

    public List<MeetingResponseDto> findAll() {
        return meetingRepository.findAll().stream().map(MeetingResponseDto::from).toList();
    }

    public List<MeetingResponseDto> findByIntern(Long internId) {
        return meetingRepository.findByInternId(internId).stream().map(MeetingResponseDto::from).toList();
    }

    public List<MeetingResponseDto> findByMentor(Long mentorId) {
        return meetingRepository.findByMentorId(mentorId).stream().map(MeetingResponseDto::from).toList();
    }

    public MeetingResponseDto findById(Long id) {
        return MeetingResponseDto.from(getEntity(id));
    }

    public Meeting getEntity(Long id) {
        return meetingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Meeting not found: " + id));
    }

    public MeetingResponseDto create(MeetingRequestDto dto, User from) {
        Meeting meeting = new Meeting();
        applyDto(meeting, dto);
        Meeting saved = meetingRepository.save(meeting);
        notifyIntern(saved, from, "New meeting scheduled");
        return MeetingResponseDto.from(saved);
    }

    public MeetingResponseDto update(Long id, MeetingRequestDto dto, User from) {
        Meeting meeting = getEntity(id);
        applyDto(meeting, dto);
        Meeting saved = meetingRepository.save(meeting);
        notifyIntern(saved, from, "Meeting updated");
        return MeetingResponseDto.from(saved);
    }

    private void notifyIntern(Meeting meeting, User from, String heading) {
        if (meeting.getIntern() == null || meeting.getIntern().getUser() == null) {
            return;
        }
        User target = meeting.getIntern().getUser();
        if (target.getId().equals(from.getId())) {
            return;
        }
        String when = meeting.getMeetingDate() == null ? ""
                : " on " + meeting.getMeetingDate()
                        + (meeting.getMeetingTime() == null ? "" : " at " + meeting.getMeetingTime());
        notificationService.notify(from, target, NotificationRecipient.Intern,
                heading + ": " + meeting.getTitle(),
                from.getName() + " scheduled a meeting \"" + meeting.getTitle() + "\"" + when + ".");
    }

    public void delete(Long id) {
        meetingRepository.delete(getEntity(id));
    }

    public List<MeetingResponseDto> upcoming() {
        return meetingRepository
                .findByMeetingDateGreaterThanEqualAndStatusOrderByMeetingDateAscMeetingTimeAsc(
                        LocalDate.now(), MeetingStatus.Scheduled)
                .stream().map(MeetingResponseDto::from).toList();
    }

    private void applyDto(Meeting meeting, MeetingRequestDto dto) {
        Intern intern = internRepository.findById(dto.internId())
                .orElseThrow(() -> new ResourceNotFoundException("Intern not found: " + dto.internId()));
        meeting.setIntern(intern);
        if (dto.mentorId() != null) {
            Mentor mentor = mentorRepository.findById(dto.mentorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Mentor not found: " + dto.mentorId()));
            meeting.setMentor(mentor);
        }
        meeting.setTitle(dto.title());
        meeting.setAgenda(dto.agenda());
        meeting.setMeetingDate(dto.meetingDate());
        meeting.setMeetingTime(dto.meetingTime());
        meeting.setStatus(MeetingStatus.valueOf(dto.status() == null ? "Scheduled" : dto.status()));
    }
}
