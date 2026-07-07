package dev.julioroyo.knowledge.outbox.repository;

import dev.julioroyo.knowledge.outbox.model.OutboxEvent;
import dev.julioroyo.knowledge.outbox.model.OutboxStatus;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

/**
 * Sustituto en memoria de la tabla outbox. En producción, reclamar los eventos
 * vencidos sería una sentencia atómica (UPDATE ... FOR UPDATE SKIP LOCKED) para que
 * instancias concurrentes del relay no tomen la misma fila.
 */
@Repository
public class OutboxRepository {

    private final Map<String, OutboxEvent> events = new ConcurrentHashMap<>();

    public OutboxEvent save(OutboxEvent event) {
        events.put(event.id(), event);
        return event;
    }

    /** Eventos pendientes cuya ventana de backoff ya ha vencido, del más antiguo al más reciente. */
    public List<OutboxEvent> findDue(Instant now, int limit) {
        return events.values().stream()
                .filter(event -> event.status() == OutboxStatus.PENDING)
                .filter(event -> !event.nextAttemptAt().isAfter(now))
                .sorted(Comparator.comparing(OutboxEvent::nextAttemptAt))
                .limit(limit)
                .toList();
    }

    /** Todos los eventos almacenados; útil para inspeccionar el estado en la demo. */
    public List<OutboxEvent> findAll() {
        return List.copyOf(events.values());
    }
}
