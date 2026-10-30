package com.nexturn.internmanagement.notification;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;

/** Publishes notifications to SNS (email subscribers). No-op when no topic is configured. */
@Component
public class SnsNotificationPublisher {

    private static final Logger log = LoggerFactory.getLogger(SnsNotificationPublisher.class);

    private final Optional<SnsClient> sns;
    private final String topicArn;

    public SnsNotificationPublisher(ObjectProvider<SnsClient> snsProvider,
            @Value("${app.sns.topic-arn:}") String topicArn) {
        this.sns = Optional.ofNullable(snsProvider.getIfAvailable());
        this.topicArn = topicArn;
    }

    public void publish(String title, String message) {
        if (sns.isEmpty() || topicArn.isBlank()) {
            return;
        }
        try {
            String subject = title == null ? "Notification" : title.replaceAll("[\\r\\n]+", " ").trim();
            if (subject.length() > 100) {
                subject = subject.substring(0, 100);
            }
            sns.get().publish(PublishRequest.builder()
                    .topicArn(topicArn)
                    .subject(subject.isEmpty() ? "Notification" : subject)
                    .message(message == null || message.isBlank() ? subject : message)
                    .build());
        } catch (RuntimeException e) {
            // never fail the business operation because email delivery failed
            log.warn("Failed to publish notification to SNS: {}", e.getMessage());
        }
    }
}
