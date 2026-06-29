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
 * Verifies the strategy/registry wiring: the service routes to the gateway that
 * matches the provider key and leaves the others untouched, and it fails loudly
 * when asked for a provider that was never registered. The gateways are mocked,
 * so the test only exercises the selection logic, not any provider integration.
 */
@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock private PaymentGateway stripe;
    @Mock private PaymentGateway mock;

    private PaymentService service;

    @BeforeEach
    void setUp() {
        // Mirrors the Map<String, PaymentGateway> Spring would inject, keyed by bean name.
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
        PaymentResult expected = new PaymentSuccess("tx-9", null);
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
