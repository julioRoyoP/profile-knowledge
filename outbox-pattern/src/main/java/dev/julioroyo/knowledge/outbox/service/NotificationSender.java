package dev.julioroyo.knowledge.outbox.service;

import dev.julioroyo.knowledge.outbox.model.OutboxEvent;

/**
 * Abstracción sobre "el sistema externo lento y potencialmente inestable" — aquí
 * un canal de notificación. El relay depende solo de este contrato, así que
 * sustituir el stub que solo loguea por un proveedor real de email/SMS/push no
 * requiere ningún cambio en la maquinaria de reintentos/backoff.
 */
public interface NotificationSender {

    /**
     * Entrega el evento; debe lanzar una excepción si falla, para que el relay
     * aplique backoff y reintente. Conviene que las implementaciones sean
     * idempotentes (entrega at-least-once).
     */
    void send(OutboxEvent event);
}
