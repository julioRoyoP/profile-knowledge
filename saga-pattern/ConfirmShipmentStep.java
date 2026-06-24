package dev.julioroyo.knowledge.saga;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Third step: hand the order to the shipping carrier.
 *
 * <p>This is the most likely <em>late</em> failure in a checkout: stock is
 * already reserved and the card already charged when the carrier rejects the
 * shipment. When that happens the orchestrator compensates in reverse — refund
 * the charge, then release the stock — which is exactly the scenario this demo
 * exists to show. The {@code carrierAvailable} guard simulates that downstream
 * failure deterministically.
 */
@Component
public class ConfirmShipmentStep implements SagaStep {

    private static final Logger log = LoggerFactory.getLogger(ConfirmShipmentStep.class);

    private final boolean carrierAvailable;

    public ConfirmShipmentStep() {
        this(true);
    }

    /** Test/demo constructor to force the carrier-down branch. */
    ConfirmShipmentStep(boolean carrierAvailable) {
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
