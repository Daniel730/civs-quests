package dev.daniel730.rpgserver.error;

import dev.daniel730.rpgserver.RpgServerPlugin;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.logging.Level;

public final class WebhookReporter {

    private final RpgServerPlugin plugin;
    private final ErrorReportingConfig config;
    private final HttpClient httpClient;

    public WebhookReporter(RpgServerPlugin plugin, ErrorReportingConfig config) {
        this.plugin = plugin;
        this.config = config;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .build();
    }

    public void send(ErrorReport report) {
        if (!config.isWebhookConfigured()) {
            return;
        }
        try {
            String json = buildPayload(report);
            HttpRequest request = HttpRequest.newBuilder(URI.create(config.getWebhookUrl()))
                    .timeout(Duration.ofSeconds(30))
                    .header("Content-Type", "application/json")
                    .header("User-Agent", "RPGServer-ErrorReporter")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                plugin.getLogger().log(Level.WARNING,
                        "[error-reporting] Webhook respondeu " + response.statusCode());
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING,
                    "[error-reporting] Falha ao enviar webhook: " + e.getMessage(), e);
        }
    }

    private String buildPayload(ErrorReport report) {
        return """
                {
                  "event": "server-error",
                  "server": %s,
                  "source": %s,
                  "plugin": %s,
                  "thread": %s,
                  "timestamp": %s,
                  "fingerprint": %s,
                  "message": %s,
                  "context": %s,
                  "stackTrace": %s
                }
                """.formatted(
                jsonString(config.getServerName()),
                jsonString(report.getSource()),
                jsonString(report.getPluginName()),
                jsonString(report.getThreadName()),
                jsonString(report.getTimestamp().toString()),
                jsonString(report.getFingerprint()),
                jsonString(report.getMessage()),
                jsonString(report.getContext()),
                jsonString(report.getStackTrace())
        ).replaceAll("\\s+", " ").trim();
    }

    private static String jsonString(String value) {
        if (value == null) {
            return "\"\"";
        }
        return "\"" + value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n")
                .replace("\t", "\\t")
                + "\"";
    }
}
