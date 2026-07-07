package dev.julioroyo.knowledge.payment.service;

import dev.julioroyo.knowledge.payment.model.PaymentRequest;
import dev.julioroyo.knowledge.payment.model.PaymentResult;

/**
 * Contrato que todo proveedor de pago debe satisfacer. Es la frontera de la
 * que dependen los consumidores; añadir un proveedor es añadir una
 * implementación, sin tocar el código que cobra.
 */
public interface PaymentGateway {

    /**
     * Clave estable del proveedor (por ejemplo "stripe") para seleccionar este
     * gateway en tiempo de ejecución. Coincide con el nombre del bean de Spring
     * para poder cablearlos en un Map<String, PaymentGateway>.
     */
    String provider();

    PaymentResult processPayment(PaymentRequest request);

    PaymentResult refundPayment(String transactionId);

    PaymentResult getPaymentStatus(String transactionId);
}
