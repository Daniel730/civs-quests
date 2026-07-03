package dev.daniel730.rpgserver.config;

import java.util.Locale;

public enum TrackedHudMode {
    NONE,
    BOSSBAR,
    SCOREBOARD,
    BOTH;

    public static TrackedHudMode fromConfig(String value) {
        if (value == null || value.isBlank()) {
            return BOTH;
        }
        return switch (value.toLowerCase(Locale.ROOT)) {
            case "bossbar", "boss_bar" -> BOSSBAR;
            case "scoreboard", "sidebar" -> SCOREBOARD;
            case "both" -> BOTH;
            case "none", "off", "false" -> NONE;
            default -> BOTH;
        };
    }

    public boolean showBossBar() {
        return this == BOSSBAR || this == BOTH;
    }

    public boolean showScoreboard() {
        return this == SCOREBOARD || this == BOTH;
    }
}
