package dev.julioroyo.knowledge.saga;

/** Domain failure raised by {@link ChargePaymentStep} when a charge is declined. */
public class PaymentDeclinedException extends RuntimeException {

    public PaymentDeclinedException(String reason) {
        super("Payment declined: " + reason);
    }
}
