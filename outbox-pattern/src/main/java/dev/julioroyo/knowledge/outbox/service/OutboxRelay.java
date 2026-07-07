package dev.julioroyo.knowledge.outbox.service;

import dev.julioroyo.knowledge.outbox.model.OutboxEvent;
import dev.julioroyo.knowledge.outbox.repository.OutboxRepository;
import java.time.Instant;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Sondea el outbox de forma programada y pasa los eventos vencidos al dispatcher
 * asíncrono. Mover cada evento a PROCESSING y persistirlo de forma síncrona antes
 * del dispatch es lo que evita entregas duplicadas: el siguiente tick (o una
 * segunda instancia) ya no lo ve como PENDING.
 */
@Slf4j
@Component
public class OutboxRelay {

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
        log.debug("Event {} claimed -> PROCESSING", event.id());
        dispatcher.dispatch(event);
    }
}
