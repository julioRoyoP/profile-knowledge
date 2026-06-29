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
 * In-memory stand-in for the durable store that a real saga relies on.
 *
 * <p>The persistence itself is deliberately trivial (an append-only list) — the
 * point of the demo is <em>that</em> every transition is recorded, not how it is
 * stored. In production this would be a transactional table written in the same
 * local transaction as each step's business effect.
 */
@Repository
public class SagaStateRepository {

    private static final Logger log = LoggerFactory.getLogger(SagaStateRepository.class);

    private final List<SagaStepRecord> records = new CopyOnWriteArrayList<>();

    public void record(String sagaId, String stepName, StepStatus status) {
        records.add(new SagaStepRecord(sagaId, stepName, status, Instant.now()));
        log.debug("Saga {} step '{}' -> {}", sagaId, stepName, status);
    }

    /** Full ordered transition history for one saga, useful for auditing or resume. */
    public List<SagaStepRecord> history(String sagaId) {
        return records.stream()
                .filter(record -> record.sagaId().equals(sagaId))
                .toList();
    }
}
