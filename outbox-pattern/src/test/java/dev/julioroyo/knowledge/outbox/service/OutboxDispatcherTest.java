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
 * Lleva al dispatcher por los tres resultados de entrega que definen el patrón:
 * un envío limpio, un fallo transitorio que se reintenta y luego tiene éxito, y un
 * fallo permanente que agota el presupuesto de reintentos. El canal externo se
 * mockea para poder simular los fallos de forma determinista.
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
        // El primer intento falla (proveedor inestable), el segundo pasa.
        doThrow(new RuntimeException("smtp timeout")).doNothing().when(sender).send(event);

        dispatcher.dispatch(event);

        // Vuelve a PENDING con un próximo intento con backoff en lugar de perderse.
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

        // Cinco intentos es el presupuesto configurado; el quinto lo pasa a FAILED terminal.
        for (int attempt = 0; attempt < 5; attempt++) {
            dispatcher.dispatch(event);
        }

        assertThat(event.status()).isEqualTo(OutboxStatus.FAILED);
        assertThat(event.attempts()).isEqualTo(5);
    }
}
