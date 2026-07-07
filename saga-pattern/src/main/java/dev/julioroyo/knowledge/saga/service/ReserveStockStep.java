package dev.julioroyo.knowledge.saga.service;

import dev.julioroyo.knowledge.saga.model.SagaContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Primer paso: reservar inventario para el pedido.
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
        context.put("stockReserved", true);
        log.info("Reserved stock for order {}", context.order().id());
    }

    @Override
    public void compensate(SagaContext context) {
        context.find("stockReserved")
                .ifPresent(reserved -> log.info("Released reserved stock for order {}", context.order().id()));
    }
}
