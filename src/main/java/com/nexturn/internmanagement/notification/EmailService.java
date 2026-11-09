package com.nexturn.internmanagement.notification;

import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/** Sends notification emails over SMTP. Does nothing unless spring.mail.host is configured. */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final ObjectProvider<JavaMailSender> mailSender;
    private final String from;
    private final String host;

    public EmailService(ObjectProvider<JavaMailSender> mailSender,
            @Value("${app.mail.from:}") String from,
            @Value("${spring.mail.host:}") String host) {
        this.mailSender = mailSender;
        this.from = from;
        this.host = host;
    }

    /** Sent asynchronously and never throws: a mail failure must not break the request. Recipients are BCC'd. */
    @Async
    public void send(Collection<String> recipients, String subject, String body) {
        JavaMailSender sender = mailSender.getIfAvailable();
        if (sender == null || host.isBlank() || from.isBlank() || recipients.isEmpty()) {
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(from);
            message.setBcc(recipients.toArray(String[]::new));
            message.setSubject(subject);
            message.setText(body);
            sender.send(message);
        } catch (Exception e) {
            log.warn("Failed to send notification email: {}", e.getMessage());
        }
    }
}
