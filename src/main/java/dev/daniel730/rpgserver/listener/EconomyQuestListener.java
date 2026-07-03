package dev.daniel730.rpgserver.listener;

import dev.daniel730.rpgserver.RpgServerPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public final class EconomyQuestListener implements Listener {

    private final RpgServerPlugin plugin;

    public EconomyQuestListener(RpgServerPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.getQuestManager().checkEconomyObjectives(player);
    }
}
