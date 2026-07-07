package dev.julioroyo.knowledge.payment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.julioroyo.knowledge.payment.exception.UnknownGatewayException;
import dev.julioroyo.knowledge.payment.model.PaymentRequest;
import dev.julioroyo.knowledge.payment.model.PaymentResult;
import dev.julioroyo.knowledge.payment.model.PaymentResult.PaymentSuccess;
import java.math.BigDecimal;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Verifica el cableado registry/strategy: el servicio enruta al gateway cuya
 * clave coincide con el proveedor y deja intactos los demás, y falla de forma
 * evidente si se pide un proveedor no registrado. Los gateways están mockeados,
 * así que solo se ejercita la lógica de selección, no la integración concreta.
 */
@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock private PaymentGateway stripe;
    @Mock private PaymentGateway mock;

    private PaymentService service;

    @BeforeEach
    void setUp() {
        // Refleja el Map<String, PaymentGateway> que inyectaría Spring, con clave el nombre del bean.
        service = new PaymentService(Map.of("stripe", stripe, "mock", mock));
    }

    @Test
    void shouldRoutePaymentToTheSelectedGatewayOnly() {
        PaymentRequest request = new PaymentRequest("order-1", BigDecimal.TEN, "EUR");
        PaymentResult expected = new PaymentSuccess("ch_1", BigDecimal.TEN);
        when(stripe.processPayment(request)).thenReturn(expected);

        PaymentResult result = service.pay("stripe", request);

        assertThat(result).isEqualTo(expected);
        verify(mock, never()).processPayment(any());
    }

    @Test
    void shouldRouteRefundToTheSelectedGateway() {
        PaymentResult expected = new PaymentSuccess("tx-9", BigDecimal.ZERO);
        when(mock.refundPayment("tx-9")).thenReturn(expected);

        PaymentResult result = service.refund("mock", "tx-9");

        assertThat(result).isEqualTo(expected);
        verify(stripe, never()).refundPayment(any());
    }

    @Test
    void shouldThrowUnknownGatewayExceptionForUnregisteredProvider() {
        PaymentRequest request = new PaymentRequest("order-1", BigDecimal.TEN, "EUR");

        assertThatThrownBy(() -> service.pay("paypal", request))
                .isInstanceOf(UnknownGatewayException.class)
                .hasMessageContaining("paypal");
    }
}
