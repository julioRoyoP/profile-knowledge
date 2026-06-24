package dev.julioroyo.knowledge.saga;

/** Domain failure raised by {@link ConfirmShipmentStep} when no carrier can take the order. */
public class ShipmentUnavailableException extends RuntimeException {

    public ShipmentUnavailableException(String orderId) {
        super("No carrier available for order " + orderId);
    }
}
