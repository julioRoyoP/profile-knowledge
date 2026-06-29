package dev.julioroyo.knowledge.saga.exception;

/** Domain failure raised by the confirm-shipment step when no carrier can take the order. */
public class ShipmentUnavailableException extends RuntimeException {

    public ShipmentUnavailableException(String orderId) {
        super("No carrier available for order " + orderId);
    }
}
