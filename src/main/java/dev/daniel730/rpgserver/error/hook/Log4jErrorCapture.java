package dev.daniel730.rpgserver.error.hook;

import dev.daniel730.rpgserver.error.ErrorReportService;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;

/**
 * Log4j2 appender registered at runtime (not via plugin annotation scan).
 */
public final class Log4jErrorCapture extends AbstractAppender {

    private static final String APPENDER_NAME = "RpgErrorReporter";

    private final ErrorReportService errorReportService;

    private Log4jErrorCapture(ErrorReportService errorReportService) {
        super(APPENDER_NAME, null, PatternLayout.createDefaultLayout(), true, Property.EMPTY_ARRAY);
        this.errorReportService = errorReportService;
    }

    public static Log4jErrorCapture install(ErrorReportService errorReportService) {
        Log4jErrorCapture appender = new Log4jErrorCapture(errorReportService);
        appender.start();
        return appender;
    }

    @Override
    public void append(LogEvent event) {
        if (event == null || !event.getLevel().isMoreSpecificThan(Level.ERROR)) {
            return;
        }
        Throwable throwable = event.getThrown();
        if (throwable == null) {
            return;
        }
        String loggerName = event.getLoggerName();
        String threadName = event.getThreadName();
        String message = event.getMessage() != null ? event.getMessage().getFormattedMessage() : null;
        errorReportService.reportLogError(loggerName, threadName, throwable, message);
    }

    @PluginFactory
    public static Log4jErrorCapture createAppender(
            @PluginAttribute("name") String name,
            @PluginAttribute(value = "ignoreExceptions", defaultBoolean = true) boolean ignoreExceptions) {
        return null;
    }
}
