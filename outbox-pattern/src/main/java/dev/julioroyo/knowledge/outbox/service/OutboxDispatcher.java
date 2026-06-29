package dev.julioroyo.knowledge.outbox.service;

import dev.julioroyo.knowledge.outbox.model.OutboxEvent;
import dev.julioroyo.knowledge.outbox.repository.OutboxRepository;
import java.time.Duration;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Delivers a single already-claimed event, asynchronously.
 *
 * <p>Kept in its own bean on purpose: {@code @Async} only takes effect through
 * the Spring proxy, so it must be invoked from <em>another</em> bean
 * ({@link OutboxRelay}). Calling an {@code @Async} method from within the same
 * class would silently run it synchronously — a classic Spring trap this split
 * avoids.
 */
@Component
public class OutboxDispatcher {

    private static final Logger log = LoggerFactory.getLogger(OutboxDispatcher.class);

    private static final int MAX_ATTEMPTS = 5;
    private static final Duration BASE_BACKOFF = Duration.ofSeconds(10);

    private final OutboxRepository repository;
    private final NotificationSender sender;

    public OutboxDispatcher(OutboxRepository repository, NotificationSender sender) {
        this.repository = repository;
        this.sender = sender;
    }

    /**
     * Sends an event that the relay has already moved to {@code PROCESSING}. On
     * success it becomes {@code SENT}; on failure it goes back to {@code PENDING}
     * with an exponential-backoff next-attempt time, or to {@code FAILED} once
     * the retry budget is spent.
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

    /** Exponential backoff: 10s, 20s, 40s, 80s ... capped by the attempt budget. */
    private Duration backoffFor(int attempts) {
        return BASE_BACKOFF.multipliedBy(1L << attempts);
    }
}
