package dev.daniel730.rpgserver.hud;

import dev.daniel730.rpgserver.RpgServerPlugin;
import dev.daniel730.rpgserver.config.PluginConfig;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Owns the ActionBar when composed HUD is enabled.
 * Default layout {@code hearts-slot}: bitmap HP/mana bars (resource pack font
 * {@code rpg:hud}) shifted onto the vacated hearts row. Hunger stays vanilla;
 * quest tracking stays on BossBar/scoreboard — not in this ActionBar line.
 */
public final class ComposedHudService {

    private static final Pattern INT = Pattern.compile("(-?\\d+)");

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
        String layout = config.isHeartsSlotHudLayout() ? "hearts-slot" : "legacy";
        plugin.getLogger().info("HUD composto ativo (layout=" + layout + ", intervalo=" + interval + " ticks).");
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
        if (config.isHeartsSlotHudLayout()) {
            return buildHeartsSlotLine(player, config);
        }
        return buildLegacyLine(player, config);
    }

    private String buildHeartsSlotLine(Player player, PluginConfig config) {
        int hp = (int) Math.ceil(player.getHealth());
        int hpMax = 20;
        if (player.getAttribute(Attribute.MAX_HEALTH) != null) {
            hpMax = (int) Math.ceil(player.getAttribute(Attribute.MAX_HEALTH).getValue());
        }
        // Prefer AuraSkills HP when PAPI is available (matches displayed max HP).
        String asHp = papi(player, "%auraskills_hp%");
        String asMax = papi(player, "%auraskills_hp_max%");
        Integer parsedHp = parseInt(asHp);
        Integer parsedMax = parseInt(asMax);
        if (parsedHp != null) {
            hp = parsedHp;
        }
        if (parsedMax != null && parsedMax > 0) {
            hpMax = parsedMax;
        }

        int mana = 0;
        int manaMax = 0;
        Integer m = parseInt(papi(player, "%civs_mana%"));
        Integer mm = parseInt(papi(player, "%civs_max_mana%"));
        if (m != null) {
            mana = m;
        }
        if (mm != null) {
            manaMax = mm;
        }
        if (manaMax <= 0) {
            String pair = papi(player, "%civs_mana_pair%");
            int[] pairVals = parsePair(pair);
            if (pairVals != null) {
                mana = pairVals[0];
                manaMax = pairVals[1];
            }
        }

        String glyphs = HeartsSlotHudComposer.build(
                hp, hpMax, mana, Math.max(1, manaMax),
                config.getHeartsSlotSegments(),
                config.getHeartsSlotShiftLeft(),
                config.getHeartsSlotGap());
        return HeartsSlotHudComposer.wrapMiniMessage(glyphs);
    }

    private String buildLegacyLine(Player player, PluginConfig config) {
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
        values.put("quest", "");
        values.put("quest_name", "");
        values.put("archetype", "");
        values.put("mana", "");
        values.put("mana_max", "");
        values.put("mana_pair", "");
        return values;
    }

    private String papi(Player player, String placeholder) {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null) {
            return "";
        }
        try {
            String out = PlaceholderAPI.setPlaceholders(player, placeholder);
            if (out == null || out.equals(placeholder)) {
                return "";
            }
            return out.trim();
        } catch (Exception ex) {
            return "";
        }
    }

    private static Integer parseInt(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        Matcher m = INT.matcher(raw.replace(",", "").replace(" ", ""));
        if (!m.find()) {
            return null;
        }
        try {
            return Integer.parseInt(m.group(1));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static int[] parsePair(String raw) {
        if (raw == null || raw.isBlank() || !raw.contains("/")) {
            return null;
        }
        String[] parts = raw.split("/", 2);
        Integer a = parseInt(parts[0]);
        Integer b = parseInt(parts[1]);
        if (a == null || b == null) {
            return null;
        }
        return new int[]{a, b};
    }
}
