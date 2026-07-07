package dev.julioroyo.knowledge.saga.exception;

/**
 * La lanza el orquestador después de que un paso falle y los pasos ya ejecutados
 * hayan sido compensados. Señala que toda la saga se revirtió, nombrando el paso
 * que lo provocó.
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
