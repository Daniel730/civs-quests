package dev.daniel730.rpgserver.listener;

import dev.daniel730.rpgserver.RpgServerPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.redcastlemedia.multitallented.civs.events.EnterCombatEvent;

import java.util.UUID;

public final class CombatQuestListener implements Listener {

    private final RpgServerPlugin plugin;

    public CombatQuestListener(RpgServerPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEnterCombat(EnterCombatEvent event) {
        if (!plugin.getCivsHook().isEnabled()) {
            return;
        }
        UUID uuid = event.UUID;
        if (uuid == null) {
            return;
        }
        Player player = Bukkit.getPlayer(uuid);
        if (player == null || !player.isOnline()) {
            return;
        }
        plugin.getQuestManager().handleEnterCombat(player);
    }
}
