package dev.julioroyo.knowledge.outbox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Example {@link NotificationSender} that just logs the delivery. Keeps the demo
 * self-contained: no SMTP, no provider SDK, no credentials. A real
 * implementation would wrap {@code JavaMailSender}, an SMS gateway, etc.
 */
@Component
public class LoggingNotificationSender implements NotificationSender {

    private static final Logger log = LoggerFactory.getLogger(LoggingNotificationSender.class);

    @Override
    public void send(OutboxEvent event) {
        log.info("Delivered notification [{}] for event {} (payload: {})",
                event.type(), event.id(), event.payload());
    }
}
