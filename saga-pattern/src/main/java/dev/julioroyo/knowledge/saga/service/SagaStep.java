package dev.julioroyo.knowledge.saga.service;

import dev.julioroyo.knowledge.saga.model.SagaContext;

/**
 * A single, reversible step within a saga.
 *
 * <p>Every step knows how to do its work ({@link #execute}) and how to undo it
 * ({@link #compensate}). The orchestrator depends only on this contract, never
 * on concrete steps, so new steps can be added or reordered without touching
 * the orchestration logic.
 */
public interface SagaStep {

    /**
     * Stable, human-readable identifier used when persisting the step state.
     * Prefer a kebab-case business name (e.g. {@code "reserve-stock"}) over the
     * class name, so renaming the class does not break stored saga history.
     */
    String name();

    /**
     * Performs the forward action. Must throw if the action cannot complete, so
     * the orchestrator can trigger compensation of the already-executed steps.
     */
    void execute(SagaContext context);

    /**
     * Undoes whatever {@link #execute} did. Must be idempotent and tolerant of
     * partial state: it may run even if {@code execute} only half-completed.
     */
    void compensate(SagaContext context);
}
