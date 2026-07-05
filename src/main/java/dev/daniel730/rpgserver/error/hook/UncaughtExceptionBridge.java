package dev.daniel730.rpgserver.error.hook;

import dev.daniel730.rpgserver.error.ErrorReportService;

public final class UncaughtExceptionBridge implements Thread.UncaughtExceptionHandler {

    private final ErrorReportService errorReportService;
    private final Thread.UncaughtExceptionHandler previousHandler;

    public UncaughtExceptionBridge(ErrorReportService errorReportService,
                                   Thread.UncaughtExceptionHandler previousHandler) {
        this.errorReportService = errorReportService;
        this.previousHandler = previousHandler;
    }

    public static void install(ErrorReportService errorReportService) {
        Thread.UncaughtExceptionHandler previous = Thread.getDefaultUncaughtExceptionHandler();
        if (previous instanceof UncaughtExceptionBridge) {
            return;
        }
        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionBridge(errorReportService, previous));
    }

    public static void uninstall() {
        Thread.UncaughtExceptionHandler current = Thread.getDefaultUncaughtExceptionHandler();
        if (current instanceof UncaughtExceptionBridge bridge) {
            Thread.setDefaultUncaughtExceptionHandler(bridge.previousHandler);
        }
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        errorReportService.report(
                "uncaught-thread",
                "unknown",
                thread != null ? thread.getName() : "unknown",
                throwable,
                throwable != null ? throwable.getMessage() : null
        );
        if (previousHandler != null) {
            previousHandler.uncaughtException(thread, throwable);
        }
    }
}
