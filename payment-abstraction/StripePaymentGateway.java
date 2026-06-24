package dev.julioroyo.knowledge.payment;

import dev.julioroyo.knowledge.payment.PaymentResult.PaymentSuccess;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Example provider implementation.
 *
 * <p>The bean name {@code "stripe"} is also the {@link #provider()} key, so it
 * lands under that key in the {@code Map<String, PaymentGateway>} Spring injects
 * into {@link PaymentService}.
 *
 * <p>The external call is <em>simulated</em> on purpose: no real Stripe SDK, so
 * the file stays self-contained. The shape — build a provider request, call out,
 * map the provider response back to the agnostic {@link PaymentResult} — mirrors
 * how a real integration is structured.
 */
@Component("stripe")
public class StripePaymentGateway implements PaymentGateway {

    private static final Logger log = LoggerFactory.getLogger(StripePaymentGateway.class);

    @Override
    public String provider() {
        return "stripe";
    }

    @Override
    public PaymentResult processPayment(PaymentRequest request) {
        // Real impl: stripeClient.charges().create(...) and map the response.
        log.info("[stripe] charging {} {} for order {}",
                request.amount(), request.currency(), request.orderId());
        return new PaymentSuccess("ch_" + UUID.randomUUID(), request.amount());
    }

    @Override
    public PaymentResult refundPayment(String transactionId) {
        log.info("[stripe] refunding {}", transactionId);
        return new PaymentSuccess(transactionId, null);
    }

    @Override
    public PaymentResult getPaymentStatus(String transactionId) {
        log.info("[stripe] status of {}", transactionId);
        return new PaymentSuccess(transactionId, null);
    }
}
