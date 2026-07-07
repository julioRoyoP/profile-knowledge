package dev.julioroyo.knowledge.saga.model;

import java.time.Instant;

/**
 * Entrada de auditoría inmutable: una fila por cada transición de estado de un
 * paso de una saga. En un sistema real esto mapea a una tabla de base de datos
 * que sobrevive a los reinicios.
 */
public record SagaStepRecord(String sagaId, String stepName, StepStatus status, Instant at) {}
