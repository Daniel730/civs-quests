package dev.daniel730.rpgserver.listener;

import dev.daniel730.rpgserver.RpgServerPlugin;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.Locale;

public final class BukkitQuestListener implements Listener {

    private final RpgServerPlugin plugin;

    public BukkitQuestListener(RpgServerPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null) {
            return;
        }
        EntityType type = event.getEntityType();
        plugin.getQuestManager().handleKillMob(killer, type.name().toLowerCase(Locale.ROOT));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Material material = event.getBlock().getType();
        plugin.getQuestManager().handleMineBlock(player, material.name().toLowerCase(Locale.ROOT));
    }
}
