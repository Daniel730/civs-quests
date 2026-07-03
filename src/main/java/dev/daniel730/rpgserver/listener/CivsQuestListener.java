package dev.daniel730.rpgserver.listener;

import dev.daniel730.rpgserver.RpgServerPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.redcastlemedia.multitallented.civs.events.PlayerAcceptsTownInviteEvent;
import org.redcastlemedia.multitallented.civs.events.RegionCreatedEvent;
import org.redcastlemedia.multitallented.civs.regions.RegionType;
import org.redcastlemedia.multitallented.civs.towns.Town;

public final class CivsQuestListener implements Listener {

    private final RpgServerPlugin plugin;

    public CivsQuestListener(RpgServerPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTownInviteAccepted(PlayerAcceptsTownInviteEvent event) {
        if (event.getUuid() == null) {
            return;
        }
        Player player = plugin.getServer().getPlayer(event.getUuid());
        if (player == null) {
            return;
        }
        Town town = event.getTown();
        String townKey = town == null ? null : town.getName();
        plugin.getQuestManager().handleJoinTown(player, townKey);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onRegionCreated(RegionCreatedEvent event) {
        Player player = event.getPlayer();
        if (player == null) {
            return;
        }
        RegionType regionType = event.getRegionType();
        if (regionType == null) {
            return;
        }
        plugin.getQuestManager().handleRegionBuilt(player, regionType.getKey());
    }
}
