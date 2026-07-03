package dev.daniel730.rpgserver.util;

import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;

import java.util.Locale;

public final class ArchetypeUtil {

    private ArchetypeUtil() {
    }

    public static Material glassPane(String archetype) {
        if (archetype == null || archetype.isBlank()) {
            return Material.GRAY_STAINED_GLASS_PANE;
        }
        return switch (archetype.toLowerCase(Locale.ROOT)) {
            case "warrior" -> Material.RED_STAINED_GLASS_PANE;
            case "merchant" -> Material.YELLOW_STAINED_GLASS_PANE;
            case "builder" -> Material.LIME_STAINED_GLASS_PANE;
            default -> Material.PURPLE_STAINED_GLASS_PANE;
        };
    }

    public static String miniColorTag(String archetype) {
        if (archetype == null || archetype.isBlank()) {
            return "<gray>";
        }
        return switch (archetype.toLowerCase(Locale.ROOT)) {
            case "warrior" -> "<red>";
            case "merchant" -> "<gold>";
            case "builder" -> "<green>";
            default -> "<light_purple>";
        };
    }

    public static String miniColorCloseTag(String archetype) {
        if (archetype == null || archetype.isBlank()) {
            return "</gray>";
        }
        return switch (archetype.toLowerCase(Locale.ROOT)) {
            case "warrior" -> "</red>";
            case "merchant" -> "</gold>";
            case "builder" -> "</green>";
            default -> "</light_purple>";
        };
    }

    public static TextColor textColor(String archetype) {
        if (archetype == null || archetype.isBlank()) {
            return NamedTextColor.GRAY;
        }
        return switch (archetype.toLowerCase(Locale.ROOT)) {
            case "warrior" -> NamedTextColor.RED;
            case "merchant" -> NamedTextColor.GOLD;
            case "builder" -> NamedTextColor.GREEN;
            default -> NamedTextColor.LIGHT_PURPLE;
        };
    }

    public static String displayName(String archetype) {
        if (archetype == null || archetype.isBlank()) {
            return "Nenhum";
        }
        return switch (archetype.toLowerCase(Locale.ROOT)) {
            case "warrior" -> "Guerreiro";
            case "builder" -> "Construtor";
            case "merchant" -> "Mercador";
            default -> archetype;
        };
    }

    public static String coloredDisplayName(String archetype) {
        return miniColorTag(archetype) + displayName(archetype) + miniColorCloseTag(archetype);
    }

    public static net.kyori.adventure.bossbar.BossBar.Color bossBarColor(String archetype) {
        if (archetype == null || archetype.isBlank()) {
            return net.kyori.adventure.bossbar.BossBar.Color.BLUE;
        }
        return switch (archetype.toLowerCase(Locale.ROOT)) {
            case "warrior" -> net.kyori.adventure.bossbar.BossBar.Color.RED;
            case "merchant" -> net.kyori.adventure.bossbar.BossBar.Color.YELLOW;
            case "builder" -> net.kyori.adventure.bossbar.BossBar.Color.GREEN;
            default -> net.kyori.adventure.bossbar.BossBar.Color.PURPLE;
        };
    }
}
