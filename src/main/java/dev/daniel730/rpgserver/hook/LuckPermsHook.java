package dev.daniel730.rpgserver.hook;

import dev.daniel730.rpgserver.RpgServerPlugin;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import org.bukkit.Bukkit;

public final class LuckPermsHook {

    private final RpgServerPlugin plugin;
    private LuckPerms luckPerms;
    private boolean enabled;

    public LuckPermsHook(RpgServerPlugin plugin) {
        this.plugin = plugin;
    }

    public void enable() {
        if (!plugin.getPluginConfig().isLuckPermsEnabled()) {
            return;
        }
        if (Bukkit.getPluginManager().getPlugin("LuckPerms") == null) {
            plugin.getLogger().warning("LuckPerms não encontrado.");
            return;
        }
        try {
            luckPerms = LuckPermsProvider.get();
            enabled = luckPerms != null;
            if (enabled) {
                plugin.getLogger().info("LuckPerms API conectada.");
            }
        } catch (IllegalStateException ex) {
            plugin.getLogger().warning("LuckPerms API indisponível: " + ex.getMessage());
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public LuckPerms getLuckPerms() {
        return luckPerms;
    }

    public String questPermission(String questId) {
        return plugin.getPluginConfig().getQuestPermissionPrefix() + questId;
    }

    public boolean hasQuestPermission(org.bukkit.entity.Player player, String questId) {
        if (!enabled) {
            return true;
        }
        return player.hasPermission(questPermission(questId));
    }
}
