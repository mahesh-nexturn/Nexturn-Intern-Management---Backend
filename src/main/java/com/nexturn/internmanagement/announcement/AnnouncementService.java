package com.nexturn.internmanagement.announcement;

import com.nexturn.internmanagement.common.ResourceNotFoundException;
import com.nexturn.internmanagement.user.Role;
import com.nexturn.internmanagement.user.User;
import com.nexturn.internmanagement.user.UserRepository;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private final UserRepository userRepository;

    public List<AnnouncementResponseDto> findAll() {
        return announcementRepository.findAll().stream().map(AnnouncementResponseDto::from).toList();
    }

    /** Returns announcements targeted to the given role or to "All", matching current frontend behavior. */
    public List<AnnouncementResponseDto> findForRole(Role role) {
        TargetAudience audience = switch (role) {
            case ADMIN -> TargetAudience.HR;
            case MENTOR -> TargetAudience.Mentor;
            case INTERN -> TargetAudience.Intern;
        };
        return announcementRepository
                .findByTargetAudienceInOrderByPublishDateDesc(List.of(TargetAudience.All, audience))
                .stream().map(AnnouncementResponseDto::from).toList();
    }

    public AnnouncementResponseDto findById(Long id) {
        return AnnouncementResponseDto.from(getEntity(id));
    }

    public Announcement getEntity(Long id) {
        return announcementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Announcement not found: " + id));
    }

    public AnnouncementResponseDto create(AnnouncementRequestDto dto, Long createdByUserId) {
        Announcement announcement = new Announcement();
        applyDto(announcement, dto);
        User creator = userRepository.findById(createdByUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + createdByUserId));
        announcement.setCreatedBy(creator);
        return AnnouncementResponseDto.from(announcementRepository.save(announcement));
    }

    public AnnouncementResponseDto update(Long id, AnnouncementRequestDto dto) {
        Announcement announcement = getEntity(id);
        applyDto(announcement, dto);
        return AnnouncementResponseDto.from(announcementRepository.save(announcement));
    }

    public void delete(Long id) {
        announcementRepository.delete(getEntity(id));
    }

    public AnnouncementStatsDto stats() {
        List<Announcement> all = announcementRepository.findAll();
        LocalDate today = LocalDate.now();
        long active = all.stream()
                .filter(a -> a.getExpiryDate() == null || !a.getExpiryDate().isBefore(today))
                .count();
        return new AnnouncementStatsDto(all.size(), active, all.size() - active);
    }

    private void applyDto(Announcement announcement, AnnouncementRequestDto dto) {
        announcement.setTitle(dto.title());
        announcement.setDescription(dto.description());
        announcement.setPublishDate(dto.publishDate());
        announcement.setExpiryDate(dto.expiryDate());
        announcement.setPriority(com.nexturn.internmanagement.task.Priority.valueOf(dto.priority()));
        announcement.setTargetAudience(TargetAudience.valueOf(dto.targetAudience()));
    }
}
