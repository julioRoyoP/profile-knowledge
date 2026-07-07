package dev.julioroyo.knowledge.saga.model;

import java.math.BigDecimal;

/**
 * Resultado tipado de un intento de pago. Es una sealed interface para que el
 * compilador obligue a tratar todos los resultados posibles.
 */
public sealed interface PaymentResult
        permits PaymentResult.PaymentSuccess,
                PaymentResult.PaymentFailure,
                PaymentResult.PaymentPending {

    /** Cobro capturado; transactionId permite reembolsar después. */
    record PaymentSuccess(String transactionId, BigDecimal amount) implements PaymentResult {}

    /** Cobro rechazado; reason es para logs, no para mostrar tal cual al usuario. */
    record PaymentFailure(String reason) implements PaymentResult {}

    /** Cobro aceptado pero aún no liquidado; requiere seguimiento posterior. */
    record PaymentPending(String reference) implements PaymentResult {}
}
