package dev.julioroyo.knowledge.saga.model;

/**
 * Ciclo de vida de un único paso de saga, tal como lo persiste el repositorio de
 * estado. El rastro durable de estas transiciones permite auditar o reanudar la
 * saga tras una caída.
 */
public enum StepStatus {
    STARTED,
    COMPLETED,
    FAILED,
    COMPENSATED,
    COMPENSATION_FAILED
}
