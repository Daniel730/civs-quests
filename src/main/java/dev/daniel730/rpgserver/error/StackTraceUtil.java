package dev.daniel730.rpgserver.error;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.regex.Pattern;

public final class StackTraceUtil {

    private static final Pattern[] REDACT_WITH_PREFIX = {
            Pattern.compile("(?i)(authorization:\\s*)\\S+", Pattern.MULTILINE),
            Pattern.compile("(?i)(bearer\\s+)\\S+", Pattern.MULTILINE),
            Pattern.compile("(?i)(token[=:]\\s*)\\S+", Pattern.MULTILINE),
            Pattern.compile("(?i)(password[=:]\\s*)\\S+", Pattern.MULTILINE),
            Pattern.compile("(?i)(secret[=:]\\s*)\\S+", Pattern.MULTILINE),
            Pattern.compile("(?i)(api[_-]?key[=:]\\s*)\\S+", Pattern.MULTILINE),
    };

    private static final Pattern[] REDACT_FULL = {
            Pattern.compile("ghp_[A-Za-z0-9]{20,}", Pattern.MULTILINE),
            Pattern.compile("github_pat_[A-Za-z0-9_]{20,}", Pattern.MULTILINE),
            Pattern.compile("gho_[A-Za-z0-9]{20,}", Pattern.MULTILINE),
            Pattern.compile("ghu_[A-Za-z0-9]{20,}", Pattern.MULTILINE),
            Pattern.compile("ghs_[A-Za-z0-9]{20,}", Pattern.MULTILINE),
            Pattern.compile("ghr_[A-Za-z0-9]{20,}", Pattern.MULTILINE),
    };

    private StackTraceUtil() {
    }

    public static String formatThrowable(Throwable throwable) {
        if (throwable == null) {
            return "";
        }
        StringWriter writer = new StringWriter();
        throwable.printStackTrace(new PrintWriter(writer));
        return redact(writer.toString());
    }

    public static String redact(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        String result = text;
        for (Pattern pattern : REDACT_WITH_PREFIX) {
            result = pattern.matcher(result).replaceAll("$1[REDACTED]");
        }
        for (Pattern pattern : REDACT_FULL) {
            result = pattern.matcher(result).replaceAll("[REDACTED_GITHUB_TOKEN]");
        }
        return result;
    }

    public static String fingerprint(Throwable throwable, String message) {
        StringBuilder seed = new StringBuilder();
        if (throwable != null) {
            seed.append(throwable.getClass().getName());
            StackTraceElement[] stack = throwable.getStackTrace();
            if (stack.length > 0) {
                StackTraceElement top = stack[0];
                seed.append('|').append(top.getClassName())
                        .append('.').append(top.getMethodName())
                        .append(':').append(top.getLineNumber());
            }
            Throwable cause = throwable.getCause();
            if (cause != null && cause != throwable) {
                seed.append('|').append(cause.getClass().getName());
            }
        }
        if (message != null && !message.isBlank()) {
            seed.append('|').append(message.trim());
        }
        return sha256Short(seed.toString());
    }

    public static String shortExceptionName(Throwable throwable) {
        if (throwable == null) {
            return "Exception";
        }
        String name = throwable.getClass().getSimpleName();
        return name.isEmpty() ? throwable.getClass().getName() : name;
    }

    public static Throwable unwrap(Throwable throwable) {
        Throwable current = throwable;
        while (current != null && current.getCause() != null && current.getCause() != current) {
            current = current.getCause();
        }
        return current == null ? throwable : current;
    }

    private static String sha256Short(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash, 0, 8);
        } catch (NoSuchAlgorithmException e) {
            return Integer.toHexString(input.hashCode());
        }
    }
}
