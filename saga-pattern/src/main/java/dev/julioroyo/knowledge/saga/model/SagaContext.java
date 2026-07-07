package dev.julioroyo.knowledge.saga.model;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Bolsa mutable compartida entre los pasos de una única ejecución de saga.
 * Transporta el Order en curso más un mapa de atributos donde los pasos publican
 * resultados que otros pasos o su compensación necesitan leer.
 */
public class SagaContext {

    private final Order order;
    private final Map<String, Object> attributes = new ConcurrentHashMap<>();

    public SagaContext(Order order) {
        this.order = order;
    }

    public Order order() {
        return order;
    }

    public void put(String key, Object value) {
        attributes.put(key, value);
    }

    /** Búsqueda con Optional para que la compensación deshaga solo si el paso publicó el valor. */
    public Optional<Object> find(String key) {
        return Optional.ofNullable(attributes.get(key));
    }
}
