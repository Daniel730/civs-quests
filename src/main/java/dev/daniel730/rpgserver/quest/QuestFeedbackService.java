package dev.daniel730.rpgserver.quest;

import dev.daniel730.rpgserver.RpgServerPlugin;
import dev.daniel730.rpgserver.config.PluginConfig;
import dev.daniel730.rpgserver.config.TrackedHudMode;
import dev.daniel730.rpgserver.profile.PlayerProfile;
import dev.daniel730.rpgserver.util.ArchetypeUtil;
import dev.daniel730.rpgserver.util.ProgressBarUtil;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Action bar / title notifications, sounds, particles, and tracked quest HUD (boss bar + sidebar).
 */
public final class QuestFeedbackService {

    private static final String SCOREBOARD_OBJECTIVE = "rpg_track";

    private final RpgServerPlugin plugin;
    private final Map<UUID, BossBar> bossBars = new ConcurrentHashMap<>();
    private final Map<UUID, Scoreboard> scoreboards = new ConcurrentHashMap<>();

    public QuestFeedbackService(RpgServerPlugin plugin) {
        this.plugin = plugin;
    }

    public void notifyObjectiveProgress(Player player, Quest quest, Quest.Objective objective,
                                        int current, int total) {
        PluginConfig config = plugin.getPluginConfig();
        PlayerProfile profile = plugin.getProfileManager().getOrCreate(player);
        if (!isNotificationsEnabled(profile)) {
            return;
        }
        String template = config.getQuestProgressActionBar();
        if (template.isBlank()) {
            return;
        }
        String message = replaceProgress(template, quest.getName(), objective.getDescription(), current, total);
        if (config.isQuestProgressPulse()) {
            pulseActionBar(player, message);
        } else {
            plugin.getMessageUtil().sendActionBar(player, message);
        }
        playSound(player, config.getQuestProgressSound(), config.getQuestProgressSoundVolume(),
                config.getQuestProgressSoundPitch());
    }

