package dev.daniel730.rpgserver.hook;

import dev.daniel730.rpgserver.RpgServerPlugin;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public final class VaultHook {

    private final RpgServerPlugin plugin;
    private Economy economy;
    private boolean enabled;

    public VaultHook(RpgServerPlugin plugin) {
        this.plugin = plugin;
    }

    public void enable() {
        if (!plugin.getPluginConfig().isVaultEnabled()) {
            return;
        }
        RegisteredServiceProvider<Economy> registration =
                plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (registration == null) {
            if (plugin.getPluginConfig().isRequireEconomy()) {
                plugin.getLogger().warning("Vault Economy não encontrada.");
            }
            return;
        }
        economy = registration.getProvider();
        enabled = economy != null;
        if (enabled) {
            plugin.getLogger().info("Vault Economy conectada.");
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Economy getEconomy() {
        return economy;
    }

    public double getBalance(Player player) {
        if (!enabled || player == null) {
            return 0;
        }
        return economy.getBalance(player);
    }

    public boolean deposit(Player player, double amount) {
        if (!enabled || player == null || amount <= 0) {
            return false;
        }
        return economy.depositPlayer(player, amount).transactionSuccess();
    }
}
