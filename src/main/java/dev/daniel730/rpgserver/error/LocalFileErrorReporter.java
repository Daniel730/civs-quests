package dev.daniel730.rpgserver.error;

import dev.daniel730.rpgserver.RpgServerPlugin;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

public final class LocalFileErrorReporter {

    private static final DateTimeFormatter FILE_TIME =
            DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss", Locale.ROOT).withZone(ZoneOffset.UTC);

    private final RpgServerPlugin plugin;
    private final ErrorReportingConfig config;

    public LocalFileErrorReporter(RpgServerPlugin plugin, ErrorReportingConfig config) {
        this.plugin = plugin;
        this.config = config;
    }

    public Path send(ErrorReport report) throws IOException {
        if (!config.isLocalConfigured()) {
            return null;
        }
        Path directory = getReportDirectory();
        Files.createDirectories(directory);

        String fileName = FILE_TIME.format(report.getTimestamp()) + "-"
                + safeFilePart(report.getPluginName()) + "-"
                + safeFilePart(report.getFingerprint()) + ".md";
        Path reportPath = directory.resolve(fileName);
        String markdown = buildMarkdown(report);
        Files.writeString(reportPath, markdown, StandardCharsets.UTF_8);
        Files.writeString(directory.resolve("latest.md"), markdown, StandardCharsets.UTF_8);
        pruneOldReports(directory);
        plugin.getLogger().info("[error-reporting] Report local gravado: " + reportPath.toAbsolutePath());
        return reportPath;
    }

    public Path getReportDirectory() {
        Path configured = Path.of(config.getLocalDirectory());
        if (configured.isAbsolute()) {
            return configured;
        }
        return plugin.getDataFolder().toPath().resolve(configured);
    }

    public List<Path> recentReports(int limit) {
        Path directory = getReportDirectory();
        if (!Files.isDirectory(directory)) {
            return List.of();
        }
        try (Stream<Path> stream = Files.list(directory)) {
            return stream
                    .filter(path -> path.getFileName().toString().endsWith(".md"))
                    .filter(path -> !path.getFileName().toString().equals("latest.md"))
                    .sorted(Comparator.comparingLong(LocalFileErrorReporter::lastModified).reversed())
                    .limit(Math.max(1, limit))
                    .toList();
        } catch (IOException ignored) {
            return List.of();
        }
    }

    private String buildMarkdown(ErrorReport report) {
        return """
                # Minecraft Server Error Report

                ## Summary

                | Field | Value |
                |-------|-------|
                | Server | %s |
                | Source | %s |
                | Plugin | %s |
                | Thread | %s |
                | Timestamp | %s |
                | Fingerprint | `%s` |

                ## Message

                ```text
                %s
                ```

                ## Runtime Context

                ```text
                %s
                ```

                ## Relevant Config

                ```yaml
                %s
                ```

                ## Stack Trace

                ```text
                %s
                ```

                ## Cursor Fix Prompt

                ```text
                Leia este report em plugins/RPGServer/%s e corrija a causa raiz no repo Civs/RPG.
                Foque no primeiro frame do stacktrace que pertence a dev.daniel730.rpgserver ou org.redcastlemedia.multitallented.civs.
                Rode mvn compile no repo alterado e explique a causa do erro antes de sugerir deploy.
                ```

                ## Suggested Fix Hints

                - Check plugin ownership first: RPG orchestrates quests/hooks; Civs owns towns, regions, menus, custom mobs and territorial systems.
                - Start from the first project frame in the stacktrace, then inspect the caller path and config/YAML named in the message.
                - If the context lists an open GUI or active quest, validate that state transition before changing shared managers.
                - Preserve redacted secrets and do not paste tokens into issues or commits.
                """.formatted(
                config.getServerName(),
                report.getSource(),
                report.getPluginName(),
                report.getThreadName(),
                report.getTimestamp(),
                report.getFingerprint(),
                blankFallback(report.getMessage(), "(sem mensagem)"),
                blankFallback(report.getContext(), "(contexto indisponivel)"),
                buildConfigSnippet(),
                blankFallback(report.getStackTrace(), "(sem stacktrace)"),
                config.getLocalDirectory() + "/latest.md"
        );
    }

    private String buildConfigSnippet() {
        return """
                error-reporting:
                  enabled: %s
                  server-name: %s
                  dedupe-minutes: %d
                  include-plugins: %s
                  sources:
                    paper-server-exception: %s
                    uncaught-threads: %s
                    log4j-errors: %s
                  local:
                    enabled: %s
                    directory: %s
                    keep-last: %d
                  github:
                    enabled: %s
                    configured: %s
                  webhook:
                    enabled: %s
                    configured: %s
                """.formatted(
                config.isEnabled(),
                config.getServerName(),
                config.getDedupeMinutes(),
                config.getIncludePlugins(),
                config.isPaperServerException(),
                config.isUncaughtThreads(),
                config.isLog4jErrors(),
                config.isLocalEnabled(),
                config.getLocalDirectory(),
                config.getLocalKeepLast(),
                config.isGithubEnabled(),
                config.isGithubConfigured(),
                config.isWebhookEnabled(),
                config.isWebhookConfigured()
        );
    }

    private void pruneOldReports(Path directory) throws IOException {
        List<Path> reports = recentReports(config.getLocalKeepLast() + 25);
        if (reports.size() <= config.getLocalKeepLast()) {
            return;
        }
        for (int i = config.getLocalKeepLast(); i < reports.size(); i++) {
            Files.deleteIfExists(reports.get(i));
        }
    }

    private static long lastModified(Path path) {
        try {
            return Files.getLastModifiedTime(path).toMillis();
        } catch (IOException ignored) {
            return 0L;
        }
    }

    private static String safeFilePart(String value) {
        if (value == null || value.isBlank()) {
            return "unknown";
        }
        return value.replaceAll("[^A-Za-z0-9._-]", "_");
    }

    private static String blankFallback(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
