package dev.julioroyo.knowledge.saga;

import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Entry point that wires the demo together: it builds the <em>ordered</em> list
 * of steps and delegates execution to {@link SagaOrchestrator}.
 *
 * <p>Order matters (stock before payment before shipping), so the steps are
 * listed explicitly here rather than injecting an unordered {@code List<SagaStep>}
 * — making the business sequence obvious and reviewable in one place.
 */
@Service
public class CheckoutService {

    private final SagaOrchestrator orchestrator;
    private final ReserveStockStep reserveStock;
    private final ChargePaymentStep chargePayment;
    private final ConfirmShipmentStep confirmShipment;

    public CheckoutService(SagaOrchestrator orchestrator,
                           ReserveStockStep reserveStock,
                           ChargePaymentStep chargePayment,
                           ConfirmShipmentStep confirmShipment) {
        this.orchestrator = orchestrator;
        this.reserveStock = reserveStock;
        this.chargePayment = chargePayment;
        this.confirmShipment = confirmShipment;
    }

    /**
     * Runs the checkout saga for the given order. Returns normally if every step
     * succeeded; throws {@link SagaExecutionException} if the saga was rolled back.
     */
    public void checkout(Order order) {
        List<SagaStep> steps = List.of(reserveStock, chargePayment, confirmShipment);
        orchestrator.execute(order.id(), steps, new SagaContext(order));
    }
}
