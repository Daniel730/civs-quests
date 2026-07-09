package dev.daniel730.rpgserver.listener;

import dev.daniel730.rpgserver.RpgServerPlugin;
import dev.daniel730.rpgserver.gui.RebirthGui;
import dev.daniel730.rpgserver.gui.RebirthHolder;
import dev.daniel730.rpgserver.progression.RebirthService;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

public final class RebirthListener implements Listener {

    private final RpgServerPlugin plugin;

    public RebirthListener(RpgServerPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getView().getTopInventory().getHolder() instanceof RebirthHolder)) {
            return;
        }
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        if (event.getClickedInventory() == null
                || !event.getClickedInventory().equals(event.getView().getTopInventory())) {
            return;
        }
        int slot = event.getRawSlot();
        if (slot == RebirthGui.CONFIRM_SLOT) {
            player.closeInventory();
            RebirthService.RebirthResult result = plugin.getRebirthService().tryRebirth(player);
            if (result == RebirthService.RebirthResult.NOT_ELIGIBLE) {
                plugin.getMessageUtil().send(player,
                        "<red>Conclua um capstone (Guerreiro, Mercador ou Construtor) para renascer.</red>");
            }
            return;
        }
        if (slot == RebirthGui.CANCEL_SLOT) {
            player.closeInventory();
            plugin.getMessageUtil().send(player, "<gray>Renascimento cancelado.</gray>");
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof RebirthHolder) {
            event.setCancelled(true);
        }
    }
}
