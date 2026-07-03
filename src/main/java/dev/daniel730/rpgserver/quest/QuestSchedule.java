package dev.daniel730.rpgserver.quest;

import java.util.Locale;

public enum QuestSchedule {
    NONE,
    DAILY,
    WEEKLY;

    public static QuestSchedule fromYaml(String value) {
        if (value == null || value.isBlank()) {
            return NONE;
        }
        return switch (value.toLowerCase(Locale.ROOT)) {
            case "daily", "diaria", "diário" -> DAILY;
            case "weekly", "semanal" -> WEEKLY;
            default -> NONE;
        };
    }

    public String displayName() {
        return switch (this) {
            case DAILY -> "Diária";
            case WEEKLY -> "Semanal";
            case NONE -> "";
        };
    }
}
