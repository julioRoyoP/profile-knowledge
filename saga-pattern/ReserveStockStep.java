package dev.julioroyo.knowledge.saga;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * First step: reserve inventory for the order.
 *
 * <p>Note for readers: in the real implementation this step acquires a
 * <em>pessimistic database lock</em> on the affected stock rows so two
 * concurrent checkouts cannot oversell the same unit. That locking detail is
 * intentionally omitted here — it belongs to the inventory module, not to the
 * saga pattern this demo is about.
 */
@Component
public class ReserveStockStep implements SagaStep {

    private static final Logger log = LoggerFactory.getLogger(ReserveStockStep.class);

    @Override
    public String name() {
        return "reserve-stock";
    }

    @Override
    public void execute(SagaContext context) {
        // Real impl: SELECT ... FOR UPDATE on stock rows, then decrement.
        context.put("stockReserved", true);
        log.info("Reserved stock for order {}", context.order().id());
    }

    @Override
    public void compensate(SagaContext context) {
        context.find("stockReserved")
                .ifPresent(reserved -> log.info("Released reserved stock for order {}", context.order().id()));
    }
}
