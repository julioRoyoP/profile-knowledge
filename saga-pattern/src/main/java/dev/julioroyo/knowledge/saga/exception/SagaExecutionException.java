package dev.julioroyo.knowledge.saga.exception;

/**
 * Thrown by the saga orchestrator after a step failed <em>and</em> the
 * already-executed steps have been compensated. It signals to the caller that
 * the whole saga was rolled back, naming the step that triggered it.
 */
public class SagaExecutionException extends RuntimeException {

    private final String sagaId;
    private final String failedStep;

    public SagaExecutionException(String sagaId, String failedStep, Throwable cause) {
        super("Saga %s rolled back after step '%s' failed".formatted(sagaId, failedStep), cause);
        this.sagaId = sagaId;
        this.failedStep = failedStep;
    }

    public String sagaId() {
        return sagaId;
    }

    public String failedStep() {
        return failedStep;
    }
}
