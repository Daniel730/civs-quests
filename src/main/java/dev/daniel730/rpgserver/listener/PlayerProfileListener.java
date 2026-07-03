package dev.daniel730.rpgserver.listener;

import dev.daniel730.rpgserver.RpgServerPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class PlayerProfileListener implements Listener {

    private final RpgServerPlugin plugin;

    public PlayerProfileListener(RpgServerPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        plugin.getProfileManager().loadProfile(event.getPlayer().getUniqueId());
        plugin.getQuestManager().resetExpiredScheduledQuests(event.getPlayer());
        if (plugin.getCivsHook().isEnabled()) {
            plugin.getQuestManager().checkCivsSkillLevels(event.getPlayer());
        }
        if (plugin.getPluginConfig().isSyncOnJoinFromCivs()) {
            plugin.getQuestManager().getProgressSync()
                    .sync(event.getPlayer(), false, false);
        }
        plugin.getSkillTreeManager().applyUnlockedPerks(event.getPlayer());
        plugin.getSkillTreeManager().checkAutoUnlocks(event.getPlayer());
        plugin.getQuestFeedbackService().refreshBossBar(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        plugin.getQuestFeedbackService().hideBossBar(event.getPlayer());
        plugin.getProfileManager().unloadProfile(event.getPlayer().getUniqueId());
    }
}
