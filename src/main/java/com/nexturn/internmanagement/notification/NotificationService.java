package com.nexturn.internmanagement.notification;

import com.nexturn.internmanagement.common.ResourceNotFoundException;
import com.nexturn.internmanagement.user.Role;
import com.nexturn.internmanagement.user.User;
import com.nexturn.internmanagement.user.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public List<NotificationResponseDto> findForRole(Role role) {
        NotificationRecipient recipient = switch (role) {
            case ADMIN -> NotificationRecipient.HR;
            case MENTOR -> NotificationRecipient.Mentor;
            case INTERN -> NotificationRecipient.Intern;
        };
        return notificationRepository
                .findByRecipientInOrderByNotificationDateDesc(List.of(NotificationRecipient.All, recipient))
                .stream().map(NotificationResponseDto::from).toList();
    }

    public NotificationResponseDto findById(Long id) {
        return NotificationResponseDto.from(getEntity(id));
    }

    public Notification getEntity(Long id) {
        return notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found: " + id));
    }

    public NotificationResponseDto create(NotificationRequestDto dto, Long createdByUserId) {
        User creator = userRepository.findById(createdByUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + createdByUserId));
        Notification notification = Notification.builder()
                .title(dto.title())
                .message(dto.message())
                .recipient(NotificationRecipient.valueOf(dto.recipient()))
                .createdBy(creator)
                .notificationDate(LocalDateTime.now())
                .status(NotificationStatus.Unread)
                .build();
        return NotificationResponseDto.from(notificationRepository.save(notification));
    }

    public NotificationResponseDto update(Long id, NotificationRequestDto dto) {
        Notification notification = getEntity(id);
        notification.setTitle(dto.title());
        notification.setMessage(dto.message());
        notification.setRecipient(NotificationRecipient.valueOf(dto.recipient()));
        return NotificationResponseDto.from(notificationRepository.save(notification));
    }

    public NotificationResponseDto markRead(Long id) {
        Notification notification = getEntity(id);
        notification.setStatus(NotificationStatus.Read);
        return NotificationResponseDto.from(notificationRepository.save(notification));
    }

    public void delete(Long id) {
        notificationRepository.delete(getEntity(id));
    }
}
