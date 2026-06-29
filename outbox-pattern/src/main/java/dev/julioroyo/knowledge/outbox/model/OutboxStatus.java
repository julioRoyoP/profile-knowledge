package dev.julioroyo.knowledge.outbox.model;

/**
 * Lifecycle of an outbox event.
 *
 * <p>The {@code PROCESSING} state is the key to <em>not</em> sending the same
 * notification twice: the relay atomically moves an event from {@code PENDING}
 * to {@code PROCESSING} before dispatching it, so a second scheduler tick (or a
 * second instance) skips events already claimed by another in-flight attempt.
 */
public enum OutboxStatus {
    /** Persisted, waiting to be picked up by the relay. */
    PENDING,
    /** Claimed by a relay run and currently being dispatched. */
    PROCESSING,
    /** Successfully delivered; terminal. */
    SENT,
    /** Exhausted the retry budget; terminal, needs manual or dead-letter handling. */
    FAILED
}
