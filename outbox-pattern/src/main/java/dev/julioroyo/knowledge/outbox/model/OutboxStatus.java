package dev.julioroyo.knowledge.outbox.model;

/**
 * Ciclo de vida de un evento outbox. El estado PROCESSING evita enviar la misma
 * notificación dos veces: el relay reclama el evento moviéndolo a PROCESSING antes
 * de despacharlo, de modo que otro tick o instancia se salta los ya reclamados.
 */
public enum OutboxStatus {
    /** Persistido, esperando a que el relay lo recoja. */
    PENDING,
    /** Reclamado por una ejecución del relay y despachándose en este momento. */
    PROCESSING,
    /** Entregado con éxito; terminal. */
    SENT,
    /** Agotado el presupuesto de reintentos; terminal, requiere tratamiento manual o dead-letter. */
    FAILED
}