    public void notifyObjectiveComplete(Player player, Quest quest, Quest.Objective objective) {
        PluginConfig config = plugin.getPluginConfig();
        PlayerProfile profile = plugin.getProfileManager().getOrCreate(player);
        if (!isNotificationsEnabled(profile)) {
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
                    config.getQuestObjectiveTitleFadeIn(),
                    config.getQuestObjectiveTitleStay(),
                    config.getQuestObjectiveTitleFadeOut());
        }
        playSound(player, config.getQuestObjectiveCompleteSound(), config.getQuestObjectiveCompleteSoundVolume(),
                config.getQuestObjectiveCompleteSoundPitch());
        if (config.isQuestObjectiveCompleteParticles()) {
            spawnCompletionParticles(player);
        }
    }

    public void notifyQuestComplete(Player player, Quest quest) {
        PluginConfig config = plugin.getPluginConfig();
        PlayerProfile profile = plugin.getProfileManager().getOrCreate(player);
        if (!isNotificationsEnabled(profile)) {
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
                    config.getQuestCompleteTitleFadeIn(),
                    config.getQuestCompleteTitleStay(),
                    config.getQuestCompleteTitleFadeOut());
        }
        playSound(player, config.getQuestCompleteSound(), config.getQuestCompleteSoundVolume(),
                config.getQuestCompleteSoundPitch());
        if (config.isQuestCompleteFirework()) {
            spawnCelebrationFirework(player);
        }
        if (config.isQuestCompleteParticles()) {
            spawnCompletionParticles(player);
        }
    }

    public void playJournalOpen(Player player) {
        PluginConfig config = plugin.getPluginConfig();
        playSound(player, config.getJournalOpenSound(), config.getJournalSoundVolume(),
                config.getJournalOpenSoundPitch());
    }

    public void playJournalAccept(Player player) {
        PluginConfig config = plugin.getPluginConfig();
        playSound(player, config.getJournalAcceptSound(), config.getJournalSoundVolume(),
                config.getJournalAcceptSoundPitch());
    }

    public void playJournalTrack(Player player) {
        PluginConfig config = plugin.getPluginConfig();
        playSound(player, config.getJournalTrackSound(), config.getJournalSoundVolume(),
                config.getJournalTrackSoundPitch());
    }

    public void playJournalClick(Player player) {
        PluginConfig config = plugin.getPluginConfig();
        playSound(player, config.getJournalClickSound(), config.getJournalSoundVolume(), 1.2f);
    }

    public void showWelcome(Player player) {
        PluginConfig config = plugin.getPluginConfig();
        if (!config.isWelcomeEnabled()) {
            return;
        }
        if (!config.getWelcomeTitle().isBlank() || !config.getWelcomeSubtitle().isBlank()) {
            plugin.getMessageUtil().sendTitle(player,
                    config.getWelcomeTitle(),
                    config.getWelcomeSubtitle(),
                    config.getWelcomeTitleFadeIn(),
                    config.getWelcomeTitleStay(),
                    config.getWelcomeTitleFadeOut());
        }
        if (!config.getWelcomeMessage().isBlank()) {
            plugin.getMessageUtil().send(player, config.getWelcomeMessage());
        }
        playSound(player, config.getWelcomeSound(), config.getWelcomeSoundVolume(),
                config.getWelcomeSoundPitch());
        if (config.isWelcomeGiveHubItem()) {
            plugin.getPlayerHubService().giveHubItem(player);
        }
    }

    public boolean isNotificationsEnabled(PlayerProfile profile) {
        if (profile.getNotificationsEnabled() != null) {
            return profile.getNotificationsEnabled();
        }
        return plugin.getPluginConfig().isQuestNotificationsEnabled();
    }

    public boolean isBossBarEnabled(PlayerProfile profile) {
        if (profile.getBossBarEnabled() != null) {
            return profile.getBossBarEnabled();
        }
        return plugin.getPluginConfig().isQuestBossBarEnabled();
    }

    public void toggleNotifications(Player player) {
        PlayerProfile profile = plugin.getProfileManager().getOrCreate(player);
        boolean next = !isNotificationsEnabled(profile);
        profile.setNotificationsEnabled(next);
        plugin.getProfileManager().markDirty(player.getUniqueId());
        plugin.getMessageUtil().send(player, next
                ? plugin.getPluginConfig().getSettingsNotificationsOn()
                : plugin.getPluginConfig().getSettingsNotificationsOff());
        if (!next) {
            hideBossBar(player);
        }
        refreshTrackedHud(player);
    }

    public void toggleBossBar(Player player) {
        PlayerProfile profile = plugin.getProfileManager().getOrCreate(player);
        boolean next = !isBossBarEnabled(profile);
        profile.setBossBarEnabled(next);
        plugin.getProfileManager().markDirty(player.getUniqueId());
        plugin.getMessageUtil().send(player, next
                ? plugin.getPluginConfig().getSettingsBossBarOn()
                : plugin.getPluginConfig().getSettingsBossBarOff());
        refreshTrackedHud(player);
    }

    public void notifyDailyCtaIfNeeded(Player player) {
        PluginConfig config = plugin.getPluginConfig();
        if (!config.isDailyCtaEnabled() || !config.isQuestNotificationsEnabled()) {
            return;
        }
        PlayerProfile profile = plugin.getProfileManager().getOrCreate(player);
        QuestManager questManager = plugin.getQuestManager();
        if (!questManager.hasAvailableDailyQuest(player, profile)) {
            return;
        }
        java.time.ZoneId zone;
        try {
            zone = java.time.ZoneId.of(config.getQuestResetTimezone());
        } catch (Exception ex) {
            zone = java.time.ZoneId.of("UTC");
        }
        String today = questManager.currentPeriodDay(zone);
        if (today.equals(profile.getDailyCtaShownDay())) {
            return;
        }
        profile.setDailyCtaShownDay(today);
        plugin.getProfileManager().markDirty(player.getUniqueId());
        plugin.getMessageUtil().sendTitle(player,
                config.getDailyCtaTitle(),
                config.getDailyCtaSubtitle(),
                10, 60, 20);
    }

    public void refreshBossBar(Player player) {
        refreshTrackedHud(player);
    }

    public void refreshTrackedHud(Player player) {
        PluginConfig config = plugin.getPluginConfig();
        TrackedHudMode mode = config.getTrackedHudMode();
        PlayerProfile profile = plugin.getProfileManager().getOrCreate(player);

        if (!isNotificationsEnabled(profile) || mode == TrackedHudMode.NONE) {
            hideBossBar(player);
            hideScoreboard(player);
            return;
        }

        Quest quest = plugin.getQuestManager().findTrackedQuest(profile);
        if (quest == null) {
            hideBossBar(player);
            hideScoreboard(player);
            return;
        }

        Optional<Quest.Objective> currentObjective = plugin.getQuestManager().findCurrentObjective(profile, quest);
        if (currentObjective.isEmpty()) {
            hideBossBar(player);
            hideScoreboard(player);
            return;
        }

        Quest.Objective objective = currentObjective.get();
        int current = objective.isCountBased()
                ? profile.getObjectiveProgress(quest.getId(), objective.getId())
                : 0;
        int total = objective.isCountBased() ? objective.getAmount() : 1;
        float ratio = total == 0 ? 0f : Math.min(1f, (float) current / total);

        QuestManager.QuestProgress questProgress = plugin.getQuestManager().getQuestProgress(profile, quest);
        int percent = ProgressBarUtil.percent(questProgress.completed(), questProgress.total());

        if (mode.showBossBar() && isBossBarEnabled(profile)) {
            updateBossBar(player, quest, objective, current, total, ratio, questProgress);
        } else {
            hideBossBar(player);
        }

        if (mode.showScoreboard()) {
            updateScoreboard(player, quest, objective, current, total, percent, questProgress);
        } else {
            hideScoreboard(player);
        }
    }

    private void updateBossBar(Player player, Quest quest, Quest.Objective objective,
                               int current, int total, float ratio,
                               QuestManager.QuestProgress questProgress) {
        PluginConfig config = plugin.getPluginConfig();
        String progressText = questProgress.completed() + "/" + questProgress.total();
        String titleRaw = config.getQuestBossBarTitle()
                .replace("{quest}", quest.getName())
                .replace("{objective}", objective.getDescription())
                .replace("{current}", String.valueOf(current))
                .replace("{total}", String.valueOf(total))
                .replace("{progress}", progressText)
                .replace("{percent}", String.valueOf(ProgressBarUtil.percent(current, total)))
                .replace("{completed}", String.valueOf(questProgress.completed()))
                .replace("{objectives_total}", String.valueOf(questProgress.total()));
        Component title = plugin.getMessageUtil().parse(titleRaw);
        BossBar.Color color = ArchetypeUtil.bossBarColor(quest.getArchetype());
        BossBar bar = bossBars.computeIfAbsent(player.getUniqueId(), ignored ->
                BossBar.bossBar(title, ratio, color, BossBar.Overlay.PROGRESS));
        bar.name(title);
        bar.color(color);
        bar.progress(Math.min(1f, Math.max(0f, ratio)));
        player.showBossBar(bar);
    }

    private void updateScoreboard(Player player, Quest quest, Quest.Objective objective,
                                  int current, int total, int percent,
                                  QuestManager.QuestProgress questProgress) {
        Scoreboard board = scoreboards.computeIfAbsent(player.getUniqueId(), ignored ->
                Bukkit.getScoreboardManager().getNewScoreboard());
        Objective objectiveHandle = board.getObjective(SCOREBOARD_OBJECTIVE);
        if (objectiveHandle == null) {
            objectiveHandle = board.registerNewObjective(
                    SCOREBOARD_OBJECTIVE, Criteria.DUMMY,
                    plugin.getMessageUtil().parse("<gold>⚔ Quest</gold>"));
            objectiveHandle.setDisplaySlot(DisplaySlot.SIDEBAR);
        }
        objectiveHandle.displayName(plugin.getMessageUtil().parse(
                "<gold>" + truncatePlain(quest.getName(), 28) + "</gold>"));

        clearScores(board, objectiveHandle);

        int line = 6;
        setLine(board, objectiveHandle, line--,
                plugin.getMessageUtil().parse("<gray>" + truncatePlain(objective.getDescription(), 32) + "</gray>"));
        if (objective.isCountBased()) {
            setLine(board, objectiveHandle, line--,
                    plugin.getMessageUtil().parse("<white>" + current + "/" + total + "</white>"));
        }
        setLine(board, objectiveHandle, line--,
                plugin.getMessageUtil().parse(ProgressBarUtil.miniMessageBar(
                        questProgress.completed(), questProgress.total(), 8)));
        setLine(board, objectiveHandle, line--,
                plugin.getMessageUtil().parse("<yellow>" + percent + "%</yellow> <dark_gray>completo</dark_gray>"));
        if (quest.getArchetype() != null && !quest.getArchetype().isBlank()) {
            setLine(board, objectiveHandle, line,
                    plugin.getMessageUtil().parse(ArchetypeUtil.coloredDisplayName(quest.getArchetype())));
        }

        if (player.getScoreboard() != board) {
            player.setScoreboard(board);
        }
    }

    private static void setLine(Scoreboard board, Objective objective, int score, Component text) {
        String entry = scoreColorCode(score) + "§r";
        var team = board.getTeam("rpg" + score);
        if (team == null) {
            team = board.registerNewTeam("rpg" + score);
        }
        team.addEntry(entry);
        team.prefix(text);
        objective.getScore(entry).setScore(score);
    }

    private static String scoreColorCode(int score) {
        return "§" + Integer.toHexString(Math.max(0, Math.min(15, score)));
    }

    private static void clearScores(Scoreboard board, Objective objective) {
        for (String entry : board.getEntries()) {
            board.resetScores(entry);
        }
        for (var team : board.getTeams()) {
            if (team.getName().startsWith("rpg")) {
                team.unregister();
            }
        }
    }

    public void hideBossBar(Player player) {
        BossBar bar = bossBars.remove(player.getUniqueId());
        if (bar != null) {
            player.hideBossBar(bar);
        }
    }

    public void hideScoreboard(Player player) {
        Scoreboard board = scoreboards.remove(player.getUniqueId());
        if (board != null) {
            for (var team : board.getTeams()) {
                if (team.getName().startsWith("rpg")) {
                    team.unregister();
                }
            }
            Objective objective = board.getObjective(SCOREBOARD_OBJECTIVE);
            if (objective != null) {
                objective.unregister();
            }
            if (player.getScoreboard() == board) {
                player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
            }
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
        for (Map.Entry<UUID, Scoreboard> entry : scoreboards.entrySet()) {
            Player player = plugin.getServer().getPlayer(entry.getKey());
            if (player != null && player.getScoreboard() == entry.getValue()) {
                player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
            }
        }
        scoreboards.clear();
    }

    public String formatAcceptMessage(String template, Quest quest) {
        if (template == null || template.isBlank()) {
            return "";
        }
        String questName = quest != null ? quest.getName() : "";
        return template.replace("{quest}", questName);
    }

    private void pulseActionBar(Player player, String message) {
        plugin.getMessageUtil().sendActionBar(player, "<gold>▶</gold> " + message);
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                plugin.getMessageUtil().sendActionBar(player, message);
            }
        }, 3L);
    }

    private void spawnCelebrationFirework(Player player) {
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            Location location = player.getLocation().add(0, 1, 0);
            Firework firework = player.getWorld().spawn(location, Firework.class, fw -> {
                FireworkMeta meta = fw.getFireworkMeta();
                meta.addEffect(FireworkEffect.builder()
                        .withColor(Color.ORANGE, Color.YELLOW, Color.AQUA)
                        .withFade(Color.WHITE)
                        .with(FireworkEffect.Type.BALL_LARGE)
                        .trail(true)
                        .flicker(true)
                        .build());
                meta.setPower(1);
                fw.setFireworkMeta(meta);
            });
            plugin.getServer().getScheduler().runTaskLater(plugin, firework::detonate, 2L);
        });
    }

    private void spawnCompletionParticles(Player player) {
        Location location = player.getLocation().add(0, 1, 0);
        player.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, location, 24, 0.4, 0.6, 0.4, 0.05);
        player.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, location, 12, 0.5, 0.3, 0.5, 0);
    }

    private void playSound(Player player, String soundName, float volume, float pitch) {
        if (soundName == null || soundName.isBlank() || "none".equalsIgnoreCase(soundName)) {
            return;
        }
        try {
            Sound sound = Sound.valueOf(soundName.toUpperCase(Locale.ROOT));
            player.playSound(player.getLocation(), sound, volume, pitch);
        } catch (IllegalArgumentException ex) {
            player.playSound(player.getLocation(), soundName.toLowerCase(Locale.ROOT), volume, pitch);
        }
    }

    private static String truncatePlain(String text, int max) {
        if (text == null) {
            return "";
        }
        if (text.length() <= max) {
            return text;
        }
        return text.substring(0, max - 1) + "…";
    }

    private static String replace(String template, String questName, String objectiveDescription) {
        String result = template.replace("{quest}", questName);
        if (objectiveDescription != null) {
            result = result.replace("{objective}", objectiveDescription);
        }
        return result;
    }

    private static String replaceProgress(String template, String questName, String objectiveDescription,
                                            int current, int total) {
        return template.replace("{quest}", questName)
                .replace("{objective}", objectiveDescription)
                .replace("{current}", String.valueOf(current))
                .replace("{total}", String.valueOf(total))
                .replace("{percent}", String.valueOf(ProgressBarUtil.percent(current, total)));
    }
}
