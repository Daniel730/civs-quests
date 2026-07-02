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
        return switch (params.toLowerCase(Locale.ROOT)) {
            case "archetype" -> formatArchetype(profile.getArchetype());
            case "active_quest" -> formatActiveQuest(profile);
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

    private String formatActiveQuest(PlayerProfile profile) {
        Quest quest = plugin.getQuestManager().findPrimaryActiveQuest(profile);
        if (quest == null) {
            return "Nenhuma";
        }
        return quest.getName();
    }
}
