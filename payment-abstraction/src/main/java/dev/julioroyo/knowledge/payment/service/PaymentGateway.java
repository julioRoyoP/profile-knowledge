package dev.julioroyo.knowledge.payment.service;

import dev.julioroyo.knowledge.payment.model.PaymentRequest;
import dev.julioroyo.knowledge.payment.model.PaymentResult;

/**
 * The seam: the contract every payment provider must satisfy.
 *
 * <p>Consumers ({@link PaymentService}) depend only on this interface — the
 * dependency-inversion principle in practice. Adding a new provider means adding
 * one implementation; no consumer code changes.
 */
public interface PaymentGateway {

    /**
     * Stable provider key (e.g. {@code "stripe"}) used to select this gateway at
     * runtime. Matches the Spring bean name so the gateways can be wired into a
     * {@code Map<String, PaymentGateway>}.
     */
    String provider();

    PaymentResult processPayment(PaymentRequest request);

    PaymentResult refundPayment(String transactionId);

    PaymentResult getPaymentStatus(String transactionId);
}
