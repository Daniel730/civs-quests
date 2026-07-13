package dev.daniel730.rpgserver.listener;

import dev.daniel730.rpgserver.RpgServerPlugin;
import dev.daniel730.rpgserver.profile.PlayerProfile;
import dev.daniel730.rpgserver.quest.Quest;
import dev.daniel730.rpgserver.quest.QuestManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.redcastlemedia.multitallented.civs.events.TutorialChooseCompleteEvent;

public final class RpgTutorialBridgeListener implements Listener {

    private final RpgServerPlugin plugin;

    public RpgTutorialBridgeListener(RpgServerPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTutorialChooseComplete(TutorialChooseCompleteEvent event) {
        Player player = event.getPlayer();
        if (player == null || !player.isOnline()) {
            return;
        }
        plugin.getServer().getScheduler().runTask(plugin, () -> offerWelcomeAndHub(player));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        if (!plugin.getCivsHook().isEnabled()) {
            return;
        }
        Player player = event.getPlayer();
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) {
                return;
            }
            PlayerProfile profile = plugin.getProfileManager().getOrCreate(player);
            if (profile.getArchetype() != null && !profile.getArchetype().isBlank()) {
                return;
            }
            String starterId = plugin.getPluginConfig().getStarterQuestId();
            if (profile.isQuestComplete(starterId) || profile.getStartedQuestIds().contains(starterId)) {
                return;
            }
            offerWelcomeAndHub(player);
        }, 80L);
    }

    private void offerWelcomeAndHub(Player player) {
        PlayerProfile profile = plugin.getProfileManager().getOrCreate(player);
        if (profile.getArchetype() != null && !profile.getArchetype().isBlank()) {
            return;
        }
        QuestManager questManager = plugin.getQuestManager();
        String starterId = plugin.getPluginConfig().getStarterQuestId();
        if (!profile.getStartedQuestIds().contains(starterId) && !profile.isQuestComplete(starterId)) {
            Quest starter = questManager.getQuest(starterId);
            if (starter != null) {
                QuestManager.StartResult result = questManager.startQuest(player, profile, starter);
                if (result == QuestManager.StartResult.STARTED) {
                    plugin.getMessageUtil().send(player,
                            "<gold>★</gold> <white>Missão de boas-vindas disponível:</white> <yellow>O Chamado do Reino</yellow>");
                }
            }
        }
        if (!profile.isHubOpened()) {
            profile.setHubOpened(true);
            plugin.getProfileManager().markDirty(player.getUniqueId());
            plugin.getPlayerHubService().openHub(player);
        }
    }
}
