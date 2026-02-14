package com.example.latest_value_price_service;


import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory implementation of latest-value price service.
 *
 * Design decisions:
 *
 * 1. Uses two-level storage:
 *
 *    stagingBatches -> Stores incomplete batches (not visible to consumers)
 *    activePrices   -> Stores completed prices (visible to consumers)
 *
 * 2. Atomic visibility guarantee:
 *
 *    Prices become visible ONLY when completeBatch() is called.
 *
 * 3. Thread safety:
 *
 *    ConcurrentHashMap used for concurrent uploads.
 *    synchronized used for batch lifecycle operations.
 *
 * 4. Latest price determined using asOf timestamp (NOT upload order).
 */
public class LatestValuePriceService {

    /**
     * Stores batches currently being uploaded.
     *
     * batchId -> (instrumentId -> Price)
     */
    private final Map<String, Map<String, Price>> stagingBatches =
            new ConcurrentHashMap<>();


    /**
     * Stores latest visible prices.
     *
     * instrumentId -> Price
     */
    private final Map<String, Price> activePrices =
            new ConcurrentHashMap<>();


    /**
     * Starts a new batch.
     *
     * @return batchId
     */
    public synchronized String startBatch() {

        String batchId = UUID.randomUUID().toString();

        stagingBatches.put(batchId, new ConcurrentHashMap<>());

        return batchId;
    }


    /**
     * Uploads chunk of prices into batch.
     *
     * Thread-safe for parallel uploads.
     */
    public void uploadChunk(String batchId, List<Price> prices) {

        Map<String, Price> batch = stagingBatches.get(batchId);

        if (batch == null)
            throw new IllegalStateException("Batch not found or already completed/cancelled");

        for (Price price : prices) {

            // keep latest price within batch
            batch.merge(
                    price.getId(),
                    price,
                    (oldPrice, newPrice) ->
                            newPrice.getAsOf().isAfter(oldPrice.getAsOf())
                                    ? newPrice
                                    : oldPrice
            );
        }
    }


    /**
     * Completes batch and atomically makes prices visible.
     */
    public synchronized void completeBatch(String batchId) {

        Map<String, Price> batch = stagingBatches.remove(batchId);

        if (batch == null)
            throw new IllegalStateException("Batch not found");

        for (Price price : batch.values()) {

            activePrices.merge(
                    price.getId(),
                    price,
                    (existing, incoming) ->
                            incoming.getAsOf().isAfter(existing.getAsOf())
                                    ? incoming
                                    : existing
            );
        }
    }


    /**
     * Cancels batch and discards data.
     */
    public synchronized void cancelBatch(String batchId) {

        stagingBatches.remove(batchId);
    }


    /**
     * Returns latest prices for requested instrument IDs.
     *
     * Only completed batches are visible.
     */
    public Map<String, Price> getLatestPrices(List<String> ids) {

        Map<String, Price> result = new HashMap<>();

        for (String id : ids) {

            Price price = activePrices.get(id);

            if (price != null)
                result.put(id, price);
        }

        return result;
    }

}
