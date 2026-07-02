package dev.daniel730.rpgserver.hook;

import dev.daniel730.rpgserver.RpgServerPlugin;
import org.bukkit.Bukkit;

public final class CivsHook {

    private final RpgServerPlugin plugin;
    private boolean enabled;

    public CivsHook(RpgServerPlugin plugin) {
        this.plugin = plugin;
    }

    public void enable() {
        if (!plugin.getPluginConfig().isCivsEnabled()) {
            return;
        }
        enabled = Bukkit.getPluginManager().getPlugin("Civs") != null;
        if (enabled) {
            plugin.getLogger().info("Civs detectado — eventos de região habilitados.");
        } else {
            plugin.getLogger().warning("Civs não encontrado — objetivos build_region ficarão inativos.");
        }
    }

    public boolean isEnabled() {
        return enabled;
    }
}
