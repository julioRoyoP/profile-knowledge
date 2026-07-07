package dev.julioroyo.knowledge.outbox.model;

import java.time.Instant;
import lombok.Getter;

/**
 * Un efecto secundario pendiente, persistido en la misma transacción local que el
 * cambio de negocio que lo produjo. Es mutable (no un record) porque su estado,
 * intentos y hora de próximo intento evolucionan a medida que el relay lo reintenta.
 */
@Getter
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

    /** Reclama atómicamente este evento para un dispatch en curso. */
    public void markProcessing() {
        this.status = OutboxStatus.PROCESSING;
    }

    public void markSent() {
        this.status = OutboxStatus.SENT;
    }

    /**
     * Registra un intento fallido: incrementa el contador y programa el siguiente
     * intento con backoff exponencial, o se rinde una vez agotado el presupuesto.
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
