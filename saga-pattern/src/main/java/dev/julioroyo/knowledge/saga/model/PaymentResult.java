package dev.julioroyo.knowledge.saga.model;

import java.math.BigDecimal;

/**
 * Typed outcome of a payment attempt.
 *
 * <p>Modeled as a sealed interface so the compiler enforces that every consumer
 * handles each possible outcome — there is no "unknown string status" to guess
 * about. Consumers use pattern matching in a {@code switch} and the compiler
 * rejects the code if a new variant is added but left unhandled.
 */
public sealed interface PaymentResult
        permits PaymentResult.PaymentSuccess,
                PaymentResult.PaymentFailure,
                PaymentResult.PaymentPending {

    /** Charge captured synchronously; {@code transactionId} can be refunded later. */
    record PaymentSuccess(String transactionId, BigDecimal amount) implements PaymentResult {}

    /** Charge declined. {@code reason} is safe to log but not to show raw to the user. */
    record PaymentFailure(String reason) implements PaymentResult {}

    /** Charge accepted but not yet settled (e.g. async voucher); needs follow-up, not a synchronous saga. */
    record PaymentPending(String reference) implements PaymentResult {}
}
