package dev.daniel730.rpgserver.error;

import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ErrorDeduplicator {

    private final long cooldownMillis;
    private final ConcurrentHashMap<String, Instant> lastReported = new ConcurrentHashMap<>();

    public ErrorDeduplicator(int dedupeMinutes) {
        this.cooldownMillis = Duration.ofMinutes(dedupeMinutes).toMillis();
    }

    public boolean shouldReport(String fingerprint) {
        if (fingerprint == null || fingerprint.isBlank()) {
            return true;
        }
        Instant now = Instant.now();
        pruneExpired(now);
        Instant previous = lastReported.get(fingerprint);
        if (previous != null && now.toEpochMilli() - previous.toEpochMilli() < cooldownMillis) {
            return false;
        }
        lastReported.put(fingerprint, now);
        return true;
    }

    private void pruneExpired(Instant now) {
        if (lastReported.size() < 256) {
            return;
        }
        long cutoff = now.toEpochMilli() - cooldownMillis;
        Iterator<Map.Entry<String, Instant>> iterator = lastReported.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Instant> entry = iterator.next();
            if (entry.getValue().toEpochMilli() < cutoff) {
                iterator.remove();
            }
        }
    }
}
