package dev.julioroyo.knowledge.outbox.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import dev.julioroyo.knowledge.outbox.model.OutboxEvent;
import dev.julioroyo.knowledge.outbox.model.OutboxStatus;
import dev.julioroyo.knowledge.outbox.repository.OutboxRepository;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Drives the dispatcher through the three delivery outcomes that define the
 * pattern: a clean send, a transient failure that is retried and then succeeds,
 * and a permanent failure that exhausts the retry budget. The external channel
 * is mocked so failures can be simulated deterministically.
 */
@ExtendWith(MockitoExtension.class)
class OutboxDispatcherTest {

    private final OutboxRepository repository = new OutboxRepository();

    @Mock private NotificationSender sender;

    private OutboxDispatcher dispatcher;

    @BeforeEach
    void setUp() {
        dispatcher = new OutboxDispatcher(repository, sender);
    }

    @Test
    void shouldMarkEventSentWhenDeliverySucceeds() {
        OutboxEvent event = repository.save(new OutboxEvent("e1", "ORDER_CONFIRMED", "{}"));
        event.markProcessing();

        dispatcher.dispatch(event);

        verify(sender).send(event);
        assertThat(event.status()).isEqualTo(OutboxStatus.SENT);
    }

    @Test
    void shouldRescheduleForRetryWhenDeliveryFailsAndSucceedOnNextAttempt() {
        OutboxEvent event = repository.save(new OutboxEvent("e2", "ORDER_CONFIRMED", "{}"));
        // First attempt fails (flaky provider), second one goes through.
        doThrow(new RuntimeException("smtp timeout")).doNothing().when(sender).send(event);

        dispatcher.dispatch(event);

        // Back to PENDING with a backed-off next attempt instead of being lost.
        assertThat(event.status()).isEqualTo(OutboxStatus.PENDING);
        assertThat(event.attempts()).isEqualTo(1);
        assertThat(event.nextAttemptAt()).isAfter(Instant.now());

        dispatcher.dispatch(event);

        assertThat(event.status()).isEqualTo(OutboxStatus.SENT);
        verify(sender, times(2)).send(event);
    }

    @Test
    void shouldMarkEventFailedWhenRetryBudgetIsExhausted() {
        OutboxEvent event = repository.save(new OutboxEvent("e3", "ORDER_CONFIRMED", "{}"));
        doThrow(new RuntimeException("provider down")).when(sender).send(event);

        // Five attempts is the configured budget; the fifth flips it to terminal FAILED.
        for (int attempt = 0; attempt < 5; attempt++) {
            dispatcher.dispatch(event);
        }

        assertThat(event.status()).isEqualTo(OutboxStatus.FAILED);
        assertThat(event.attempts()).isEqualTo(5);
    }
}
