package dev.daniel730.rpgserver.listener;

import dev.daniel730.rpgserver.RpgServerPlugin;
import dev.daniel730.rpgserver.profile.PlayerProfile;
import dev.daniel730.rpgserver.quest.QuestManager;
import dev.daniel730.rpgserver.util.AgentDebugLog;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.File;

public final class PlayerProfileListener implements Listener {

    private final RpgServerPlugin plugin;

    public PlayerProfileListener(RpgServerPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        boolean newProfile = !profileFile(player).exists();
        plugin.getProfileManager().loadProfile(player.getUniqueId());
        PlayerProfile profile = plugin.getProfileManager().getOrCreate(player);
        int activeBefore = profile.getActiveQuestIds().size();
        QuestManager.SanitizeResult sanitized = plugin.getQuestManager().sanitizeProfile(player, profile);
        // #region agent log
        AgentDebugLog.log(plugin, "H5", "PlayerProfileListener.onJoin",
                "player joined",
                java.util.Map.of("activeBefore", activeBefore,
                        "activeAfter", profile.getActiveQuestIds().size(),
                        "strippedInvalid", sanitized.strippedInvalid(),
                        "strippedCompletedActive", sanitized.strippedCompletedActive(),
                        "demotedExcess", sanitized.demotedExcess()));
        // #endregion
        plugin.getQuestManager().resetExpiredScheduledQuests(player);
        if (plugin.getCivsHook().isEnabled()) {
            plugin.getQuestManager().backfillCivsState(player);
        }
        if (plugin.getPluginConfig().isSyncOnJoinFromCivs()) {
            plugin.getQuestManager().getProgressSync()
                    .sync(player, false, false);
        }
        plugin.getSkillTreeManager().applyUnlockedPerks(player);
        plugin.getSkillTreeManager().checkAutoUnlocks(player);
        plugin.getPathTraitService().reapplyPathTraits(player);
        plugin.getQuestFeedbackService().notifyDailyCtaIfNeeded(player);
        maybeShowWelcome(player, newProfile);
        maybeGiveHubItem(player);
        plugin.getQuestFeedbackService().refreshTrackedHud(player);
    }

    private void maybeGiveHubItem(Player player) {
        if (!plugin.getPluginConfig().isGuideBookOnJoin()) {
            return;
        }
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) {
                return;
            }
            if (!plugin.getPlayerHubService().hasHubItemInInventory(player)) {
                plugin.getPlayerHubService().giveHubItem(player);
            }
        }, 60L);
    }

    private void maybeShowWelcome(Player player, boolean newProfile) {
        PlayerProfile profile = plugin.getProfileManager().getOrCreate(player);
        boolean noArchetype = profile.getArchetype() == null || profile.getArchetype().isBlank();
        if (!noArchetype || profile.isWelcomeShown()) {
            return;
        }
        if (!newProfile && !plugin.getPluginConfig().isWelcomeEnabled()) {
            return;
        }
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) {
                return;
            }
            PlayerProfile current = plugin.getProfileManager().getOrCreate(player);
            if (current.isWelcomeShown()) {
                return;
            }
            if (current.getArchetype() != null && !current.getArchetype().isBlank()) {
                return;
            }
            plugin.getQuestFeedbackService().showWelcome(player);
            current.setWelcomeShown(true);
            plugin.getProfileManager().markDirty(player.getUniqueId());
        }, 40L);
    }

    private File profileFile(Player player) {
        return new File(plugin.getDataFolder(), "players/" + player.getUniqueId() + ".yml");
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        plugin.getQuestFeedbackService().hideBossBar(event.getPlayer());
        plugin.getQuestFeedbackService().hideScoreboard(event.getPlayer());
        plugin.getProfileManager().unloadProfile(event.getPlayer().getUniqueId());
    }
}
