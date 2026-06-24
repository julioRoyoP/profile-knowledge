package dev.julioroyo.knowledge.saga;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Mutable bag shared across the steps of a single saga execution.
 *
 * <p>It carries the business aggregate being processed (the {@link Order}) plus
 * an attribute map where steps publish results that later steps — or their own
 * compensation — need to read (e.g. a payment transaction id used later to
 * issue a refund).
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

    /**
     * Optional-based lookup instead of returning {@code null}, so compensation
     * code can express "only undo if the forward step actually published this".
     */
    public Optional<Object> find(String key) {
        return Optional.ofNullable(attributes.get(key));
    }
}
