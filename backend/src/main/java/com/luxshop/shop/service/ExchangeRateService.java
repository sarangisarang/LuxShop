package com.luxshop.shop.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.luxshop.shop.dto.RatesResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * Live GEL exchange rates from an international source (ExchangeRate-API's free,
 * key-less endpoint), cached in memory and refreshed hourly. Server-side so the
 * browser avoids CORS and rate limits; falls back to static rates when the API
 * is unreachable, so prices always render.
 */
@Service
public class ExchangeRateService {

    private static final Logger log = LoggerFactory.getLogger(ExchangeRateService.class);
    private static final String URL = "https://open.er-api.com/v6/latest/GEL";
    private static final long TTL_MS = 60 * 60 * 1000L;

    // Approximate fallback (per 1 GEL) if the API can't be reached.
    private static final Map<String, Double> FALLBACK = Map.of(
            "GEL", 1.0, "USD", 0.37, "EUR", 0.34, "GBP", 0.29, "TRY", 15.0);

    private final RestClient client = RestClient.create();
    private volatile Snapshot cache;

    public RatesResponse getRates() {
        Snapshot snapshot = cache;
        if (snapshot == null || System.currentTimeMillis() - snapshot.fetchedAt > TTL_MS) {
            snapshot = refresh();
        }
        return new RatesResponse("GEL", snapshot.rates, snapshot.updated);
    }

    private synchronized Snapshot refresh() {
        Snapshot current = cache;
        if (current != null && System.currentTimeMillis() - current.fetchedAt < TTL_MS) {
            return current;
        }
        try {
            ErApiResponse response = client.get().uri(URL).retrieve().body(ErApiResponse.class);
            if (response != null && "success".equals(response.result()) && response.rates() != null) {
                cache = new Snapshot(response.rates(), response.time_last_update_utc(), System.currentTimeMillis());
                return cache;
            }
        } catch (Exception e) {
            log.warn("Exchange-rate fetch failed ({}); using fallback rates.", e.getMessage());
        }
        Snapshot fallback = cache != null
                ? new Snapshot(cache.rates, cache.updated, System.currentTimeMillis())
                : new Snapshot(FALLBACK, "fallback rates", System.currentTimeMillis());
        cache = fallback;
        return fallback;
    }

    private record Snapshot(Map<String, Double> rates, String updated, long fetchedAt) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record ErApiResponse(String result, String time_last_update_utc, Map<String, Double> rates) {
    }
}
