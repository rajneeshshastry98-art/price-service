package com.example.latest_value_price_service;


import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class LatestValuePriceServiceTest {

    @Test
    void testBatchCompletionMakesDataVisible() {

        LatestValuePriceService service = new LatestValuePriceService();

        String batchId = service.startBatch();

        Price price = new Price(
                "TCS",
                Instant.now(),
                Map.of("price", 4200)
        );

        service.uploadChunk(batchId, List.of(price));

        service.completeBatch(batchId);

        Map<String, Price> result =
                service.getLatestPrices(List.of("TCS"));

        assertEquals(4200,
                result.get("TCS").getPayload().get("price"));
    }


    @Test
    void testCancelledBatchNotVisible() {

        LatestValuePriceService service = new LatestValuePriceService();

        String batchId = service.startBatch();

        Price price = new Price(
                "INFY",
                Instant.now(),
                Map.of("price", 1500)
        );

        service.uploadChunk(batchId, List.of(price));

        service.cancelBatch(batchId);

        Map<String, Price> result =
                service.getLatestPrices(List.of("INFY"));

        assertTrue(result.isEmpty());
    }


    @Test
    void testLatestAsOfWins() {

        LatestValuePriceService service = new LatestValuePriceService();

        String batch1 = service.startBatch();

        service.uploadChunk(batch1, List.of(
                new Price(
                        "WIPRO",
                        Instant.parse("2026-01-01T10:00:00Z"),
                        Map.of("price", 500)
                )
        ));

        service.completeBatch(batch1);


        String batch2 = service.startBatch();

        service.uploadChunk(batch2, List.of(
                new Price(
                        "WIPRO",
                        Instant.parse("2025-01-01T10:00:00Z"),
                        Map.of("price", 400)
                )
        ));

        service.completeBatch(batch2);

        Map<String, Price> result =
                service.getLatestPrices(List.of("WIPRO"));

        assertEquals(500,
                result.get("WIPRO").getPayload().get("price"));
    }

}

