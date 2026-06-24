package dev.julioroyo.knowledge.outbox;

import java.time.Instant;

/**
 * One pending side effect, persisted in the same local transaction as the
 * business change that produced it.
 *
 * <p>Modeled here as a mutable entity (not a record) because its status, attempt
 * count and next-attempt time evolve as the relay retries it — exactly what a
 * JPA {@code @Entity} would hold. A real entity would carry {@code @Id},
 * {@code @Version} (optimistic lock) and column mappings; those are omitted to
 * keep the focus on the pattern.
 */
public class OutboxEvent {

    private final String id;
    private final String type;
    private final String payload;

    private OutboxStatus status = OutboxStatus.PENDING;
    private int attempts = 0;
    private Instant nextAttemptAt = Instant.now();

    public OutboxEvent(String id, String type, String payload) {
        this.id = id;
        this.type = type;
        this.payload = payload;
    }

    public String id() {
        return id;
    }

    public String type() {
        return type;
    }

    public String payload() {
        return payload;
    }

    public OutboxStatus status() {
        return status;
    }

    public int attempts() {
        return attempts;
    }

    public Instant nextAttemptAt() {
        return nextAttemptAt;
    }

    /** Atomically claim this event for an in-flight dispatch. */
    public void markProcessing() {
        this.status = OutboxStatus.PROCESSING;
    }

    public void markSent() {
        this.status = OutboxStatus.SENT;
    }

    /**
     * Record a failed attempt: increment the counter and schedule the next try
     * with exponential backoff, or give up once the budget is exhausted.
     */
    public void markRetry(int maxAttempts, Instant nextAttemptAt) {
        this.attempts++;
        if (attempts >= maxAttempts) {
            this.status = OutboxStatus.FAILED;
        } else {
            this.status = OutboxStatus.PENDING;
            this.nextAttemptAt = nextAttemptAt;
        }
    }
}
