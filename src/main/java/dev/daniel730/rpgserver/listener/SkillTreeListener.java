package dev.daniel730.rpgserver.listener;

import dev.daniel730.rpgserver.RpgServerPlugin;
import dev.daniel730.rpgserver.gui.SkillTreeGui;
import dev.daniel730.rpgserver.gui.SkillTreeHolder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;

public final class SkillTreeListener implements Listener {

    private final RpgServerPlugin plugin;

    public SkillTreeListener(RpgServerPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        Inventory top = event.getView().getTopInventory();
        if (!(top.getHolder() instanceof SkillTreeHolder holder)) {
            return;
        }
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        if (event.getClickedInventory() == null || !event.getClickedInventory().equals(top)) {
            return;
        }
        String perkId = holder.getPerkId(event.getRawSlot());
        if (perkId == null) {
            return;
        }
        plugin.getQuestFeedbackService().playJournalClick(player);
        if (plugin.getSkillTreeManager().tryUnlock(player, perkId)) {
            SkillTreeGui.render(plugin, player, holder);
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof SkillTreeHolder) {
            event.setCancelled(true);
        }
    }
}
