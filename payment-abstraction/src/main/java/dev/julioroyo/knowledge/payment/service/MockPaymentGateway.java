package dev.julioroyo.knowledge.payment.service;

import dev.julioroyo.knowledge.payment.model.PaymentRequest;
import dev.julioroyo.knowledge.payment.model.PaymentResult;
import dev.julioroyo.knowledge.payment.model.PaymentResult.PaymentFailure;
import dev.julioroyo.knowledge.payment.model.PaymentResult.PaymentSuccess;
import java.math.BigDecimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Second example implementation, present mainly to make the abstraction's payoff
 * visible: the same {@link PaymentGateway} contract admits a completely different
 * provider with no consumer change. Handy as a deterministic gateway for local
 * runs and tests — it approves any positive amount and declines the rest.
 */
@Component("mock")
public class MockPaymentGateway implements PaymentGateway {

    private static final Logger log = LoggerFactory.getLogger(MockPaymentGateway.class);

    @Override
    public String provider() {
        return "mock";
    }

    @Override
    public PaymentResult processPayment(PaymentRequest request) {
        log.info("[mock] processing {} for order {}", request.amount(), request.orderId());
        return request.amount().compareTo(BigDecimal.ZERO) > 0
                ? new PaymentSuccess("mock-" + request.orderId(), request.amount())
                : new PaymentFailure("amount must be positive");
    }

    @Override
    public PaymentResult refundPayment(String transactionId) {
        return new PaymentSuccess(transactionId, null);
    }

    @Override
    public PaymentResult getPaymentStatus(String transactionId) {
        return new PaymentSuccess(transactionId, null);
    }
}
