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
            enabled = false;
            return;
        }
        if (Bukkit.getPluginManager().getPlugin("Essentials") == null) {
            plugin.getLogger().info("Essentials ausente — recompensas kit/warp e balance_min via Vault apenas.");
            enabled = false;
            return;
        }
        enabled = true;
        plugin.getLogger().info("Essentials hook ativo.");
    }

    public void refresh() {
        enabled = false;
        enable();
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
        dispatchConsoleCommand("kit " + kitName + " " + player.getName());
        return true;
    }

    public boolean warpPlayer(Player player, String warpName) {
        if (!enabled || player == null || warpName == null || warpName.isBlank()) {
            return false;
        }
        if (!plugin.getPluginConfig().isEssentialsWarpRewardsEnabled()) {
            return false;
        }
        dispatchConsoleCommand("warp " + warpName + " " + player.getName());
        return true;
    }

    private void dispatchConsoleCommand(String command) {
        if (Bukkit.isPrimaryThread()) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        } else {
            Bukkit.getScheduler().runTask(plugin, () ->
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command));
        }
    }
}
