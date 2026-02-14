package com.example.latest_value_price_service;


import java.time.Instant;
import java.util.Map;
import java.util.Objects;

/**
 * Immutable price record.
 *
 * id      -> Instrument identifier (e.g., TCS, INFY)
 * asOf    -> Timestamp when price was determined
 * payload -> Flexible price data structure
 */
public final class Price {

    private final String id;
    private final Instant asOf;
    private final Map<String, Object> payload;

    public Price(String id, Instant asOf, Map<String, Object> payload) {
        this.id = Objects.requireNonNull(id, "id cannot be null");
        this.asOf = Objects.requireNonNull(asOf, "asOf cannot be null");
        this.payload = Objects.requireNonNull(payload, "payload cannot be null");
    }

    public String getId() {
        return id;
    }

    public Instant getAsOf() {
        return asOf;
    }

    public Map<String, Object> getPayload() {
        return payload;
    }
}

