package dev.julioroyo.knowledge.saga;

import dev.julioroyo.knowledge.saga.exception.SagaExecutionException;
import dev.julioroyo.knowledge.saga.model.Order;
import dev.julioroyo.knowledge.saga.model.SagaStepRecord;
import dev.julioroyo.knowledge.saga.repository.SagaStateRepository;
import dev.julioroyo.knowledge.saga.service.ChargePaymentStep;
import dev.julioroyo.knowledge.saga.service.CheckoutService;
import dev.julioroyo.knowledge.saga.service.ConfirmShipmentStep;
import dev.julioroyo.knowledge.saga.service.ReserveStockStep;
import dev.julioroyo.knowledge.saga.service.SagaOrchestrator;
import java.math.BigDecimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * Punto de entrada ejecutable de la demo saga-pattern. Al arrancar ejecuta dos
 * checkouts seguidos: uno que completa con éxito (sin compensación) y otro donde
 * confirm-shipment falla y dispara la compensación en orden inverso. Todo el
 * patrón queda visible en los logs de consola.
 */
@SpringBootApplication
public class SagaDemoApplication {

    private static final Logger log = LoggerFactory.getLogger(SagaDemoApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(SagaDemoApplication.class, args);
    }

    @Bean
    CommandLineRunner demo(CheckoutService checkoutService,
                           SagaOrchestrator orchestrator,
                           ReserveStockStep reserveStock,
                           ChargePaymentStep chargePayment,
                           SagaStateRepository stateRepository) {
        return args -> {
            runHappyPath(checkoutService, stateRepository);
            runCompensatingPath(orchestrator, reserveStock, chargePayment, stateRepository);
            log.info("=== Demo finished ===");
        };
    }

    /** Escenario 1: todos los pasos tienen éxito; no se compensa nada. */
    private void runHappyPath(CheckoutService checkoutService, SagaStateRepository stateRepository) {
        Order order = new Order("order-success", new BigDecimal("129.90"));
        log.info("");
        log.info("================ SCENARIO 1: checkout succeeds ================");
        log.info("Starting checkout for {} (amount {})", order.id(), order.amount());
        checkoutService.checkout(order);
        log.info("Checkout for {} completed with no compensation", order.id());
        printHistory(stateRepository, order.id());
    }

    /**
     * Escenario 2: el transportista está caído, así que confirm-shipment
     * falla después de reservar el stock y cobrar la tarjeta. El orquestador
     * compensa en orden inverso: reembolsar el pago y luego liberar el stock.
     */
    private void runCompensatingPath(SagaOrchestrator orchestrator,
                                     ReserveStockStep reserveStock,
                                     ChargePaymentStep chargePayment,
                                     SagaStateRepository stateRepository) {
        // Mismo cableado que en producción, pero con el transportista forzado a caído.
        CheckoutService failingCheckout = new CheckoutService(
                orchestrator, reserveStock, chargePayment, new ConfirmShipmentStep(false));

        Order order = new Order("order-rollback", new BigDecimal("74.50"));
        log.info("");
        log.info("========= SCENARIO 2: shipment fails -> compensation =========");
        log.info("Starting checkout for {} (amount {})", order.id(), order.amount());
        try {
            failingCheckout.checkout(order);
        } catch (SagaExecutionException e) {
            log.warn("Checkout for {} rolled back at step '{}': {}",
                    e.sagaId(), e.failedStep(), e.getMessage());
        }
        printHistory(stateRepository, order.id());
    }

    /** Vuelca el rastro de estado persistido de una saga para que las transiciones sean auditables. */
    private void printHistory(SagaStateRepository stateRepository, String sagaId) {
        log.info("State trail for saga '{}':", sagaId);
        for (SagaStepRecord record : stateRepository.history(sagaId)) {
            log.info("    {} -> {}", record.stepName(), record.status());
        }
    }
}
