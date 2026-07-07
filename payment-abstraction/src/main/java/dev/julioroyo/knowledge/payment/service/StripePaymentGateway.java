package dev.julioroyo.knowledge.payment.service;

import dev.julioroyo.knowledge.payment.model.PaymentRequest;
import dev.julioroyo.knowledge.payment.model.PaymentResult;
import dev.julioroyo.knowledge.payment.model.PaymentResult.PaymentSuccess;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Implementación de proveedor de ejemplo. El nombre del bean "stripe" es
 * también su clave provider(), así que Spring lo inyecta bajo esa clave en el
 * Map<String, PaymentGateway> que consume PaymentService. La llamada externa
 * está simulada: no usa el SDK real, pero mantiene la forma de una integración
 * (construir petición, llamar, mapear la respuesta a PaymentResult).
 */
@Slf4j
@Component("stripe")
public class StripePaymentGateway implements PaymentGateway {

    @Override
    public String provider() {
        return "stripe";
    }

    @Override
    public PaymentResult processPayment(PaymentRequest request) {
        log.info("[stripe] charging {} {} for order {}",
                request.amount(), request.currency(), request.orderId());
        return new PaymentSuccess("ch_" + UUID.randomUUID(), request.amount());
    }

    @Override
    public PaymentResult refundPayment(String transactionId) {
        log.info("[stripe] refunding {}", transactionId);
        // Demo: solo simula éxito. El importe real no se conoce aquí; se usa ZERO.
        return new PaymentSuccess(transactionId, BigDecimal.ZERO);
    }

    @Override
    public PaymentResult getPaymentStatus(String transactionId) {
        log.info("[stripe] status of {}", transactionId);
        return new PaymentSuccess(transactionId, BigDecimal.ZERO);
    }
}
