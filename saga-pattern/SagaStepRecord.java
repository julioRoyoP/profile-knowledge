package dev.julioroyo.knowledge.saga;

import java.time.Instant;

/**
 * Immutable audit entry: one row per state transition of one step of one saga.
 * In a real system this maps to a database table that survives restarts.
 */
public record SagaStepRecord(String sagaId, String stepName, StepStatus status, Instant at) {}
