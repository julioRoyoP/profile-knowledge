package dev.julioroyo.knowledge.payment.service;

import dev.julioroyo.knowledge.payment.exception.UnknownGatewayException;
import dev.julioroyo.knowledge.payment.model.PaymentRequest;
import dev.julioroyo.knowledge.payment.model.PaymentResult;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Consumidor de la abstracción. Nunca nombra un proveedor concreto: elige uno
 * por clave del Map<String, PaymentGateway> que Spring construye con un bean
 * por cada PaymentGateway. Añadir un proveedor no cambia esta clase.
 */
@Slf4j
@Service
public class PaymentService {

    private final Map<String, PaymentGateway> gateways;

    /** Spring inyecta una entrada por cada bean PaymentGateway, con clave el nombre del bean. */
    public PaymentService(Map<String, PaymentGateway> gateways) {
        this.gateways = gateways;
        log.info("Payment gateways registered: {}", gateways.keySet());
    }

    public PaymentResult pay(String provider, PaymentRequest request) {
        return gatewayFor(provider).processPayment(request);
    }

    public PaymentResult refund(String provider, String transactionId) {
        return gatewayFor(provider).refundPayment(transactionId);
    }

    public PaymentResult status(String provider, String transactionId) {
        return gatewayFor(provider).getPaymentStatus(transactionId);
    }

    private PaymentGateway gatewayFor(String provider) {
        return Optional.ofNullable(gateways.get(provider))
                .orElseThrow(() -> new UnknownGatewayException(provider, gateways.keySet()));
    }
}
