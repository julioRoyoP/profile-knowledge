package dev.julioroyo.knowledge.payment.exception;

import java.util.Set;

/** Se lanza cuando se pide un pago para una clave de proveedor sin gateway registrado. */
public class UnknownGatewayException extends RuntimeException {

    public UnknownGatewayException(String provider, Set<String> available) {
        super("No payment gateway registered for '%s'. Available: %s".formatted(provider, available));
    }
}
