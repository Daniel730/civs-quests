package dev.daniel730.rpgserver.placeholder;

import dev.daniel730.rpgserver.RpgServerPlugin;
import dev.daniel730.rpgserver.profile.PlayerProfile;
import dev.daniel730.rpgserver.quest.Quest;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public final class RpgPlaceholderExpansion extends PlaceholderExpansion {

    private final RpgServerPlugin plugin;

    public RpgPlaceholderExpansion(RpgServerPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "rpg";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Daniel730";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null) {
            return "";
        }
        PlayerProfile profile = plugin.getProfileManager().loadProfile(player.getUniqueId());
        String normalized = params.toLowerCase(Locale.ROOT);
        if (normalized.equals("quest_progress")) {
            return plugin.getQuestManager().formatPrimaryQuestProgress(profile);
        }
        if (normalized.equals("tracked_quest")) {
            return plugin.getQuestManager().formatTrackedQuestName(profile);
        }
        if (normalized.equals("tracked_progress")) {
            return plugin.getQuestManager().formatTrackedQuestProgress(profile);
        }
        if (normalized.startsWith("quest_progress_")) {
            String questId = params.substring("quest_progress_".length());
            Quest quest = plugin.getQuestManager().getQuest(questId);
            if (quest == null) {
                return "";
            }
            return plugin.getQuestManager().formatQuestProgress(profile, quest);
        }
        return switch (normalized) {
            case "archetype" -> formatArchetype(profile.getArchetype());
            case "archetype_colored" -> formatArchetypeColored(profile.getArchetype());
            case "active_quest" -> formatActiveQuest(profile);
            case "perks_unlocked" -> String.valueOf(profile.getUnlockedPerkIds().size());
            case "active_perk_count" -> String.valueOf(profile.getUnlockedPerkIds().size());
            default -> null;
        };
    }

    private String formatArchetype(String archetype) {
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

    private String formatArchetypeColored(String archetype) {
        if (archetype == null || archetype.isBlank()) {
            return "§7Nenhum";
        }
        return switch (archetype.toLowerCase(Locale.ROOT)) {
            case "warrior" -> "§cGuerreiro";
            case "builder" -> "§aConstrutor";
            case "merchant" -> "§6Mercador";
            default -> "§d" + archetype;
        };
    }

    private String formatActiveQuest(PlayerProfile profile) {
        Quest quest = plugin.getQuestManager().findPrimaryActiveQuest(profile);
        if (quest == null) {
            return "Nenhuma";
        }
        return quest.getName();
    }
}
