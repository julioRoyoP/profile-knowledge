package dev.julioroyo.knowledge.saga.model;

/**
 * Lifecycle of a single saga step, as persisted by the saga state repository.
 *
 * <p>Keeping a durable trail of these transitions is what lets a real saga be
 * resumed or audited after a crash: on restart you know exactly which steps
 * completed and therefore which compensations still owe to run.
 */
public enum StepStatus {
    STARTED,
    COMPLETED,
    FAILED,
    COMPENSATED,
    COMPENSATION_FAILED
}
