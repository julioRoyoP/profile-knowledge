package dev.julioroyo.knowledge.saga.exception;

/** Domain failure raised by the charge-payment step when a charge is declined. */
public class PaymentDeclinedException extends RuntimeException {

    public PaymentDeclinedException(String reason) {
        super("Payment declined: " + reason);
    }
}
