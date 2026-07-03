package dev.daniel730.rpgserver.quest;

import dev.daniel730.rpgserver.RpgServerPlugin;
import dev.daniel730.rpgserver.config.PluginConfig;
import dev.daniel730.rpgserver.profile.PlayerProfile;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Action bar / title notifications and optional boss bar for tracked quest progress.
 */
public final class QuestFeedbackService {

    private final RpgServerPlugin plugin;
    private final Map<UUID, BossBar> bossBars = new ConcurrentHashMap<>();

    public QuestFeedbackService(RpgServerPlugin plugin) {
        this.plugin = plugin;
    }

    public void notifyObjectiveComplete(Player player, Quest quest, Quest.Objective objective) {
        PluginConfig config = plugin.getPluginConfig();
        if (!config.isQuestNotificationsEnabled()) {
            return;
        }
        String questName = quest.getName();
        String description = objective.getDescription();
        if (!config.getQuestObjectiveActionBar().isBlank()) {
            plugin.getMessageUtil().sendActionBar(player,
                    replace(config.getQuestObjectiveActionBar(), questName, description));
        }
        if (!config.getQuestObjectiveTitle().isBlank() || !config.getQuestObjectiveSubtitle().isBlank()) {
            plugin.getMessageUtil().sendTitle(player,
                    replace(config.getQuestObjectiveTitle(), questName, description),
                    replace(config.getQuestObjectiveSubtitle(), questName, description),
                    5, 40, 10);
        }
    }

    public void notifyQuestComplete(Player player, Quest quest) {
        PluginConfig config = plugin.getPluginConfig();
        if (!config.isQuestNotificationsEnabled()) {
            return;
        }
        String questName = quest.getName();
        if (!config.getQuestCompleteActionBar().isBlank()) {
            plugin.getMessageUtil().sendActionBar(player,
                    replace(config.getQuestCompleteActionBar(), questName, null));
        }
        if (!config.getQuestCompleteTitle().isBlank() || !config.getQuestCompleteSubtitle().isBlank()) {
            plugin.getMessageUtil().sendTitle(player,
                    replace(config.getQuestCompleteTitle(), questName, null),
                    replace(config.getQuestCompleteSubtitle(), questName, null),
                    10, 60, 20);
        }
    }

    public void refreshBossBar(Player player) {
        PluginConfig config = plugin.getPluginConfig();
        if (!config.isQuestBossBarEnabled()) {
            hideBossBar(player);
            return;
        }
        PlayerProfile profile = plugin.getProfileManager().getOrCreate(player);
        Quest quest = plugin.getQuestManager().findPrimaryActiveQuest(profile);
        if (quest == null) {
            hideBossBar(player);
            return;
        }
        QuestManager.QuestProgress progress = plugin.getQuestManager().getQuestProgress(profile, quest);
        float ratio = progress.total() == 0 ? 0f : (float) progress.completed() / progress.total();
        String progressText = progress.completed() + "/" + progress.total();
        String titleRaw = config.getQuestBossBarTitle()
                .replace("{quest}", quest.getName())
                .replace("{progress}", progressText)
                .replace("{completed}", String.valueOf(progress.completed()))
                .replace("{total}", String.valueOf(progress.total()));
        Component title = plugin.getMessageUtil().parse(titleRaw);
        BossBar bar = bossBars.computeIfAbsent(player.getUniqueId(), ignored ->
                BossBar.bossBar(title, ratio, BossBar.Color.BLUE, BossBar.Overlay.PROGRESS));
        bar.name(title);
        bar.progress(Math.min(1f, Math.max(0f, ratio)));
        player.showBossBar(bar);
    }

    public void hideBossBar(Player player) {
        BossBar bar = bossBars.remove(player.getUniqueId());
        if (bar != null) {
            player.hideBossBar(bar);
        }
    }

    public void clearAll() {
        for (Map.Entry<UUID, BossBar> entry : bossBars.entrySet()) {
            Player player = plugin.getServer().getPlayer(entry.getKey());
            if (player != null) {
                player.hideBossBar(entry.getValue());
            }
        }
        bossBars.clear();
    }

    public String formatAcceptMessage(String template, Quest quest) {
        if (template == null || template.isBlank()) {
            return "";
        }
        String questName = quest != null ? quest.getName() : "";
        return template.replace("{quest}", questName);
    }

    private static String replace(String template, String questName, String objectiveDescription) {
        String result = template.replace("{quest}", questName);
        if (objectiveDescription != null) {
            result = result.replace("{objective}", objectiveDescription);
        }
        return result;
    }
}
