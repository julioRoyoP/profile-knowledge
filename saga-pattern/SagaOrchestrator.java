package dev.julioroyo.knowledge.saga;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Heart of the demo: runs an ordered list of {@link SagaStep}s and guarantees
 * that if any step fails, every step that already succeeded is compensated in
 * reverse order — the manual-compensation alternative to a distributed 2PC
 * transaction.
 *
 * <h2>Why a saga and not 2PC?</h2>
 * The steps touch independent systems (inventory, payment gateway, shipping)
 * that do not share a transaction manager and cannot be enrolled in a single
 * XA/two-phase commit. A saga trades atomicity for <em>eventual</em>
 * consistency: each step commits locally and publishes a compensating action,
 * so a late failure unwinds the earlier successes instead of locking three
 * external systems for the duration of the whole checkout.
 *
 * <h2>Why a {@link Deque}?</h2>
 * Successful steps are pushed onto a stack as they complete, so compensation
 * naturally pops them in LIFO order — you always undo the most recent effect
 * first (refund the charge before releasing the stock it paid for).
 */
@Service
public class SagaOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(SagaOrchestrator.class);

    private final SagaStateRepository stateRepository;

    public SagaOrchestrator(SagaStateRepository stateRepository) {
        this.stateRepository = stateRepository;
    }

    /**
     * Executes the steps in order. On the first failure, compensates the
     * already-completed steps in reverse and rethrows as a
     * {@link SagaExecutionException}.
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
     * Unwinds completed steps newest-first. A failing compensation is logged and
     * recorded but does <em>not</em> abort the remaining rollback — a stuck
     * compensation must not strand the other steps in a half-undone state; it is
     * surfaced via {@link StepStatus#COMPENSATION_FAILED} for an operator or a
     * retry job to pick up.
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
