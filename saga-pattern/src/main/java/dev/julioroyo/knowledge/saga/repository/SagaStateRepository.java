package dev.julioroyo.knowledge.saga.repository;

import dev.julioroyo.knowledge.saga.model.SagaStepRecord;
import dev.julioroyo.knowledge.saga.model.StepStatus;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

/**
 * Sustituto en memoria del almacén durable en el que se apoya una saga real.
 * Aquí es una lista de solo anexado; lo relevante es que cada transición quede
 * registrada.
 */
@Repository
public class SagaStateRepository {

    private static final Logger log = LoggerFactory.getLogger(SagaStateRepository.class);

    private final List<SagaStepRecord> records = new CopyOnWriteArrayList<>();

    public void record(String sagaId, String stepName, StepStatus status) {
        records.add(new SagaStepRecord(sagaId, stepName, status, Instant.now()));
        log.debug("Saga {} step '{}' -> {}", sagaId, stepName, status);
    }

    /** Historial de transiciones completo y ordenado de una saga, útil para auditar o reanudar. */
    public List<SagaStepRecord> history(String sagaId) {
        return records.stream()
                .filter(record -> record.sagaId().equals(sagaId))
                .toList();
    }
}
