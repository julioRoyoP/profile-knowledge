package dev.julioroyo.knowledge.payment.model;

import java.math.BigDecimal;

/**
 * Resultado tipado de una operación de pago, común a todas las
 * implementaciones de gateway. Es una sealed interface para que cada
 * llamador trate todos los casos de forma exhaustiva.
 */
public sealed interface PaymentResult
        permits PaymentResult.PaymentSuccess,
                PaymentResult.PaymentFailure,
                PaymentResult.PaymentPending {

    /** Cobro capturado; transactionId lo identifica para reembolsos posteriores. */
    record PaymentSuccess(String transactionId, BigDecimal amount) implements PaymentResult {}

    /** Operación rechazada por el proveedor; reason es para logs, no para mostrar al usuario. */
    record PaymentFailure(String reason) implements PaymentResult {}

    /** Aceptado pero aún no liquidado; se resuelve con una consulta de estado posterior. */
    record PaymentPending(String reference) implements PaymentResult {}
}
