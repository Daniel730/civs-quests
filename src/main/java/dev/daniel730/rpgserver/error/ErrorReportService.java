package dev.daniel730.rpgserver.error;

import dev.daniel730.rpgserver.RpgServerPlugin;
import org.bukkit.Bukkit;

import java.time.Instant;
import java.util.logging.Level;

public final class ErrorReportService {

    private final RpgServerPlugin plugin;
    private ErrorReportingConfig config;
    private ErrorDeduplicator deduplicator;
    private GitHubIssuesReporter githubReporter;
    private WebhookReporter webhookReporter;

    public ErrorReportService(RpgServerPlugin plugin) {
        this.plugin = plugin;
    }

    public void enable(ErrorReportingConfig config) {
        this.config = config;
        if (!config.isEnabled()) {
            plugin.getLogger().info("[error-reporting] Desabilitado (error-reporting.enabled: false).");
            return;
        }
        if (!config.hasOutboundChannel()) {
            plugin.getLogger().warning("[error-reporting] Habilitado mas nenhum canal outbound configurado "
                    + "(github.token ou webhook.url).");
            return;
        }
        this.deduplicator = new ErrorDeduplicator(config.getDedupeMinutes());
        this.githubReporter = new GitHubIssuesReporter(plugin, config);
        this.webhookReporter = new WebhookReporter(plugin, config);
        plugin.getLogger().info("[error-reporting] Ativo — dedupe " + config.getDedupeMinutes()
                + " min, GitHub=" + config.isGithubConfigured() + ", Webhook=" + config.isWebhookConfigured());
    }

    public void reload(ErrorReportingConfig config) {
        enable(config);
    }

    public boolean isActive() {
        return config != null && config.isEnabled() && config.hasOutboundChannel() && deduplicator != null;
    }

    public ErrorReportingConfig getConfig() {
        return config;
    }

    public void report(String source, String pluginName, String threadName, Throwable throwable, String message) {
        if (!isActive()) {
            return;
        }
        if (!config.acceptsPlugin(pluginName)) {
            return;
        }
        Throwable root = StackTraceUtil.unwrap(throwable);
        String safeMessage = StackTraceUtil.redact(message == null && root != null ? root.getMessage() : message);
        String stackTrace = StackTraceUtil.formatThrowable(root != null ? root : throwable);
        if (stackTrace.isBlank() && safeMessage.isBlank()) {
            return;
        }
        String fingerprint = StackTraceUtil.fingerprint(root != null ? root : throwable, safeMessage);
        if (!deduplicator.shouldReport(fingerprint)) {
            return;
        }

        ErrorReport report = new ErrorReport(
                source,
                pluginName,
                threadName,
                safeMessage,
                stackTrace,
                fingerprint,
                Instant.now()
        );

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> dispatch(report));
    }

    public void reportLogError(String loggerName, String threadName, Throwable throwable, String message) {
        String pluginName = inferPluginFromLogger(loggerName);
        report("log4j", pluginName, threadName, throwable, message);
    }

    private void dispatch(ErrorReport report) {
        try {
            if (config.isGithubConfigured()) {
                githubReporter.send(report);
            }
            if (config.isWebhookConfigured()) {
                webhookReporter.send(report);
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING,
                    "[error-reporting] Falha ao despachar report: " + e.getMessage(), e);
        }
    }

    private static String inferPluginFromLogger(String loggerName) {
        if (loggerName == null || loggerName.isBlank()) {
            return "unknown";
        }
        if (loggerName.contains(".")) {
            String[] parts = loggerName.split("\\.");
            if (parts.length >= 2) {
                return parts[parts.length - 1];
            }
        }
        return loggerName;
    }
}
