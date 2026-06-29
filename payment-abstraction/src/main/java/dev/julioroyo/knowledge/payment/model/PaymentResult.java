package dev.julioroyo.knowledge.payment.model;

import java.math.BigDecimal;

/**
 * Typed outcome of a payment operation, shared by every payment gateway
 * implementation.
 *
 * <p>Same modeling idea as the saga demo: a sealed interface so every caller
 * handles each outcome exhaustively, with no stringly-typed "unknown status".
 * Keeping the contract's return type abstract (this interface) rather than a
 * provider-specific response is what makes gateways truly interchangeable.
 */
public sealed interface PaymentResult
        permits PaymentResult.PaymentSuccess,
                PaymentResult.PaymentFailure,
                PaymentResult.PaymentPending {

    /** Charge captured; {@code transactionId} identifies it for later refunds. */
    record PaymentSuccess(String transactionId, BigDecimal amount) implements PaymentResult {}

    /** Operation rejected by the provider. {@code reason} is for logs, not raw user display. */
    record PaymentFailure(String reason) implements PaymentResult {}

    /** Accepted but not settled yet (e.g. async voucher); resolved by a later status check. */
    record PaymentPending(String reference) implements PaymentResult {}
}
