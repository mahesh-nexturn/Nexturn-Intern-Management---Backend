package com.nexturn.internmanagement.notification;
import com.nexturn.internmanagement.notification.dto.NotificationRequestDto;
import com.nexturn.internmanagement.notification.dto.NotificationResponseDto;

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
    private final EmailService emailService;

    private void email(User target, NotificationRecipient audience, String title, String message) {
        List<User> users = target != null ? List.of(target) : switch (audience) {
            case HR -> userRepository.findAll().stream()
                    .filter(u -> u.getRole() == Role.ADMIN || u.getRole() == Role.HR).toList();
            case Mentor -> userRepository.findByRole(Role.MENTOR);
            case Intern -> userRepository.findByRole(Role.INTERN);
            case All -> userRepository.findAll();
        };
        emailService.send(users.stream().map(User::getEmail).filter(e -> e != null && !e.isBlank()).distinct().toList(),
                title, message);
    }

    private static NotificationRecipient audienceOf(Role role) {
        return switch (role) {
            case ADMIN, HR -> NotificationRecipient.HR;
            case MENTOR -> NotificationRecipient.Mentor;
            case INTERN -> NotificationRecipient.Intern;
        };
    }

    /** Loads a notification only if it is visible to the given user; otherwise behaves as not found. */
    public Notification getVisibleEntity(Long id, Role role, Long userId) {
        Notification n = getEntity(id);
        boolean audienceMatch = n.getRecipient() == NotificationRecipient.All || n.getRecipient() == audienceOf(role);
        boolean targetMatch = n.getTargetUser() == null || n.getTargetUser().getId().equals(userId);
        if (!audienceMatch || !targetMatch) {
            throw new ResourceNotFoundException("Notification not found: " + id);
        }
        return n;
    }

    public NotificationResponseDto findVisibleById(Long id, Role role, Long userId) {
        return NotificationResponseDto.from(getVisibleEntity(id, role, userId));
    }

    public NotificationResponseDto markReadVisible(Long id, Role role, Long userId) {
        Notification notification = getVisibleEntity(id, role, userId);
        notification.setStatus(NotificationStatus.Read);
        return NotificationResponseDto.from(notificationRepository.save(notification));
    }

    public List<NotificationResponseDto> findForUser(Role role, Long userId) {
        NotificationRecipient recipient = audienceOf(role);
        return notificationRepository
                .findByRecipientInOrderByNotificationDateDesc(List.of(NotificationRecipient.All, recipient))
                .stream()
                .filter(n -> n.getTargetUser() == null || n.getTargetUser().getId().equals(userId))
                .map(NotificationResponseDto::from).toList();
    }

    /** Sends a notification that only {@code target} (or, when null, everyone in {@code audience}) can see. */
    public void notify(User from, User target, NotificationRecipient audience, String title, String message) {
        notify(from, target, audience, title, message, null);
    }

    public void notify(User from, User target, NotificationRecipient audience, String title, String message,
            String link) {
        notificationRepository.save(Notification.builder()
                .title(title)
                .message(message)
                .link(link)
                .recipient(audience)
                .createdBy(from)
                .targetUser(target)
                .notificationDate(LocalDateTime.now())
                .status(NotificationStatus.Unread)
                .build());
        email(target, audience, title, message);
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
        NotificationResponseDto saved = NotificationResponseDto.from(notificationRepository.save(notification));
        email(null, notification.getRecipient(), dto.title(), dto.message());
        return saved;
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
