package dev.daniel730.rpgserver.listener;

import dev.daniel730.rpgserver.RpgServerPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.redcastlemedia.multitallented.civs.events.SpellPreCastEvent;

public final class CivsSpellQuestListener implements Listener {

    private final RpgServerPlugin plugin;

    public CivsSpellQuestListener(RpgServerPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSpellPreCast(SpellPreCastEvent event) {
        Player player = Bukkit.getPlayer(event.getUuid());
        if (player == null) {
            return;
        }
        plugin.getQuestManager().handleSpellCast(player, event.getSpellId());
    }
}
