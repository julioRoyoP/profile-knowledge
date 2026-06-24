package dev.julioroyo.knowledge.outbox;

/**
 * Abstraction over "the slow, possibly flaky external system" — here a
 * notification channel. The relay depends only on this contract, so swapping a
 * logging stub for a real email/SMS/push provider needs no change to the
 * retry/backoff machinery.
 */
public interface NotificationSender {

    /**
     * Delivers the event. Must throw if delivery failed, so the relay can apply
     * backoff and retry. Implementations should be idempotent where possible, as
     * at-least-once delivery means an event may be re-sent after an ambiguous
     * failure.
     */
    void send(OutboxEvent event);
}
