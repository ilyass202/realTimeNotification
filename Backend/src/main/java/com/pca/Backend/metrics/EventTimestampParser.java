package com.pca.Backend.metrics;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Calcule le délai entre l'horodatage de l'événement (CDC / Kafka / payload) et le traitement backend.
 */
public final class EventTimestampParser {

    private static final long MAX_LAG_MS = 86_400_000L;

    private EventTimestampParser() {
    }

    /**
     * @return délai en millisecondes, ou -1 si pas de timestamp exploitable dans le JSON
     */
    public static long lagMillis(JsonNode json) {
        if (json == null || json.isNull()) {
            return -1;
        }
        Long raw = extractRawTimestamp(json);
        if (raw == null || raw <= 0) {
            return -1;
        }
        return lagFromEpochRaw(raw);
    }

    /**
     * @param rawTimestamp valeur brute (microsecondes Debezium, ms, etc.)
     * @return délai en millisecondes, ou -1 si invalide
     */
    public static long lagMillisFromRaw(Long rawTimestamp) {
        if (rawTimestamp == null || rawTimestamp <= 0) {
            return -1;
        }
        return lagFromEpochRaw(rawTimestamp);
    }
    public static long lagMillisFromKafkaRecord(long kafkaTimestampMs) {
        if (kafkaTimestampMs <= 0) {
            return -1;
        }
        long lag = System.currentTimeMillis() - kafkaTimestampMs;
        if (lag < 0 || lag > MAX_LAG_MS) {
            return -1;
        }
        return lag;
    }

    private static Long extractRawTimestamp(JsonNode json) {
        for (String field : new String[] {
            "created_at", "createdAt", "ts_ms", "timestamp", "event_time"
        }) {
            if (json.hasNonNull(field) && json.get(field).isNumber()) {
                return json.get(field).asLong();
            }
        }
        JsonNode after = json.get("after");
        if (after != null && after.isObject()) {
            return extractRawTimestamp(after);
        }
        JsonNode payload = json.get("payload");
        if (payload != null && payload.isObject()) {
            return extractRawTimestamp(payload);
        }
        return null;
    }

    static long toEpochMillis(long raw) {
        if (raw >= 100_000_000_000_000_000L) {
            return raw / 1_000_000L;
        }
        if (raw >= 100_000_000_000_000L) {
            return raw / 1_000L;
        }
        if (raw >= 100_000_000_000L) {
            return raw;
        }
        return raw * 1_000L;
    }

    private static long lagFromEpochRaw(long raw) {
        long eventEpochMs = toEpochMillis(raw);
        long lag = System.currentTimeMillis() - eventEpochMs;
        if (lag < 0 || lag > MAX_LAG_MS) {
            return -1;
        }
        return lag;
    }
}
