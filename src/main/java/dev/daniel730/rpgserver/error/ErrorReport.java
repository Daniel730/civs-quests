package dev.daniel730.rpgserver.error;

import java.time.Instant;
import java.util.Objects;

public final class ErrorReport {

    private final String source;
    private final String pluginName;
    private final String threadName;
    private final String message;
    private final String stackTrace;
    private final String context;
    private final String fingerprint;
    private final Instant timestamp;

    public ErrorReport(String source, String pluginName, String threadName,
                       String message, String stackTrace, String context,
                       String fingerprint, Instant timestamp) {
        this.source = Objects.requireNonNull(source, "source");
        this.pluginName = pluginName == null ? "unknown" : pluginName;
        this.threadName = threadName == null ? "unknown" : threadName;
        this.message = message == null ? "" : message;
        this.stackTrace = stackTrace == null ? "" : stackTrace;
        this.context = context == null ? "" : context;
        this.fingerprint = fingerprint == null ? "" : fingerprint;
        this.timestamp = timestamp == null ? Instant.now() : timestamp;
    }

    public String getSource() {
        return source;
    }

    public String getPluginName() {
        return pluginName;
    }

    public String getThreadName() {
        return threadName;
    }

    public String getMessage() {
        return message;
    }

    public String getStackTrace() {
        return stackTrace;
    }

    public String getContext() {
        return context;
    }

    public String getFingerprint() {
        return fingerprint;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}
