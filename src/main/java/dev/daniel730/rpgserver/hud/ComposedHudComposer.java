package dev.daniel730.rpgserver.hud;

import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Pure string composition for the unified ActionBar HUD (testable without Bukkit).
 * Placeholders use {@code {key}} form; runtime resolves PAPI {@code %...%} separately.
 */
public final class ComposedHudComposer {

    private static final Pattern TOKEN = Pattern.compile("\\{([a-z0-9_]+)}", Pattern.CASE_INSENSITIVE);

    private ComposedHudComposer() {
    }

    public static String compose(String template, Map<String, String> values) {
        if (template == null || template.isBlank()) {
            return "";
        }
        Matcher matcher = TOKEN.matcher(template);
        StringBuffer out = new StringBuffer();
        while (matcher.find()) {
            String key = matcher.group(1).toLowerCase(Locale.ROOT);
            String replacement = values.getOrDefault(key, "");
            if (replacement == null) {
                replacement = "";
            }
            matcher.appendReplacement(out, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(out);
        return out.toString().replaceAll("\\s{2,}", " ").trim();
    }

    public static String compactBar(int current, int max, int length) {
        if (length <= 0) {
            return "";
        }
        int safeMax = Math.max(1, max);
        int filled = (int) Math.round((double) Math.max(0, current) / safeMax * length);
        filled = Math.min(length, Math.max(0, filled));
        return "|".repeat(filled) + ".".repeat(length - filled);
    }
}
