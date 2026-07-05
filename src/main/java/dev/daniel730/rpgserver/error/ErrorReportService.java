package dev.daniel730.rpgserver.error;

import dev.daniel730.rpgserver.RpgServerPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.logging.Level;

public final class ErrorReportService {

    private final RpgServerPlugin plugin;
    private ErrorReportingConfig config;
    private ErrorDeduplicator deduplicator;
    private LocalFileErrorReporter localReporter;
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
        if (!config.hasAnyChannel()) {
            plugin.getLogger().warning("[error-reporting] Habilitado mas nenhum canal configurado "
                    + "(local.directory, github.token ou webhook.url).");
            return;
        }
        this.deduplicator = new ErrorDeduplicator(config.getDedupeMinutes());
        this.localReporter = new LocalFileErrorReporter(plugin, config);
        this.githubReporter = new GitHubIssuesReporter(plugin, config);
        this.webhookReporter = new WebhookReporter(plugin, config);
        plugin.getLogger().info("[error-reporting] Ativo — dedupe " + config.getDedupeMinutes()
                + " min, Local=" + config.isLocalConfigured()
                + ", GitHub=" + config.isGithubConfigured()
                + ", Webhook=" + config.isWebhookConfigured());
    }

    public void reload(ErrorReportingConfig config) {
        enable(config);
    }

    public boolean isActive() {
        return config != null && config.isEnabled() && config.hasAnyChannel() && deduplicator != null;
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
                buildRuntimeContext(),
                fingerprint,
                Instant.now()
        );

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> dispatch(report));
    }

    public void reportLogError(String loggerName, String threadName, Throwable throwable, String message) {
        String pluginName = inferPluginFromThrowable(throwable);
        if (pluginName.equals("unknown")) {
            pluginName = inferPluginFromLogger(loggerName);
        }
        report("log4j", pluginName, threadName, throwable, message);
    }

    private void dispatch(ErrorReport report) {
        if (config.isLocalConfigured()) {
            try {
                localReporter.send(report);
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING,
                        "[error-reporting] Falha ao gravar report local: " + e.getMessage(), e);
            }
        }
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

    public List<Path> getRecentLocalReports(int limit) {
        if (localReporter == null) {
            ErrorReportingConfig effectiveConfig = config;
            if (effectiveConfig == null) {
                effectiveConfig = new ErrorReportingConfig(plugin.getConfig());
            }
            localReporter = new LocalFileErrorReporter(plugin, effectiveConfig);
        }
        return localReporter.recentReports(limit);
    }

    public Path getLocalReportDirectory() {
        if (localReporter == null) {
            ErrorReportingConfig effectiveConfig = config;
            if (effectiveConfig == null) {
                effectiveConfig = new ErrorReportingConfig(plugin.getConfig());
            }
            localReporter = new LocalFileErrorReporter(plugin, effectiveConfig);
        }
        return localReporter.getReportDirectory();
    }

    private String buildRuntimeContext() {
        StringBuilder context = new StringBuilder();
        context.append("dataFolder=").append(plugin.getDataFolder().getAbsolutePath()).append('\n');
        context.append("serverPrimaryThread=").append(Bukkit.isPrimaryThread()).append('\n');
        context.append("onlinePlayers=").append(Bukkit.getOnlinePlayers().size()).append('\n');
        if (!Bukkit.isPrimaryThread()) {
            context.append("playerDetails=skipped (not on primary thread)\n");
            return context.toString();
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            context.append("- player=").append(player.getName())
                    .append(" uuid=").append(player.getUniqueId())
                    .append(" world=").append(player.getWorld().getName())
                    .append(" xyz=")
                    .append(player.getLocation().getBlockX()).append(',')
                    .append(player.getLocation().getBlockY()).append(',')
                    .append(player.getLocation().getBlockZ());
            try {
                var profile = plugin.getProfileManager().getOrCreate(player);
                var activeQuest = plugin.getQuestManager().findPrimaryActiveQuest(profile);
                context.append(" activeQuest=")
                        .append(activeQuest == null ? "none" : activeQuest.getId());
            } catch (Exception ex) {
                context.append(" activeQuest=unavailable(").append(ex.getClass().getSimpleName()).append(')');
            }
            try {
                context.append(" openInventory=\"")
                        .append(StackTraceUtil.redact(player.getOpenInventory().getTitle()))
                        .append('"');
            } catch (Exception ex) {
                context.append(" openInventory=unavailable(").append(ex.getClass().getSimpleName()).append(')');
            }
            context.append('\n');
        }
        return context.toString();
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

    private static String inferPluginFromThrowable(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            for (StackTraceElement element : current.getStackTrace()) {
                String className = element.getClassName();
                if (className.startsWith("dev.daniel730.rpgserver.")) {
                    return "RPGServer";
                }
                if (className.startsWith("org.redcastlemedia.multitallented.civs.")) {
                    return "Civs";
                }
            }
            Throwable cause = current.getCause();
            if (cause == current) {
                break;
            }
            current = cause;
        }
        return "unknown";
    }
}
