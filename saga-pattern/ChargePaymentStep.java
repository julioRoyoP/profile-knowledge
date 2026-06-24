package dev.julioroyo.knowledge.saga;

import dev.julioroyo.knowledge.saga.PaymentResult.PaymentFailure;
import dev.julioroyo.knowledge.saga.PaymentResult.PaymentPending;
import dev.julioroyo.knowledge.saga.PaymentResult.PaymentSuccess;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Second step: charge the customer through an external gateway.
 *
 * <p>Shows the {@link PaymentResult} sealed interface consumed with exhaustive
 * pattern matching: the {@code switch} has no {@code default}, so adding a new
 * payment outcome later forces this code to be revisited at compile time.
 */
@Component
public class ChargePaymentStep implements SagaStep {

    private static final Logger log = LoggerFactory.getLogger(ChargePaymentStep.class);

    @Override
    public String name() {
        return "charge-payment";
    }

    @Override
    public void execute(SagaContext context) {
        Order order = context.order();
        PaymentResult result = charge(order);

        switch (result) {
            case PaymentSuccess success -> {
                context.put("transactionId", success.transactionId());
                log.info("Captured {} for order {} (tx {})",
                        success.amount(), order.id(), success.transactionId());
            }
            case PaymentPending pending ->
                throw new IllegalStateException(
                        "Payment is pending settlement (%s); a synchronous saga cannot proceed"
                                .formatted(pending.reference()));
            case PaymentFailure failure ->
                throw new PaymentDeclinedException(failure.reason());
        }
    }

    @Override
    public void compensate(SagaContext context) {
        // Only refund if a charge actually went through.
        context.find("transactionId").ifPresent(transactionId ->
                log.info("Refunding transaction {} for order {}", transactionId, context.order().id()));
    }

    /**
     * Stand-in for the real gateway call. Returns a successful capture; the other
     * {@link PaymentResult} branches above document how declines and pending
     * settlements would be handled.
     */
    private PaymentResult charge(Order order) {
        return new PaymentSuccess("tx-" + UUID.randomUUID(), order.amount());
    }
}
