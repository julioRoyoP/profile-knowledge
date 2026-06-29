package dev.julioroyo.knowledge.outbox.service;

import dev.julioroyo.knowledge.outbox.model.OutboxEvent;
import dev.julioroyo.knowledge.outbox.repository.OutboxRepository;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Polls the outbox on a schedule and hands due events to the async dispatcher.
 *
 * <p>The "claim then dispatch" split is what prevents duplicate sends: this
 * method synchronously moves each event to {@code PROCESSING} and persists it
 * <em>before</em> the async dispatch runs, so the next scheduler tick (or a
 * second instance) no longer sees it as {@code PENDING} and won't pick it up
 * again.
 */
@Component
public class OutboxRelay {

    private static final Logger log = LoggerFactory.getLogger(OutboxRelay.class);

    private static final int BATCH_SIZE = 100;

    private final OutboxRepository repository;
    private final OutboxDispatcher dispatcher;

    public OutboxRelay(OutboxRepository repository, OutboxDispatcher dispatcher) {
        this.repository = repository;
        this.dispatcher = dispatcher;
    }

    @Scheduled(fixedDelay = 5_000)
    public void dispatchPending() {
        List<OutboxEvent> due = repository.findDue(Instant.now(), BATCH_SIZE);
        if (due.isEmpty()) {
            return;
        }
        log.debug("Relay picked up {} due event(s)", due.size());
        due.forEach(this::claimAndDispatch);
    }

    private void claimAndDispatch(OutboxEvent event) {
        event.markProcessing();
        repository.save(event);
        dispatcher.dispatch(event);
    }
}
