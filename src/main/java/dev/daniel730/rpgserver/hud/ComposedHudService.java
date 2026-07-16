package dev.daniel730.rpgserver.hud;

import dev.daniel730.rpgserver.RpgServerPlugin;
import dev.daniel730.rpgserver.config.PluginConfig;
import dev.daniel730.rpgserver.profile.PlayerProfile;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Owns the ActionBar when composed HUD is enabled: merges HP, Civs mana, AuraSkills,
 * and tracked quest into one line via PlaceholderAPI + local tokens.
 */
public final class ComposedHudService {

    private final RpgServerPlugin plugin;
    private final Map<UUID, Long> suppressUntilMs = new ConcurrentHashMap<>();
    private BukkitTask task;

    public ComposedHudService(RpgServerPlugin plugin) {
        this.plugin = plugin;
    }

    public void start() {
        stop();
        PluginConfig config = plugin.getPluginConfig();
        if (!config.isComposedHudEnabled()) {
            plugin.getLogger().info("HUD composto desligado (hud.composed.enabled=false).");
            return;
        }
        long interval = Math.max(5L, config.getComposedHudIntervalTicks());
        task = Bukkit.getScheduler().runTaskTimer(plugin, this::tickOnlinePlayers, 40L, interval);
        plugin.getLogger().info("HUD composto ativo (ActionBar unificada, intervalo=" + interval + " ticks).");
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        suppressUntilMs.clear();
    }

    public boolean isEnabled() {
        return plugin.getPluginConfig().isComposedHudEnabled();
    }

    /** Pause composed redraw briefly after a quest ActionBar pulse. */
    public void suppress(Player player, long ticks) {
        if (player == null) {
            return;
        }
        long until = System.currentTimeMillis() + ticks * 50L;
        suppressUntilMs.put(player.getUniqueId(), until);
    }

    public void refresh(Player player) {
        if (!isEnabled() || player == null || !player.isOnline()) {
            return;
        }
        Long until = suppressUntilMs.get(player.getUniqueId());
        if (until != null && System.currentTimeMillis() < until) {
            return;
        }
        String line = buildLine(player);
        if (!line.isBlank()) {
            plugin.getMessageUtil().sendActionBar(player, line);
        }
    }

    private void tickOnlinePlayers() {
        if (!isEnabled()) {
            return;
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            refresh(player);
        }
    }

    String buildLine(Player player) {
        PluginConfig config = plugin.getPluginConfig();
        String template = config.getComposedHudFormat();
        Map<String, String> values = buildLocalTokens(player);
        String composed = ComposedHudComposer.compose(template, values);
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            try {
                composed = PlaceholderAPI.setPlaceholders(player, composed);
            } catch (Exception ex) {
                plugin.getLogger().fine("PAPI HUD: " + ex.getMessage());
            }
        }
        return composed;
    }

    private Map<String, String> buildLocalTokens(Player player) {
        Map<String, String> values = new HashMap<>();
        double health = player.getHealth();
        double maxHealth = 20.0;
        if (player.getAttribute(Attribute.MAX_HEALTH) != null) {
            maxHealth = player.getAttribute(Attribute.MAX_HEALTH).getValue();
        }
        int hp = (int) Math.ceil(health);
        int hpMax = (int) Math.ceil(maxHealth);
        values.put("hp", String.valueOf(hp));
        values.put("hp_max", String.valueOf(hpMax));
        values.put("hp_bar", ComposedHudComposer.compactBar(hp, hpMax, 8));
        values.put("food", String.valueOf(player.getFoodLevel()));

        PlayerProfile profile = plugin.getProfileManager().getOrCreate(player);
        String tracked = plugin.getQuestManager().formatTrackedQuestProgress(profile);
        if (tracked == null || tracked.isBlank() || "—".equals(tracked) || "-".equals(tracked)) {
            tracked = "";
        }
        values.put("quest", tracked);
        values.put("quest_name", nullToEmpty(plugin.getQuestManager().formatTrackedQuestName(profile)));
        values.put("archetype", formatArchetypeShort(profile.getArchetype()));

        // Fallbacks if PAPI/Civs expansions missing — filled by %civs_*% when present.
        values.put("mana", "");
        values.put("mana_max", "");
        values.put("mana_pair", "");
        return values;
    }

    private static String nullToEmpty(String value) {
        return value == null || "—".equals(value) || "-".equals(value) ? "" : value;
    }

    private static String formatArchetypeShort(String archetype) {
        if (archetype == null || archetype.isBlank()) {
            return "";
        }
        return switch (archetype.toLowerCase(Locale.ROOT)) {
            case "warrior" -> "⚔";
            case "builder" -> "⛏";
            case "merchant" -> "⚜";
            default -> "";
        };
    }
}
