package dev.daniel730.rpgserver.util;

public final class ProgressBarUtil {

    private static final int DEFAULT_LENGTH = 10;

    private ProgressBarUtil() {
    }

    public static String miniMessageBar(int current, int total) {
        return miniMessageBar(current, total, DEFAULT_LENGTH);
    }

    public static String miniMessageBar(int current, int total, int length) {
        if (length <= 0) {
            return "";
        }
        int filled;
        if (total <= 0) {
            filled = length;
        } else {
            filled = (int) Math.round((double) current / total * length);
            filled = Math.min(length, Math.max(0, filled));
        }
        int empty = length - filled;
        return "<green>" + "█".repeat(filled) + "</green><dark_gray>" + "░".repeat(empty) + "</dark_gray>";
    }

    public static int percent(int current, int total) {
        if (total <= 0) {
            return 100;
        }
        return Math.min(100, Math.max(0, (int) Math.round((double) current / total * 100)));
    }
}
