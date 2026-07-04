package dev.daniel730.rpgserver.listener;

import dev.daniel730.rpgserver.RpgServerPlugin;
import dev.daniel730.rpgserver.gui.CodexHolder;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

public final class CodexListener implements Listener {

    public CodexListener(RpgServerPlugin plugin) {
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof CodexHolder) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof CodexHolder) {
            event.setCancelled(true);
        }
    }
}
