package dev.daniel730.rpgserver.listener;

import dev.daniel730.rpgserver.RpgServerPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.redcastlemedia.multitallented.civs.events.GainExpEvent;

/**
 * Placeholder for future Civs-internal skill objectives (building/territorial XP).
 * GainExpEvent fires from Civs {@code Civilian.awardSkill()} / {@code addSkillXp()}.
 */
public final class CivsInternalSkillListener implements Listener {

    public CivsInternalSkillListener(RpgServerPlugin plugin) {
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onGainExp(GainExpEvent event) {
        Player player = Bukkit.getPlayer(event.getUuid());
        if (player == null) {
            return;
        }
        // Future: civs_skill_xp objectives keyed by event.getType()
    }
}
