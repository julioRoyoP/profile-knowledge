package dev.julioroyo.knowledge.saga.exception;

/** Fallo de dominio que lanza el paso de confirmación de envío cuando ningún transportista puede aceptar el pedido. */
public class ShipmentUnavailableException extends RuntimeException {

    public ShipmentUnavailableException(String orderId) {
        super("No carrier available for order " + orderId);
    }
}
