package dev.julioroyo.knowledge.saga.model;

import java.math.BigDecimal;

/**
 * Agregado de pedido mínimo. Un sistema real llevaría líneas de pedido, cliente,
 * dirección de envío, etc.; aquí conservamos solo lo que la saga necesita para
 * ilustrar el flujo.
 */
public record Order(String id, BigDecimal amount) {}
