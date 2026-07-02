package dev.daniel730.rpgserver.hook;

import dev.daniel730.rpgserver.RpgServerPlugin;
import dev.daniel730.rpgserver.placeholder.RpgPlaceholderExpansion;
import org.bukkit.Bukkit;

public final class PlaceholderHook {

    private final RpgServerPlugin plugin;
    private RpgPlaceholderExpansion expansion;
    private boolean enabled;

    public PlaceholderHook(RpgServerPlugin plugin) {
        this.plugin = plugin;
    }

    public void enable() {
        if (!plugin.getPluginConfig().isPlaceholderEnabled()) {
            return;
        }
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null) {
            plugin.getLogger().warning("PlaceholderAPI não encontrado.");
            return;
        }
        expansion = new RpgPlaceholderExpansion(plugin);
        if (expansion.register()) {
            enabled = true;
            plugin.getLogger().info("PlaceholderAPI expansion 'rpg' registrada.");
        }
    }

    public void disable() {
        if (expansion != null) {
            expansion.unregister();
        }
    }

    public boolean isEnabled() {
        return enabled;
    }
}
