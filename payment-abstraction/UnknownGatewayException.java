package dev.julioroyo.knowledge.payment;

import java.util.Set;

/** Raised when a payment is requested for a provider key with no registered gateway. */
public class UnknownGatewayException extends RuntimeException {

    public UnknownGatewayException(String provider, Set<String> available) {
        super("No payment gateway registered for '%s'. Available: %s".formatted(provider, available));
    }
}
