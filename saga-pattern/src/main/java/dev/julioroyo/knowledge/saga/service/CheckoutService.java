package dev.julioroyo.knowledge.saga.service;

import dev.julioroyo.knowledge.saga.model.Order;
import dev.julioroyo.knowledge.saga.model.SagaContext;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Ensambla la lista ordenada de pasos (stock, pago, envío) y delega la ejecución
 * en SagaOrchestrator. El orden se lista explícitamente aquí para que la secuencia
 * de negocio sea obvia en un único sitio.
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
     * Ejecuta la saga de checkout para el pedido. Retorna con normalidad si todos
     * los pasos tuvieron éxito; lanza SagaExecutionException si la saga se revirtió.
     */
    public void checkout(Order order) {
        List<SagaStep> steps = List.of(reserveStock, chargePayment, confirmShipment);
        orchestrator.execute(order.id(), steps, new SagaContext(order));
    }
}
