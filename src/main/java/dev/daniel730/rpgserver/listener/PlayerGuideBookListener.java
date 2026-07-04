package dev.daniel730.rpgserver.listener;

import dev.daniel730.rpgserver.RpgServerPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public final class PlayerGuideBookListener implements Listener {

    private final RpgServerPlugin plugin;

    public PlayerGuideBookListener(RpgServerPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onGuideBookUse(PlayerInteractEvent event) {
        if (event.getItem() == null) {
            return;
        }
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        if (!plugin.getPlayerGuideBookService().isGuideBookItem(event.getItem())) {
            return;
        }
        event.setCancelled(true);
        Player player = event.getPlayer();
        plugin.getPlayerGuideBookService().openGuide(player);
    }
}
