package dev.julioroyo.knowledge.outbox.repository;

import dev.julioroyo.knowledge.outbox.model.OutboxEvent;
import dev.julioroyo.knowledge.outbox.model.OutboxStatus;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

/**
 * In-memory stand-in for the outbox table.
 *
 * <p>In a real database, claiming due events would be a single atomic statement
 * such as {@code UPDATE outbox SET status='PROCESSING' WHERE status='PENDING'
 * AND next_attempt_at <= now() ... FOR UPDATE SKIP LOCKED} so that concurrent
 * relay instances never grab the same row. Here the claim is split into "read
 * due" + "mark processing" by the relay, which is enough to show the intent.
 */
@Repository
public class OutboxRepository {

    private final Map<String, OutboxEvent> events = new ConcurrentHashMap<>();

    public OutboxEvent save(OutboxEvent event) {
        events.put(event.id(), event);
        return event;
    }

    /** Pending events whose backoff window has elapsed, oldest first. */
    public List<OutboxEvent> findDue(Instant now, int limit) {
        return events.values().stream()
                .filter(event -> event.status() == OutboxStatus.PENDING)
                .filter(event -> !event.nextAttemptAt().isAfter(now))
                .sorted(Comparator.comparing(OutboxEvent::nextAttemptAt))
                .limit(limit)
                .toList();
    }
}
