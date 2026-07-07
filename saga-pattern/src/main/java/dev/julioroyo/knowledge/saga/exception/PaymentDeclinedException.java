package dev.julioroyo.knowledge.saga.exception;

/** Fallo de dominio que lanza el paso de cobro cuando un pago es rechazado. */
public class PaymentDeclinedException extends RuntimeException {

    public PaymentDeclinedException(String reason) {
        super("Payment declined: " + reason);
    }
}
