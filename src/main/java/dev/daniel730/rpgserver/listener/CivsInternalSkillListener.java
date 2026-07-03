package dev.daniel730.rpgserver.listener;

import dev.daniel730.rpgserver.RpgServerPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.redcastlemedia.multitallented.civs.events.GainExpEvent;

/**
 * Civs-internal skill objectives via {@link GainExpEvent} (territorial/building XP ledger).
 */
public final class CivsInternalSkillListener implements Listener {

    private final RpgServerPlugin plugin;

    public CivsInternalSkillListener(RpgServerPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onGainExp(GainExpEvent event) {
        Player player = Bukkit.getPlayer(event.getUuid());
        if (player == null) {
            return;
        }
        plugin.getQuestManager().handleCivsSkillXp(player, event.getType(), event.getExp());
        plugin.getQuestManager().checkCivsSkillLevels(player);
    }
}
