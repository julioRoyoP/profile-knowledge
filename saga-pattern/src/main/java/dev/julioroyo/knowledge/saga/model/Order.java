package dev.julioroyo.knowledge.saga.model;

import java.math.BigDecimal;

/**
 * Minimal order aggregate. A real system would carry line items, customer,
 * shipping address, etc.; here we keep only what the saga needs to illustrate
 * the flow.
 */
public record Order(String id, BigDecimal amount) {}
