package dev.daniel730.rpgserver.error;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

public final class ErrorReportingConfig {

    private final boolean enabled;
    private final String serverName;
    private final int dedupeMinutes;
    private final List<String> includePlugins;
    private final boolean paperServerException;
    private final boolean uncaughtThreads;
    private final boolean log4jErrors;
    private final boolean githubEnabled;
    private final String githubToken;
    private final String githubOwner;
    private final String githubRepo;
    private final String githubCivsRepo;
    private final List<String> githubLabels;
    private final boolean localEnabled;
    private final String localDirectory;
    private final int localKeepLast;
    private final boolean webhookEnabled;
    private final String webhookUrl;

    public ErrorReportingConfig(FileConfiguration config) {
        this.enabled = config.getBoolean("error-reporting.enabled", false);
        this.serverName = config.getString("error-reporting.server-name", "minecraft-server");
        this.dedupeMinutes = Math.max(1, config.getInt("error-reporting.dedupe-minutes", 15));
        this.includePlugins = config.getStringList("error-reporting.include-plugins");
        this.paperServerException = config.getBoolean("error-reporting.sources.paper-server-exception", true);
        this.uncaughtThreads = config.getBoolean("error-reporting.sources.uncaught-threads", true);
        this.log4jErrors = config.getBoolean("error-reporting.sources.log4j-errors", true);
        this.githubEnabled = config.getBoolean("error-reporting.github.enabled", false);
        this.githubToken = resolveGithubToken(config);
        this.githubOwner = config.getString("error-reporting.github.owner", "Daniel730");
        this.githubRepo = config.getString("error-reporting.github.repo", "civs-quests");
        this.githubCivsRepo = config.getString("error-reporting.github.civs-repo", "Civs");
        this.githubLabels = config.getStringList("error-reporting.github.labels");
        this.localEnabled = config.getBoolean("error-reporting.local.enabled", true);
        this.localDirectory = config.getString("error-reporting.local.directory", "error-reports");
        this.localKeepLast = Math.max(1, config.getInt("error-reporting.local.keep-last", 50));
        this.webhookEnabled = config.getBoolean("error-reporting.webhook.enabled", false);
        this.webhookUrl = config.getString("error-reporting.webhook.url", "");
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean hasOutboundChannel() {
        return isGithubConfigured() || isWebhookConfigured();
    }

    public boolean hasAnyChannel() {
        return isLocalConfigured() || hasOutboundChannel();
    }

    public boolean isGithubConfigured() {
        return githubEnabled && githubToken != null && !githubToken.isBlank()
                && !githubToken.contains("REPLACE") && !githubToken.contains("CHANGE_ME");
    }

    public boolean isWebhookConfigured() {
        return webhookEnabled && webhookUrl != null && !webhookUrl.isBlank();
    }

    public boolean isLocalConfigured() {
        return localEnabled && localDirectory != null && !localDirectory.isBlank();
    }

    public String getServerName() {
        return serverName;
    }

    public int getDedupeMinutes() {
        return dedupeMinutes;
    }

    public List<String> getIncludePlugins() {
        return includePlugins == null ? List.of() : includePlugins;
    }

    public boolean acceptsPlugin(String pluginName) {
        if (pluginName == null || pluginName.isBlank()) {
            return true;
        }
        List<String> allowed = getIncludePlugins();
        if (allowed.isEmpty()) {
            return true;
        }
        String normalized = pluginName.toLowerCase(Locale.ROOT);
        for (String entry : allowed) {
            if (entry != null && normalized.equals(entry.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    public boolean isPaperServerException() {
        return paperServerException;
    }

    public boolean isUncaughtThreads() {
        return uncaughtThreads;
    }

    public boolean isLog4jErrors() {
        return log4jErrors;
    }

    public boolean isGithubEnabled() {
        return githubEnabled;
    }

    public String getGithubToken() {
        return githubToken;
    }

    public String getGithubOwner() {
        return githubOwner;
    }

    public String getGithubRepo() {
        return githubRepo;
    }

    public String getGithubCivsRepo() {
        return githubCivsRepo;
    }

    public String resolveGithubRepo(String pluginName) {
        if (pluginName != null && pluginName.equalsIgnoreCase("Civs")) {
            return githubCivsRepo;
        }
        return githubRepo;
    }

    public List<String> getGithubLabels() {
        return githubLabels == null || githubLabels.isEmpty()
                ? List.of("server-error")
                : Collections.unmodifiableList(githubLabels);
    }

    public boolean isLocalEnabled() {
        return localEnabled;
    }

    public String getLocalDirectory() {
        return localDirectory;
    }

    public int getLocalKeepLast() {
        return localKeepLast;
    }

    public boolean isWebhookEnabled() {
        return webhookEnabled;
    }

    public String getWebhookUrl() {
        return webhookUrl;
    }

    private static String resolveGithubToken(FileConfiguration config) {
        String inline = config.getString("error-reporting.github.token", "");
        if (inline != null && !inline.isBlank() && !inline.contains("REPLACE") && !inline.contains("CHANGE_ME")) {
            return inline.trim();
        }
        String tokenFile = config.getString("error-reporting.github.token-file", "");
        if (tokenFile == null || tokenFile.isBlank()) {
            return inline == null ? "" : inline;
        }
        java.io.File file = new java.io.File(tokenFile);
        if (!file.isFile() || !file.canRead()) {
            return "";
        }
        try {
            String content = java.nio.file.Files.readString(file.toPath()).trim();
            return content.isBlank() ? "" : content;
        } catch (Exception ignored) {
            return "";
        }
    }
}
