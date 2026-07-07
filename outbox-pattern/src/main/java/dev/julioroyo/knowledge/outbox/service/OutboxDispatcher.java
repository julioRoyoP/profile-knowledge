package dev.julioroyo.knowledge.outbox.service;

import dev.julioroyo.knowledge.outbox.model.OutboxEvent;
import dev.julioroyo.knowledge.outbox.repository.OutboxRepository;
import java.time.Duration;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Entrega de forma asíncrona un único evento ya reclamado. Vive en su propio bean
 * porque @Async solo surte efecto a través del proxy de Spring: debe invocarlo otro
 * bean (OutboxRelay), no un método de la misma clase.
 */
@Slf4j
@Component
public class OutboxDispatcher {

    private static final int MAX_ATTEMPTS = 5;
    private static final Duration BASE_BACKOFF = Duration.ofSeconds(10);

    private final OutboxRepository repository;
    private final NotificationSender sender;

    public OutboxDispatcher(OutboxRepository repository, NotificationSender sender) {
        this.repository = repository;
        this.sender = sender;
    }

    /**
     * Envía un evento que el relay ya movió a PROCESSING. Si tiene éxito pasa a
     * SENT; si falla vuelve a PENDING con próximo intento en backoff exponencial, o
     * a FAILED una vez agotados los reintentos.
     */
    @Async
    public void dispatch(OutboxEvent event) {
        try {
            sender.send(event);
            event.markSent();
            repository.save(event);
            log.debug("Event {} delivered on attempt {}", event.id(), event.attempts() + 1);
        } catch (RuntimeException e) {
            Instant nextAttempt = Instant.now().plus(backoffFor(event.attempts()));
            event.markRetry(MAX_ATTEMPTS, nextAttempt);
            repository.save(event);
            log.warn("Event {} delivery failed (attempt {}/{}), next status {}: {}",
                    event.id(), event.attempts(), MAX_ATTEMPTS, event.status(), e.getMessage());
        }
    }

    /** Backoff exponencial: 10s, 20s, 40s, 80s ... acotado por el número de intentos. */
    private Duration backoffFor(int attempts) {
        return BASE_BACKOFF.multipliedBy(1L << attempts);
    }
}
