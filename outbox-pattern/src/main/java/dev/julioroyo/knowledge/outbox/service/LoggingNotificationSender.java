package dev.julioroyo.knowledge.outbox.service;

import dev.julioroyo.knowledge.outbox.model.OutboxEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * NotificationSender de ejemplo que se limita a loguear la entrega.
 * Mantiene la demo autocontenida: sin SMTP, sin SDK de proveedor, sin
 * credenciales. Una implementación real envolvería JavaMailSender, una
 * pasarela de SMS, etc.
 */
@Slf4j
@Component
public class LoggingNotificationSender implements NotificationSender {

    @Override
    public void send(OutboxEvent event) {
        log.info("Delivered notification [{}] for event {} (payload: {})",
                event.type(), event.id(), event.payload());
    }
}
