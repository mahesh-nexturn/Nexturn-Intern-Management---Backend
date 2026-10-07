package com.nexturn.internmanagement.notification;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByRecipientInOrderByNotificationDateDesc(List<NotificationRecipient> recipients);
}
