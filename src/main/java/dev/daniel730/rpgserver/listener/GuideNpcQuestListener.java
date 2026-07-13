package dev.daniel730.rpgserver.listener;

import dev.daniel730.rpgserver.RpgServerPlugin;
import dev.daniel730.rpgserver.gui.QuestJournalGui;
import dev.daniel730.rpgserver.profile.PlayerProfile;
import dev.daniel730.rpgserver.quest.Quest;
import dev.daniel730.rpgserver.quest.QuestManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.redcastlemedia.multitallented.civs.events.GuideNpcInteractEvent;

import java.util.List;
import java.util.Locale;

public final class GuideNpcQuestListener implements Listener {

    private final RpgServerPlugin plugin;

    public GuideNpcQuestListener(RpgServerPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onGuideNpcInteract(GuideNpcInteractEvent event) {
        Player player = event.getPlayer();
        if (player == null || !player.isOnline()) {
            return;
        }
        String archetype = event.getArchetype();
        if (archetype == null || archetype.isBlank() || "neutral".equalsIgnoreCase(archetype)) {
            offerStarterQuests(player);
            return;
        }
        offerArchetypeQuest(player, archetype.toLowerCase(Locale.ROOT));
    }

    private void offerStarterQuests(Player player) {
        PlayerProfile profile = plugin.getProfileManager().getOrCreate(player);
        QuestManager questManager = plugin.getQuestManager();
        String starterId = plugin.getPluginConfig().getStarterQuestId();
        if (!profile.getStartedQuestIds().contains(starterId) && !profile.isQuestComplete(starterId)) {
            Quest welcome = questManager.getQuest(starterId);
            if (welcome != null) {
                questManager.startQuest(player, profile, welcome);
            }
        }
        QuestJournalGui.open(plugin, player);
    }

    private void offerArchetypeQuest(Player player, String archetype) {
        PlayerProfile profile = plugin.getProfileManager().getOrCreate(player);
        QuestManager questManager = plugin.getQuestManager();
        if (profile.getArchetype() == null || profile.getArchetype().isBlank()) {
            for (Quest path : questManager.getPathQuests(player, profile)) {
                if (archetype.equalsIgnoreCase(path.getArchetype())) {
                    QuestManager.StartResult result = questManager.startQuest(player, profile, path);
                    if (result == QuestManager.StartResult.STARTED) {
                        plugin.getMessageUtil().send(player,
                                "<gold>★</gold> <white>Missão oferecida:</white> <yellow>" + path.getName() + "</yellow>");
                        questManager.setTrackedQuest(player, profile, path);
                    }
                    return;
                }
            }
        }
        List<Quest> chain = questManager.getArchetypeStoryQuests(player, profile, archetype);
        for (Quest quest : chain) {
            QuestManager.QuestStatus status = questManager.getQuestStatus(player, profile, quest);
            if (status == QuestManager.QuestStatus.NOT_STARTED
                    || status == QuestManager.QuestStatus.IN_PROGRESS) {
                if (status == QuestManager.QuestStatus.NOT_STARTED) {
                    questManager.startQuest(player, profile, quest);
                }
                questManager.setTrackedQuest(player, profile, quest);
                plugin.getMessageUtil().send(player,
                        "<gold>★</gold> <white>Fale com o guia — missão:</white> <yellow>" + quest.getName() + "</yellow>");
                QuestJournalGui.open(plugin, player);
                return;
            }
        }
        QuestJournalGui.open(plugin, player);
    }
}
