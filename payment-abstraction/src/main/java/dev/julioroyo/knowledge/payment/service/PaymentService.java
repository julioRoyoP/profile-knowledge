package dev.julioroyo.knowledge.payment.service;

import dev.julioroyo.knowledge.payment.exception.UnknownGatewayException;
import dev.julioroyo.knowledge.payment.model.PaymentRequest;
import dev.julioroyo.knowledge.payment.model.PaymentResult;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Consumer of the abstraction. It never names a concrete provider: it picks one
 * by key from the {@code Map<String, PaymentGateway>} that Spring builds
 * automatically — every {@link PaymentGateway} bean keyed by its bean name.
 *
 * <p>This is the same registry/strategy wiring used in the real system: adding a
 * provider is just adding a {@code @Component} implementation; this class does
 * not change.
 */
@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final Map<String, PaymentGateway> gateways;

    /** Spring injects one entry per {@link PaymentGateway} bean, keyed by bean name. */
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

    private PaymentGateway gatewayFor(String provider) {
        return Optional.ofNullable(gateways.get(provider))
                .orElseThrow(() -> new UnknownGatewayException(provider, gateways.keySet()));
    }
}
