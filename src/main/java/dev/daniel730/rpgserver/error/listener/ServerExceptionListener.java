package dev.daniel730.rpgserver.error.listener;

import com.destroystokyo.paper.event.server.ServerExceptionEvent;
import com.destroystokyo.paper.exception.ServerException;
import com.destroystokyo.paper.exception.ServerPluginException;
import dev.daniel730.rpgserver.error.ErrorReportService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

public final class ServerExceptionListener implements Listener {

    private final ErrorReportService errorReportService;

    public ServerExceptionListener(ErrorReportService errorReportService) {
        this.errorReportService = errorReportService;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onServerException(ServerExceptionEvent event) {
        ServerException serverException = event.getException();
        if (serverException == null) {
            return;
        }
        Throwable cause = serverException.getCause() != null ? serverException.getCause() : serverException;
        String pluginName = resolvePluginName(serverException);
        errorReportService.report(
                "paper-server-exception",
                pluginName,
                Thread.currentThread().getName(),
                cause,
                serverException.getMessage()
        );
    }

    private static String resolvePluginName(ServerException serverException) {
        if (serverException instanceof ServerPluginException pluginException) {
            Plugin plugin = pluginException.getResponsiblePlugin();
            if (plugin != null) {
                return plugin.getName();
            }
        }
        return "unknown";
    }
}
