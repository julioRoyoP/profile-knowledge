package dev.julioroyo.knowledge.outbox.service;

import dev.julioroyo.knowledge.outbox.model.OutboxEvent;
import dev.julioroyo.knowledge.outbox.repository.OutboxRepository;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Lado productor del outbox: confirma el pedido y escribe el evento outbox en la
 * misma transacción local, de modo que o se confirman ambos o ninguno. La entrega
 * real queda fuera de esta transacción, a cargo de OutboxRelay.
 */
@Slf4j
@Service
public class OrderConfirmationService {

    private final OutboxRepository outbox;

    public OrderConfirmationService(OutboxRepository outbox) {
        this.outbox = outbox;
    }

    @Transactional
    public void confirmOrder(String orderId) {
        // ... aquí se persistiría el cambio de estado del pedido, en esta misma transacción ...

        OutboxEvent event = new OutboxEvent(
                UUID.randomUUID().toString(),
                "ORDER_CONFIRMED",
                "{\"orderId\":\"%s\"}".formatted(orderId));
        outbox.save(event);

        log.info("Order {} confirmed; outbox event {} enqueued", orderId, event.id());
    }
}
