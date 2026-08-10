package dev.daniel730.rpgserver.listener;

import dev.daniel730.rpgserver.gui.CodexHolder;
import dev.daniel730.rpgserver.gui.GuiMotion;
import dev.daniel730.rpgserver.gui.PlayerHubHolder;
import dev.daniel730.rpgserver.gui.QuestJournalHolder;
import dev.daniel730.rpgserver.gui.RebirthHolder;
import dev.daniel730.rpgserver.gui.SkillTreeHolder;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.InventoryHolder;

/**
 * Cancels in-flight {@link GuiMotion} tasks when a player closes an RPG inventory GUI.
 */
public final class GuiMotionListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryClose(InventoryCloseEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof PlayerHubHolder
                || holder instanceof QuestJournalHolder
                || holder instanceof SkillTreeHolder
                || holder instanceof CodexHolder
                || holder instanceof RebirthHolder) {
            GuiMotion.cancel(holder);
        }
    }
}
