package dev.daniel730.rpgserver.config;

import java.util.Locale;

/**
 * Where short-lived quest feedback (progress / complete pulses) is shown.
 * Prefer {@link #CHAT} or {@link #AUTO} when AuraSkills owns the ActionBar.
 */
public enum TransientHudChannel {
    /** Chat when AuraSkills is present, otherwise ActionBar. */
    AUTO,
    CHAT,
    ACTIONBAR,
    NONE;

    public static TransientHudChannel fromConfig(String raw) {
        if (raw == null || raw.isBlank()) {
            return AUTO;
        }
        String key = raw.trim().toLowerCase(Locale.ROOT).replace('-', '_');
        return switch (key) {
            case "chat", "message", "messages" -> CHAT;
            case "actionbar", "action_bar" -> ACTIONBAR;
            case "off", "none", "disabled" -> NONE;
            default -> AUTO;
        };
    }

    public boolean usesActionBar(boolean auraSkillsPresent) {
        return this == ACTIONBAR || (this == AUTO && !auraSkillsPresent);
    }

    public boolean usesChat(boolean auraSkillsPresent) {
        return this == CHAT || (this == AUTO && auraSkillsPresent);
    }
}
