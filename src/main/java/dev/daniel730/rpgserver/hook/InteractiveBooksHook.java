package dev.daniel730.rpgserver.hook;

import dev.daniel730.rpgserver.RpgServerPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class InteractiveBooksHook {

    private final RpgServerPlugin plugin;
    private boolean enabled;

    public InteractiveBooksHook(RpgServerPlugin plugin) {
        this.plugin = plugin;
    }

    public void enable() {
        if (!plugin.getPluginConfig().isInteractiveBooksEnabled()) {
            return;
        }
        if (Bukkit.getPluginManager().getPlugin("InteractiveBooks") == null) {
            plugin.getLogger().info("InteractiveBooks ausente — lore-book ignorado.");
            return;
        }
        enabled = true;
        plugin.getLogger().info("InteractiveBooks hook ativo.");
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void grantLoreBook(Player player, String bookId) {
        if (!enabled || player == null || bookId == null || bookId.isBlank()) {
            return;
        }
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                "interactivebooks give " + player.getName() + " " + bookId);
        plugin.getMessageUtil().send(player, "<gray>Livro de lore recebido:</gray> " + bookId);
    }
}
