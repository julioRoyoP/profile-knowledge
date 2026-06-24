package dev.julioroyo.knowledge.outbox;

import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Producer side — the reason the outbox exists.
 *
 * <p>Confirming the order and writing the outbox event happen in the <em>same
 * local transaction</em>. That is the whole point: either both commit or neither
 * does, so the notification can never be lost (event written but order rolled
 * back) nor sent for a phantom order (notification fired but order rolled back).
 * The actual delivery is deliberately left out of this transaction and handled
 * later by {@link OutboxRelay}, so a slow or flaky email provider can never
 * block or roll back the business operation.
 */
@Service
public class OrderConfirmationService {

    private static final Logger log = LoggerFactory.getLogger(OrderConfirmationService.class);

    private final OutboxRepository outbox;

    public OrderConfirmationService(OutboxRepository outbox) {
        this.outbox = outbox;
    }

    @Transactional
    public void confirmOrder(String orderId) {
        // ... persist the order state change here, in this same transaction ...

        OutboxEvent event = new OutboxEvent(
                UUID.randomUUID().toString(),
                "ORDER_CONFIRMED",
                "{\"orderId\":\"%s\"}".formatted(orderId));
        outbox.save(event);

        log.info("Order {} confirmed; outbox event {} enqueued", orderId, event.id());
    }
}
