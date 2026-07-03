package dev.daniel730.rpgserver.hook;

import dev.daniel730.rpgserver.RpgServerPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class EssentialsHook {

    private final RpgServerPlugin plugin;
    private boolean enabled;

    public EssentialsHook(RpgServerPlugin plugin) {
        this.plugin = plugin;
    }

    public void enable() {
        if (!plugin.getPluginConfig().isEssentialsEnabled()) {
            return;
        }
        if (Bukkit.getPluginManager().getPlugin("Essentials") == null) {
            plugin.getLogger().info("Essentials ausente — recompensas kit/warp e balance_min via Vault apenas.");
            return;
        }
        enabled = true;
        plugin.getLogger().info("Essentials hook ativo.");
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean giveKit(Player player, String kitName) {
        if (!enabled || player == null || kitName == null || kitName.isBlank()) {
            return false;
        }
        if (!plugin.getPluginConfig().isEssentialsKitRewardsEnabled()) {
            return false;
        }
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "kit " + kitName + " " + player.getName());
        return true;
    }

    public boolean warpPlayer(Player player, String warpName) {
        if (!enabled || player == null || warpName == null || warpName.isBlank()) {
            return false;
        }
        if (!plugin.getPluginConfig().isEssentialsWarpRewardsEnabled()) {
            return false;
        }
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "warp " + warpName + " " + player.getName());
        return true;
    }
}
