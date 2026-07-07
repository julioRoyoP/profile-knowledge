package dev.julioroyo.knowledge.payment.model;

import java.math.BigDecimal;

/**
 * Petición de cobro agnóstica del proveedor. Lo mínimo para que cualquier
 * gateway la satisfaga sin filtrar detalles del proveedor concreto.
 */
public record PaymentRequest(String orderId, BigDecimal amount, String currency) {}
