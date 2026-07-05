package dev.daniel730.rpgserver.error;

import dev.daniel730.rpgserver.RpgServerPlugin;
import dev.daniel730.rpgserver.error.hook.Log4jErrorCapture;
import dev.daniel730.rpgserver.error.hook.UncaughtExceptionBridge;
import dev.daniel730.rpgserver.error.listener.ServerExceptionListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import java.util.logging.Level;

public final class ErrorReportingManager {

    private static final String LOG4J_APPENDER_NAME = "RpgErrorReporter";

    private final RpgServerPlugin plugin;
    private final ErrorReportService reportService;
    private ErrorReportingConfig config;
    private ServerExceptionListener serverExceptionListener;
    private Log4jErrorCapture log4jAppender;

    public ErrorReportingManager(RpgServerPlugin plugin) {
        this.plugin = plugin;
        this.reportService = new ErrorReportService(plugin);
    }

    public ErrorReportService getReportService() {
        return reportService;
    }

    public void enable(ErrorReportingConfig config) {
        disable();
        this.config = config;
        reportService.enable(config);
        if (!reportService.isActive()) {
            return;
        }
        if (config.isPaperServerException()) {
            serverExceptionListener = new ServerExceptionListener(reportService);
            plugin.getServer().getPluginManager().registerEvents(serverExceptionListener, plugin);
        }
        if (config.isUncaughtThreads()) {
            UncaughtExceptionBridge.install(reportService);
        }
        if (config.isLog4jErrors()) {
            attachLog4jAppender();
        }
    }

    public void reload(ErrorReportingConfig config) {
        enable(config);
    }

    public void disable() {
        if (serverExceptionListener != null) {
            HandlerList.unregisterAll(serverExceptionListener);
            serverExceptionListener = null;
        }
        UncaughtExceptionBridge.uninstall();
        detachLog4jAppender();
        config = null;
    }

    private void attachLog4jAppender() {
        try {
            detachLog4jAppender();
            LoggerContext context = (LoggerContext) LogManager.getContext(false);
            Configuration configuration = context.getConfiguration();
            log4jAppender = Log4jErrorCapture.install(reportService);
            LoggerConfig root = configuration.getRootLogger();
            root.addAppender(log4jAppender, org.apache.logging.log4j.Level.ERROR, null);
            context.updateLoggers();
        } catch (Exception | LinkageError e) {
            plugin.getLogger().log(Level.WARNING,
                    "[error-reporting] Não foi possível anexar appender Log4j: " + e.getMessage());
            log4jAppender = null;
        }
    }

    private void detachLog4jAppender() {
        if (log4jAppender == null) {
            return;
        }
        try {
            LoggerContext context = (LoggerContext) LogManager.getContext(false);
            Configuration configuration = context.getConfiguration();
            LoggerConfig root = configuration.getRootLogger();
            Appender existing = root.getAppenders().get(LOG4J_APPENDER_NAME);
            if (existing != null) {
                root.removeAppender(LOG4J_APPENDER_NAME);
            }
            log4jAppender.stop();
            context.updateLoggers();
        } catch (Exception | LinkageError ignored) {
            // best effort cleanup
        } finally {
            log4jAppender = null;
        }
    }
}
