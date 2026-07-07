package dev.julioroyo.knowledge.payment.service;

import dev.julioroyo.knowledge.payment.model.PaymentRequest;
import dev.julioroyo.knowledge.payment.model.PaymentResult;
import dev.julioroyo.knowledge.payment.model.PaymentResult.PaymentFailure;
import dev.julioroyo.knowledge.payment.model.PaymentResult.PaymentPending;
import dev.julioroyo.knowledge.payment.model.PaymentResult.PaymentSuccess;
import java.math.BigDecimal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Segunda implementación del mismo contrato PaymentGateway, para hacer visible
 * que un proveedor completamente distinto encaja sin cambiar al consumidor.
 * Es determinista: el resultado depende del importe y del transactionId, lo
 * que la hace útil como gateway de pruebas y para escenificar cada resultado.
 */
@Slf4j
@Component("mock")
public class MockPaymentGateway implements PaymentGateway {

    @Override
    public String provider() {
        return "mock";
    }

    @Override
    public PaymentResult processPayment(PaymentRequest request) {
        log.info("[mock] processing {} for order {}", request.amount(), request.orderId());
        return request.amount().compareTo(BigDecimal.ZERO) > 0
                ? new PaymentSuccess("mock-" + request.orderId(), request.amount())
                : new PaymentFailure("amount must be positive");
    }

    @Override
    public PaymentResult refundPayment(String transactionId) {
        log.info("[mock] refunding {}", transactionId);
        return resolveByTransactionId(transactionId);
    }

    @Override
    public PaymentResult getPaymentStatus(String transactionId) {
        log.info("[mock] status of {}", transactionId);
        return resolveByTransactionId(transactionId);
    }

    // Variación determinista según el transactionId, para poder escenificar cada
    // resultado: "fail" -> fallo, "pending" -> pendiente, resto -> éxito.
    private PaymentResult resolveByTransactionId(String transactionId) {
        if (transactionId == null || transactionId.contains("fail")) {
            return new PaymentFailure("transaction not found or not refundable");
        }
        if (transactionId.contains("pending")) {
            return new PaymentPending(transactionId);
        }
        return new PaymentSuccess(transactionId, BigDecimal.ZERO);
    }
}
