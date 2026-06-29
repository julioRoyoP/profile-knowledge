package dev.julioroyo.knowledge.payment.model;

import java.math.BigDecimal;

/**
 * Provider-agnostic charge request. Deliberately minimal: just enough to show a
 * stable contract that any gateway can satisfy without leaking provider details.
 */
public record PaymentRequest(String orderId, BigDecimal amount, String currency) {}
