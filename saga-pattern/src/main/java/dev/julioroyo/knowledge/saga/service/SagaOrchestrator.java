package dev.julioroyo.knowledge.saga.service;

import dev.julioroyo.knowledge.saga.exception.SagaExecutionException;
import dev.julioroyo.knowledge.saga.model.SagaContext;
import dev.julioroyo.knowledge.saga.model.StepStatus;
import dev.julioroyo.knowledge.saga.repository.SagaStateRepository;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Corazón de la demo: ejecuta una lista ordenada de pasos (SagaStep) y, si alguno
 * falla, compensa en orden inverso los pasos ya completados. Es la alternativa de
 * compensación manual a una transacción distribuida 2PC. Los pasos exitosos se
 * apilan en un Deque para deshacerlos en orden LIFO.
 */
@Service
public class SagaOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(SagaOrchestrator.class);

    private final SagaStateRepository stateRepository;

    public SagaOrchestrator(SagaStateRepository stateRepository) {
        this.stateRepository = stateRepository;
    }

    /**
     * Ejecuta los pasos en orden. Ante el primer fallo, compensa los pasos ya
     * completados en orden inverso y relanza como SagaExecutionException.
     */
    public void execute(String sagaId, List<SagaStep> steps, SagaContext context) {
        Deque<SagaStep> executed = new ArrayDeque<>();

        for (SagaStep step : steps) {
            try {
                stateRepository.record(sagaId, step.name(), StepStatus.STARTED);
                step.execute(context);
                stateRepository.record(sagaId, step.name(), StepStatus.COMPLETED);
                executed.push(step);
            } catch (RuntimeException e) {
                log.error("Saga {} failed at step '{}': {}", sagaId, step.name(), e.getMessage(), e);
                stateRepository.record(sagaId, step.name(), StepStatus.FAILED);
                compensate(sagaId, executed, context);
                throw new SagaExecutionException(sagaId, step.name(), e);
            }
        }

        log.info("Saga {} completed successfully", sagaId);
    }

    /**
     * Deshace los pasos completados empezando por el más reciente. Una compensación
     * que falla se registra como COMPENSATION_FAILED pero no aborta el resto del
     * rollback.
     */
    private void compensate(String sagaId, Deque<SagaStep> executed, SagaContext context) {
        while (!executed.isEmpty()) {
            SagaStep step = executed.pop();
            try {
                step.compensate(context);
                stateRepository.record(sagaId, step.name(), StepStatus.COMPENSATED);
            } catch (RuntimeException e) {
                log.error("Saga {} compensation failed for step '{}': {}", sagaId, step.name(), e.getMessage(), e);
                stateRepository.record(sagaId, step.name(), StepStatus.COMPENSATION_FAILED);
            }
        }
    }
}
