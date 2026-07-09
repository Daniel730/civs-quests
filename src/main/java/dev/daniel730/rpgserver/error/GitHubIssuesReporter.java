package dev.daniel730.rpgserver.error;

import dev.daniel730.rpgserver.RpgServerPlugin;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.logging.Level;

public final class GitHubIssuesReporter {

    private final RpgServerPlugin plugin;
    private final ErrorReportingConfig config;
    private final HttpClient httpClient;

    public GitHubIssuesReporter(RpgServerPlugin plugin, ErrorReportingConfig config) {
        this.plugin = plugin;
        this.config = config;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .build();
    }

    public void send(ErrorReport report) {
        if (!config.isGithubConfigured()) {
            return;
        }
        try {
            String title = buildTitle(report);
            String body = buildBody(report);
            String json = buildIssueJson(title, body, config.getGithubLabels());
            URI uri = URI.create("https://api.github.com/repos/"
                    + config.getGithubOwner() + "/" + config.resolveGithubRepo(report.getPluginName()) + "/issues");

            HttpRequest request = HttpRequest.newBuilder(uri)
                    .timeout(Duration.ofSeconds(30))
                    .header("Accept", "application/vnd.github+json")
                    .header("Authorization", "Bearer " + config.getGithubToken())
                    .header("X-GitHub-Api-Version", "2022-11-28")
                    .header("User-Agent", "RPGServer-ErrorReporter")
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                plugin.getLogger().info("[error-reporting] GitHub issue criada para "
                        + report.getPluginName() + " (" + report.getFingerprint() + ").");
            } else {
                plugin.getLogger().log(Level.WARNING,
                        "[error-reporting] GitHub API respondeu " + response.statusCode() + ": "
                                + StackTraceUtil.redact(response.body()));
            }
        } catch (java.net.ConnectException | java.net.NoRouteToHostException | java.net.UnknownHostException e) {
            plugin.getLogger().fine("[error-reporting] GitHub indisponível (rede): " + e.getMessage());
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING,
                    "[error-reporting] Falha ao criar issue no GitHub: " + e.getMessage(), e);
        }
    }

    private static String buildTitle(ErrorReport report) {
        String exception = extractExceptionName(report.getStackTrace(), report.getMessage());
        return "[server-error] " + exception + " in " + report.getPluginName();
    }

    private String buildBody(ErrorReport report) {
        return """
                ## Server Error Report

                | Campo | Valor |
                |-------|-------|
                | **Servidor** | %s |
                | **Fonte** | %s |
                | **Plugin** | %s |
                | **Thread** | %s |
                | **Timestamp** | %s |
                | **Fingerprint** | `%s` |

                ### Mensagem

                ```
                %s
                ```

                ### Stack trace

                ```
                %s
                ```

                ### Runtime context

                ```
                %s
                ```

                ---
                *Reportado automaticamente pelo RPG Server error-reporting.*
                """.formatted(
                config.getServerName(),
                report.getSource(),
                report.getPluginName(),
                report.getThreadName(),
                report.getTimestamp(),
                report.getFingerprint(),
                report.getMessage(),
                report.getStackTrace(),
                report.getContext()
        );
    }

    private static String extractExceptionName(String stackTrace, String message) {
        if (stackTrace != null) {
            int colon = stackTrace.indexOf(':');
            if (colon > 0) {
                String firstLine = stackTrace.substring(0, colon).trim();
                int dot = firstLine.lastIndexOf('.');
                if (dot >= 0 && dot < firstLine.length() - 1) {
                    return firstLine.substring(dot + 1);
                }
            }
        }
        if (message != null && !message.isBlank()) {
            return message.length() > 60 ? message.substring(0, 57) + "..." : message;
        }
        return "Exception";
    }

    private static String buildIssueJson(String title, String body, List<String> labels) {
        StringBuilder json = new StringBuilder();
        json.append("{\"title\":").append(jsonString(title))
                .append(",\"body\":").append(jsonString(body));
        if (labels != null && !labels.isEmpty()) {
            json.append(",\"labels\":[");
            for (int i = 0; i < labels.size(); i++) {
                if (i > 0) {
                    json.append(',');
                }
                json.append(jsonString(labels.get(i)));
            }
            json.append(']');
        }
        json.append('}');
        return json.toString();
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
