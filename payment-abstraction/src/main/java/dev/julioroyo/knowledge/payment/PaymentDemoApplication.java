package dev.julioroyo.knowledge.payment;

import dev.julioroyo.knowledge.payment.exception.UnknownGatewayException;
import dev.julioroyo.knowledge.payment.model.PaymentRequest;
import dev.julioroyo.knowledge.payment.model.PaymentResult;
import dev.julioroyo.knowledge.payment.model.PaymentResult.PaymentFailure;
import dev.julioroyo.knowledge.payment.model.PaymentResult.PaymentPending;
import dev.julioroyo.knowledge.payment.model.PaymentResult.PaymentSuccess;
import dev.julioroyo.knowledge.payment.service.PaymentService;
import java.math.BigDecimal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * Punto de entrada ejecutable de la demo payment-abstraction. Al arrancar
 * cobra con dos proveedores distintos a través del mismo PaymentService y
 * escenifica cada resultado (éxito, fallo, pendiente) y el error de proveedor
 * desconocido, de modo que el patrón queda visible en los logs de consola.
 */
@Slf4j
@SpringBootApplication
public class PaymentDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentDemoApplication.class, args);
    }

    @Bean
    CommandLineRunner demo(PaymentService payments) {
        return args -> {
            sameServiceTwoProviders(payments);
            mockFailurePath(payments);
            refundAndStatusVariations(payments);
            unknownProvider(payments);
            log.info("=== Demo finished ===");
        };
    }

    /** El mismo PaymentService cobra con "stripe" y con "mock" sin cambio alguno. */
    private void sameServiceTwoProviders(PaymentService payments) {
        log.info("");
        log.info("======== SCENARIO 1: same service, two providers ========");
        PaymentRequest request = new PaymentRequest("order-1", new BigDecimal("49.90"), "EUR");
        describe("stripe pay", payments.pay("stripe", request));
        describe("mock pay", payments.pay("mock", request));
    }

    /** MockPaymentGateway rechaza un importe no positivo con PaymentFailure. */
    private void mockFailurePath(PaymentService payments) {
        log.info("");
        log.info("======== SCENARIO 2: mock declines a non-positive amount ========");
        PaymentRequest invalid = new PaymentRequest("order-2", BigDecimal.ZERO, "EUR");
        describe("mock pay", payments.pay("mock", invalid));
    }

    /**
     * El mock resuelve refund/status de forma determinista según el
     * transactionId, así que la misma llamada produce éxito, pendiente o fallo.
     */
    private void refundAndStatusVariations(PaymentService payments) {
        log.info("");
        log.info("======== SCENARIO 3: mock refund/status variations ========");
        describe("status ok", payments.status("mock", "mock-order-1"));
        describe("status pending", payments.status("mock", "mock-order-pending"));
        describe("refund fail", payments.refund("mock", "mock-order-fail"));
    }

    /** Pedir un proveedor no registrado falla de forma evidente. */
    private void unknownProvider(PaymentService payments) {
        log.info("");
        log.info("======== SCENARIO 4: unknown provider ========");
        PaymentRequest request = new PaymentRequest("order-3", BigDecimal.TEN, "EUR");
        try {
            payments.pay("paypal", request);
        } catch (UnknownGatewayException e) {
            log.warn("Rejected: {}", e.getMessage());
        }
    }

    /** Trata cada caso de la sealed interface de forma exhaustiva con switch. */
    private void describe(String label, PaymentResult result) {
        String outcome = switch (result) {
            case PaymentSuccess s -> "SUCCESS tx=%s amount=%s".formatted(s.transactionId(), s.amount());
            case PaymentFailure f -> "FAILURE reason=%s".formatted(f.reason());
            case PaymentPending p -> "PENDING reference=%s".formatted(p.reference());
        };
        log.info("    {} -> {}", label, outcome);
    }
}
