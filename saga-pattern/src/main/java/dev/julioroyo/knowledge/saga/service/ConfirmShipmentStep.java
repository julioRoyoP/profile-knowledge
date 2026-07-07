package dev.julioroyo.knowledge.saga.service;

import dev.julioroyo.knowledge.saga.exception.ShipmentUnavailableException;
import dev.julioroyo.knowledge.saga.model.SagaContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Tercer paso: entregar el pedido al transportista. Es el fallo tardío que dispara
 * la compensación de la demo; el guard carrierAvailable lo simula de forma
 * determinista.
 */
@Component
public class ConfirmShipmentStep implements SagaStep {

    private static final Logger log = LoggerFactory.getLogger(ConfirmShipmentStep.class);

    private final boolean carrierAvailable;

    public ConfirmShipmentStep() {
        this(true);
    }

    /** Constructor para test/demo que fuerza la rama de transportista caído. */
    public ConfirmShipmentStep(boolean carrierAvailable) {
        this.carrierAvailable = carrierAvailable;
    }

    @Override
    public String name() {
        return "confirm-shipment";
    }

    @Override
    public void execute(SagaContext context) {
        if (!carrierAvailable) {
            throw new ShipmentUnavailableException(context.order().id());
        }
        context.put("shipmentConfirmed", true);
        log.info("Shipment confirmed for order {}", context.order().id());
    }

    @Override
    public void compensate(SagaContext context) {
        context.find("shipmentConfirmed")
                .ifPresent(confirmed -> log.info("Cancelled shipment for order {}", context.order().id()));
    }
}
