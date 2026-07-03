package dev.daniel730.rpgserver.config;

import dev.daniel730.rpgserver.quest.QuestAcceptResult;
import org.bukkit.configuration.file.FileConfiguration;

public final class PluginConfig {

    private final boolean debug;
    private final boolean civsEnabled;
    private final boolean auraSkillsEnabled;
    private final boolean vaultEnabled;
    private final boolean requireEconomy;
    private final boolean placeholderEnabled;
    private final boolean luckPermsEnabled;
    private final boolean chestShopEnabled;
    private final boolean essentialsEnabled;
    private final boolean essentialsKitRewardsEnabled;
    private final boolean essentialsWarpRewardsEnabled;
    private final boolean interactiveBooksEnabled;
    private final boolean questBookAutoGrant;
    private final boolean veinMinerEnabled;
    private final String questPermissionPrefix;
    private final int autosaveMinutes;
    private final boolean syncOnJoinFromCivs;
    private final int maxActiveQuests;
    private final boolean allowAbandon;
    private final String questResetTimezone;
    private final String messagePrefix;
    private final String noPermissionMessage;
    private final String reloadSuccessMessage;
    private final boolean questNotificationsEnabled;
    private final boolean questBossBarEnabled;
    private final String questObjectiveActionBar;
    private final String questObjectiveTitle;
    private final String questObjectiveSubtitle;
    private final String questCompleteActionBar;
    private final String questCompleteTitle;
    private final String questCompleteSubtitle;
    private final String questBossBarTitle;
    private final String questAcceptSuccess;
    private final String questAcceptNotFound;
    private final String questAcceptAlreadyComplete;
    private final String questAcceptAlreadyStarted;
    private final String questAcceptLocked;
    private final String questAcceptNoPermission;
    private final String questAcceptMaxActive;
    private final String questTrackSuccess;
    private final String questTrackFailed;
    private final int questProgressNotifyInterval;
    private final String questProgressActionBar;
    private final String questJournalLockedPrerequisite;
    private final String questJournalLockedPermission;
    private final String questJournalLockedGeneric;
    private final String questJournalTrackedBadge;
    private final String questJournalTrackHint;
    private final String questJournalAlreadyTracked;
    private final String questJournalAcceptHint;
    private final String questJournalAbandonHint;
    private final String questJournalCompletedHint;
    private final String questJournalPageIndicator;
    private final String questSyncSuccessAll;
    private final String questSyncSuccessOne;
    private final String questBookRefreshed;
    private final String questBookGrant;
    private final String questBookOpened;
    private final double rewardMoneyMultiplier;
    private final double rewardSkillXpMultiplier;
    private final double rewardCivsSkillXpMultiplier;
    private final String rewardSummaryHeader;
    private final String questJournalChainNext;
    private final String questJournalChainRequires;
    private final boolean dailyCtaEnabled;
    private final String dailyCtaTitle;
    private final String dailyCtaSubtitle;
    private final TrackedHudMode trackedHudMode;
    private final boolean questProgressPulse;
    private final String questProgressSound;
    private final float questProgressSoundVolume;
    private final float questProgressSoundPitch;
    private final String questObjectiveCompleteSound;
    private final float questObjectiveCompleteSoundVolume;
    private final float questObjectiveCompleteSoundPitch;
    private final boolean questObjectiveCompleteParticles;
    private final int questObjectiveTitleFadeIn;
    private final int questObjectiveTitleStay;
    private final int questObjectiveTitleFadeOut;
    private final String questCompleteSound;
    private final float questCompleteSoundVolume;
    private final float questCompleteSoundPitch;
    private final boolean questCompleteFirework;
    private final boolean questCompleteParticles;
    private final int questCompleteTitleFadeIn;
    private final int questCompleteTitleStay;
    private final int questCompleteTitleFadeOut;
    private final String journalOpenSound;
    private final float journalOpenSoundPitch;
    private final String journalAcceptSound;
    private final float journalAcceptSoundPitch;
    private final String journalTrackSound;
    private final float journalTrackSoundPitch;
    private final String journalClickSound;
    private final float journalSoundVolume;
    private final boolean welcomeEnabled;
    private final String welcomeTitle;
    private final String welcomeSubtitle;
    private final String welcomeMessage;
    private final String welcomeSound;
    private final float welcomeSoundVolume;
    private final float welcomeSoundPitch;
    private final boolean welcomeGiveQuestBook;
    private final int welcomeTitleFadeIn;
    private final int welcomeTitleStay;
    private final int welcomeTitleFadeOut;

    public PluginConfig(FileConfiguration config) {
        this.debug = config.getBoolean("settings.debug", false);
        this.civsEnabled = config.getBoolean("integrations.civs.enabled", true);
        this.auraSkillsEnabled = config.getBoolean("integrations.auraskills.enabled", true);
        this.vaultEnabled = config.getBoolean("integrations.vault.enabled", true);
        this.requireEconomy = config.getBoolean("integrations.vault.require-economy", true);
        this.placeholderEnabled = config.getBoolean("integrations.placeholderapi.enabled", true);
        this.luckPermsEnabled = config.getBoolean("integrations.luckperms.enabled", true);
        this.chestShopEnabled = config.getBoolean("integrations.chestshop.enabled", true);
        this.essentialsEnabled = config.getBoolean("integrations.essentials.enabled", true);
        this.essentialsKitRewardsEnabled = config.getBoolean("integrations.essentials.kit-rewards", true);
        this.essentialsWarpRewardsEnabled = config.getBoolean("integrations.essentials.warp-rewards", true);
        this.interactiveBooksEnabled = config.getBoolean("integrations.interactivebooks.enabled", true);
        this.questBookAutoGrant = config.getBoolean("integrations.interactivebooks.quest-book-auto-grant", true);
        this.veinMinerEnabled = config.getBoolean("integrations.veinminer.enabled", false);
        this.questPermissionPrefix = config.getString("integrations.luckperms.quest-permission-prefix", "rpg.quest.");
        this.autosaveMinutes = config.getInt("progression.autosave-minutes", 5);
        this.syncOnJoinFromCivs = config.getBoolean("progression.sync-on-join-from-civs", false);
        this.maxActiveQuests = config.getInt("quests.max-active", 3);
        this.allowAbandon = config.getBoolean("quests.allow-abandon", true);
        this.questResetTimezone = config.getString("quests.reset-timezone", "UTC");
        this.messagePrefix = config.getString("messages.prefix", "<gray>[<gold>RPG</gold>]</gray> ");
        this.noPermissionMessage = config.getString("messages.no-permission", "<red>Sem permissão.</red>");
        this.reloadSuccessMessage = config.getString("messages.reload-success", "<green>Configuração recarregada.</green>");
        this.questNotificationsEnabled = config.getBoolean("messages.quest-notifications", true);
        this.questBossBarEnabled = config.getBoolean("messages.quest-bossbar.enabled",
                config.getBoolean("messages.quest-bossbar", true));
        this.questObjectiveActionBar = config.getString("messages.quest-objective-complete.action-bar",
                "<yellow>✓</yellow> <white>{objective}</white>");
        this.questObjectiveTitle = config.getString("messages.quest-objective-complete.title",
                "<gold>Objetivo concluído</gold>");
        this.questObjectiveSubtitle = config.getString("messages.quest-objective-complete.subtitle",
                "<gray>{objective}</gray>");
        this.questCompleteActionBar = config.getString("messages.quest-complete.action-bar",
                "<green>✓</green> <white>{quest}</white>");
        this.questCompleteTitle = config.getString("messages.quest-complete.title",
                "<green>Quest concluída!</green>");
        this.questCompleteSubtitle = config.getString("messages.quest-complete.subtitle",
                "<white>{quest}</white>");
        this.questBossBarTitle = config.getString("messages.quest-bossbar-title",
                config.getString("messages.quest-bossbar.title",
                        "<gold>{quest}</gold> <gray>({progress})</gray>"));
        this.questAcceptSuccess = config.getString("messages.quest-accept.success",
                "<green>Quest aceita:</green> <white>{quest}</white>");
        this.questAcceptNotFound = config.getString("messages.quest-accept.not-found",
                "<red>Quest não encontrada.</red>");
        this.questAcceptAlreadyComplete = config.getString("messages.quest-accept.already-complete",
                "<red>Esta quest já foi concluída.</red>");
        this.questAcceptAlreadyStarted = config.getString("messages.quest-accept.already-started",
                "<red>Esta quest já está em andamento.</red>");
        this.questAcceptLocked = config.getString("messages.quest-accept.locked",
                "<red>Pré-requisitos não atendidos.</red>");
        this.questAcceptNoPermission = config.getString("messages.quest-accept.no-permission",
                "<red>Você não tem permissão para esta quest.</red>");
        this.questAcceptMaxActive = config.getString("messages.quest-accept.max-active",
                "<red>Limite de quests ativas atingido.</red>");
        this.questTrackSuccess = config.getString("messages.quest-track.success",
                "<yellow>Rastreando:</yellow> <white>{quest}</white>");
        this.questTrackFailed = config.getString("messages.quest-track.failed",
                "<red>Não foi possível rastrear esta quest.</red>");
        this.questProgressNotifyInterval = config.getInt("messages.quest-progress.interval", 5);
        this.questProgressActionBar = config.getString("messages.quest-progress.action-bar",
                "<gray>{objective}</gray> <white>({current}/{total})</white>");
        this.questJournalLockedPrerequisite = config.getString("messages.quest-journal.locked-prerequisite",
                "<red>Falta concluir:</red> <white>{quest}</white>");
        this.questJournalLockedPermission = config.getString("messages.quest-journal.locked-permission",
                "<red>Requer desbloqueio (permissão).</red>");
        this.questJournalLockedGeneric = config.getString("messages.quest-journal.locked-generic",
                "<red>Esta quest está bloqueada.</red> <gray>Cumpra os requisitos acima.</gray>");
        this.questJournalTrackedBadge = config.getString("messages.quest-journal.tracked-badge",
                "<gold>★ Quest rastreada</gold>");
        this.questJournalTrackHint = config.getString("messages.quest-journal.track-hint",
                "<gold>▶ Clique para rastrear</gold> <gray>(boss bar + notificações)</gray>");
        this.questJournalAlreadyTracked = config.getString("messages.quest-journal.already-tracked",
                "<gray>{quest} já está sendo rastreada.</gray>");
        this.questJournalAcceptHint = config.getString("messages.quest-journal.accept-hint",
                "<yellow>▶ Clique para aceitar a quest.</yellow>");
        this.questJournalAbandonHint = config.getString("messages.quest-journal.abandon-hint",
                "<red>▶ Shift-clique para abandonar.</red>");
        this.questJournalCompletedHint = config.getString("messages.quest-journal.completed-hint",
                "<aqua>✔ Quest concluída.</aqua>");
        this.questJournalPageIndicator = config.getString("messages.quest-journal.page-indicator",
                "<dark_gray>Página {page}/{total}</dark_gray>");
        this.questSyncSuccessAll = config.getString("messages.quest-sync.success-all",
                "<green>Sync concluído</green> para <white>{players}</white> jogador(es): "
                        + "<yellow>{objectives}</yellow> objetivo(s) e <yellow>{quests}</yellow> quest(s) backfilled.");
        this.questSyncSuccessOne = config.getString("messages.quest-sync.success-one",
                "<green>Sync concluído</green> para <white>{player}</white>: "
                        + "<yellow>{objectives}</yellow> objetivo(s) e <yellow>{quests}</yellow> quest(s) backfilled.");
        this.questBookRefreshed = config.getString("messages.quest-book.refreshed",
                "<gray>Livro atualizado:</gray> <white>{quest}</white>");
        this.questBookGrant = config.getString("messages.quest-book.grant",
                "<gray>Livro de quest recebido:</gray> <white>{quest}</white>");
        this.questBookOpened = config.getString("messages.quest-book.opened",
                "<gray>Abrindo livro:</gray> <white>{quest}</white>");
        this.questJournalChainNext = config.getString("messages.quest-journal.chain-next",
                "<aqua>Próximo:</aqua> <white>{quest}</white>");
        this.questJournalChainRequires = config.getString("messages.quest-journal.chain-requires",
                "<red>Requer:</red> <white>{quests}</white>");
        this.dailyCtaEnabled = config.getBoolean("messages.daily-cta.enabled", true);
        this.dailyCtaTitle = config.getString("messages.daily-cta.title",
                "<gold><bold>Missão Diária disponível!</bold></gold>");
        this.dailyCtaSubtitle = config.getString("messages.daily-cta.subtitle",
                "<gray>Abra o diário com /rpg journal</gray>");
        this.trackedHudMode = TrackedHudMode.fromConfig(config.getString("feedback.tracked-hud", "both"));
        this.questProgressPulse = config.getBoolean("feedback.quest-progress.pulse", true);
        this.questProgressSound = config.getString("feedback.quest-progress.sound", "BLOCK_NOTE_BLOCK_PLING");
        this.questProgressSoundVolume = (float) config.getDouble("feedback.quest-progress.sound-volume", 0.35);
        this.questProgressSoundPitch = (float) config.getDouble("feedback.quest-progress.sound-pitch", 1.6);
        this.questObjectiveCompleteSound = config.getString("feedback.quest-objective-complete.sound",
                "ENTITY_EXPERIENCE_ORB_PICKUP");
        this.questObjectiveCompleteSoundVolume = (float) config.getDouble(
                "feedback.quest-objective-complete.sound-volume", 0.8);
        this.questObjectiveCompleteSoundPitch = (float) config.getDouble(
                "feedback.quest-objective-complete.sound-pitch", 1.3);
        this.questObjectiveCompleteParticles = config.getBoolean("feedback.quest-objective-complete.particles", true);
        this.questObjectiveTitleFadeIn = config.getInt("feedback.quest-objective-complete.title-fade-in", 5);
        this.questObjectiveTitleStay = config.getInt("feedback.quest-objective-complete.title-stay", 40);
        this.questObjectiveTitleFadeOut = config.getInt("feedback.quest-objective-complete.title-fade-out", 10);
        this.questCompleteSound = config.getString("feedback.quest-complete.sound", "ENTITY_PLAYER_LEVELUP");
        this.questCompleteSoundVolume = (float) config.getDouble("feedback.quest-complete.sound-volume", 1.0);
        this.questCompleteSoundPitch = (float) config.getDouble("feedback.quest-complete.sound-pitch", 1.2);
        this.questCompleteFirework = config.getBoolean("feedback.quest-complete.firework", true);
        this.questCompleteParticles = config.getBoolean("feedback.quest-complete.particles", true);
        this.questCompleteTitleFadeIn = config.getInt("feedback.quest-complete.title-fade-in", 10);
        this.questCompleteTitleStay = config.getInt("feedback.quest-complete.title-stay", 70);
        this.questCompleteTitleFadeOut = config.getInt("feedback.quest-complete.title-fade-out", 20);
        this.journalOpenSound = config.getString("feedback.journal.open-sound", "ITEM_BOOK_PAGE_TURN");
        this.journalOpenSoundPitch = (float) config.getDouble("feedback.journal.open-sound-pitch", 1.0);
        this.journalAcceptSound = config.getString("feedback.journal.accept-sound", "ENTITY_PLAYER_LEVELUP");
        this.journalAcceptSoundPitch = (float) config.getDouble("feedback.journal.accept-sound-pitch", 1.4);
        this.journalTrackSound = config.getString("feedback.journal.track-sound", "BLOCK_NOTE_BLOCK_CHIME");
        this.journalTrackSoundPitch = (float) config.getDouble("feedback.journal.track-sound-pitch", 1.2);
        this.journalClickSound = config.getString("feedback.journal.click-sound", "UI_BUTTON_CLICK");
        this.journalSoundVolume = (float) config.getDouble("feedback.journal.sound-volume", 0.7);
        this.welcomeEnabled = config.getBoolean("feedback.welcome.enabled", true);
        this.welcomeTitle = config.getString("messages.welcome.title",
                "<gradient:#FFD700:#FF8C00:#FFD700><bold>Bem-vindo ao Reino</bold></gradient>");
        this.welcomeSubtitle = config.getString("messages.welcome.subtitle",
                "<gray>Abra </gray><yellow>/rpg journal</yellow><gray> para começar</gray>");
        this.welcomeMessage = config.getString("messages.welcome.message",
                "<gold>★</gold> <white>Sua aventura começa agora!</white> "
                        + "<gray>Use</gray> <yellow>/rpg journal</yellow> <gray>para escolher um caminho.</gray>");
        this.welcomeSound = config.getString("feedback.welcome.sound", "ENTITY_PLAYER_LEVELUP");
        this.welcomeSoundVolume = (float) config.getDouble("feedback.welcome.sound-volume", 0.9);
        this.welcomeSoundPitch = (float) config.getDouble("feedback.welcome.sound-pitch", 0.9);
        this.welcomeGiveQuestBook = config.getBoolean("feedback.welcome.give-quest-book", true);
        this.welcomeTitleFadeIn = config.getInt("feedback.welcome.title-fade-in", 15);
        this.welcomeTitleStay = config.getInt("feedback.welcome.title-stay", 80);
        this.welcomeTitleFadeOut = config.getInt("feedback.welcome.title-fade-out", 25);
        this.rewardMoneyMultiplier = config.getDouble("progression.reward-multipliers.money", 1.0);
        this.rewardSkillXpMultiplier = config.getDouble("progression.reward-multipliers.skill-xp", 1.0);
        this.rewardCivsSkillXpMultiplier = config.getDouble("progression.reward-multipliers.civs-skill-xp", 1.0);
        this.rewardSummaryHeader = config.getString("messages.reward-summary.header",
                "<gold>★ Recompensas recebidas</gold>");
    }

    public boolean isDebug() {
        return debug;
    }

    public boolean isCivsEnabled() {
        return civsEnabled;
    }

    public boolean isAuraSkillsEnabled() {
        return auraSkillsEnabled;
    }

    public boolean isVaultEnabled() {
        return vaultEnabled;
    }

    public boolean isRequireEconomy() {
        return requireEconomy;
    }

    public boolean isPlaceholderEnabled() {
        return placeholderEnabled;
    }

    public boolean isLuckPermsEnabled() {
        return luckPermsEnabled;
    }

    public boolean isChestShopEnabled() {
        return chestShopEnabled;
    }

    public boolean isEssentialsEnabled() {
        return essentialsEnabled;
    }

    public boolean isEssentialsKitRewardsEnabled() {
        return essentialsKitRewardsEnabled;
    }

    public boolean isEssentialsWarpRewardsEnabled() {
        return essentialsWarpRewardsEnabled;
    }

    public boolean isInteractiveBooksEnabled() {
        return interactiveBooksEnabled;
    }

    public boolean isQuestBookAutoGrant() {
        return questBookAutoGrant;
    }

    public boolean isVeinMinerEnabled() {
        return veinMinerEnabled;
    }

    public String getQuestPermissionPrefix() {
        return questPermissionPrefix;
    }

    public int getAutosaveMinutes() {
        return autosaveMinutes;
    }

    public boolean isSyncOnJoinFromCivs() {
        return syncOnJoinFromCivs;
    }

    public int getMaxActiveQuests() {
        return maxActiveQuests;
    }

    public boolean isAllowAbandon() {
        return allowAbandon;
    }

    public String getQuestResetTimezone() {
        return questResetTimezone;
    }

    public String getMessagePrefix() {
        return messagePrefix;
    }

    public String getNoPermissionMessage() {
        return noPermissionMessage;
    }

    public String getReloadSuccessMessage() {
        return reloadSuccessMessage;
    }

    public boolean isQuestNotificationsEnabled() {
        return questNotificationsEnabled;
    }

    public boolean isQuestBossBarEnabled() {
        return questBossBarEnabled;
    }

    public String getQuestObjectiveActionBar() {
        return questObjectiveActionBar;
    }

    public String getQuestObjectiveTitle() {
        return questObjectiveTitle;
    }

    public String getQuestObjectiveSubtitle() {
        return questObjectiveSubtitle;
    }

    public String getQuestCompleteActionBar() {
        return questCompleteActionBar;
    }

    public String getQuestCompleteTitle() {
        return questCompleteTitle;
    }

    public String getQuestCompleteSubtitle() {
        return questCompleteSubtitle;
    }

    public String getQuestBossBarTitle() {
        return questBossBarTitle;
    }

    public String getQuestAcceptSuccess() {
        return questAcceptSuccess;
    }

    public String getQuestAcceptNotFound() {
        return questAcceptNotFound;
    }

    public String getQuestAcceptAlreadyComplete() {
        return questAcceptAlreadyComplete;
    }

    public String getQuestAcceptAlreadyStarted() {
        return questAcceptAlreadyStarted;
    }

    public String getQuestAcceptLocked() {
        return questAcceptLocked;
    }

    public String getQuestAcceptNoPermission() {
        return questAcceptNoPermission;
    }

    public String getQuestAcceptMaxActive() {
        return questAcceptMaxActive;
    }

    public String getQuestTrackSuccess() {
        return questTrackSuccess;
    }

    public String getQuestTrackFailed() {
        return questTrackFailed;
    }

    public int getQuestProgressNotifyInterval() {
        return questProgressNotifyInterval;
    }

    public String getQuestProgressActionBar() {
        return questProgressActionBar;
    }

    public String getQuestJournalLockedPrerequisite() {
        return questJournalLockedPrerequisite;
    }

    public String getQuestJournalLockedPermission() {
        return questJournalLockedPermission;
    }

    public String getQuestJournalLockedGeneric() {
        return questJournalLockedGeneric;
    }

    public String getQuestJournalTrackedBadge() {
        return questJournalTrackedBadge;
    }

    public String getQuestJournalTrackHint() {
        return questJournalTrackHint;
    }

    public String getQuestJournalAlreadyTracked() {
        return questJournalAlreadyTracked;
    }

    public String getQuestJournalAcceptHint() {
        return questJournalAcceptHint;
    }

    public String getQuestJournalAbandonHint() {
        return questJournalAbandonHint;
    }

    public String getQuestJournalCompletedHint() {
        return questJournalCompletedHint;
    }

    public String getQuestJournalPageIndicator() {
        return questJournalPageIndicator;
    }

    public String getQuestSyncSuccessAll() {
        return questSyncSuccessAll;
    }

    public String getQuestSyncSuccessOne() {
        return questSyncSuccessOne;
    }

    public String getQuestBookRefreshed() {
        return questBookRefreshed;
    }

    public String getQuestBookGrant() {
        return questBookGrant;
    }

    public String getQuestBookOpened() {
        return questBookOpened;
    }

    public double getRewardMoneyMultiplier() {
        return rewardMoneyMultiplier;
    }

    public double getRewardSkillXpMultiplier() {
        return rewardSkillXpMultiplier;
    }

    public double getRewardCivsSkillXpMultiplier() {
        return rewardCivsSkillXpMultiplier;
    }

    public String getRewardSummaryHeader() {
        return rewardSummaryHeader;
    }

    public String getQuestJournalChainNext() {
        return questJournalChainNext;
    }

    public String getQuestJournalChainRequires() {
        return questJournalChainRequires;
    }

    public boolean isDailyCtaEnabled() {
        return dailyCtaEnabled;
    }

    public String getDailyCtaTitle() {
        return dailyCtaTitle;
    }

    public String getDailyCtaSubtitle() {
        return dailyCtaSubtitle;
    }

    public String getQuestAcceptMessage(QuestAcceptResult result) {
        return switch (result) {
            case SUCCESS -> questAcceptSuccess;
            case NOT_FOUND -> questAcceptNotFound;
            case ALREADY_COMPLETE -> questAcceptAlreadyComplete;
            case ALREADY_STARTED -> questAcceptAlreadyStarted;
            case LOCKED -> questAcceptLocked;
            case NO_PERMISSION -> questAcceptNoPermission;
            case MAX_ACTIVE -> questAcceptMaxActive;
        };
    }

    public TrackedHudMode getTrackedHudMode() {
        return trackedHudMode;
    }

    public boolean isQuestProgressPulse() {
        return questProgressPulse;
    }

    public String getQuestProgressSound() {
        return questProgressSound;
    }

    public float getQuestProgressSoundVolume() {
        return questProgressSoundVolume;
    }

    public float getQuestProgressSoundPitch() {
        return questProgressSoundPitch;
    }

    public String getQuestObjectiveCompleteSound() {
        return questObjectiveCompleteSound;
    }

    public float getQuestObjectiveCompleteSoundVolume() {
        return questObjectiveCompleteSoundVolume;
    }

    public float getQuestObjectiveCompleteSoundPitch() {
        return questObjectiveCompleteSoundPitch;
    }

    public boolean isQuestObjectiveCompleteParticles() {
        return questObjectiveCompleteParticles;
    }

    public int getQuestObjectiveTitleFadeIn() {
        return questObjectiveTitleFadeIn;
    }

    public int getQuestObjectiveTitleStay() {
        return questObjectiveTitleStay;
    }

    public int getQuestObjectiveTitleFadeOut() {
        return questObjectiveTitleFadeOut;
    }

    public String getQuestCompleteSound() {
        return questCompleteSound;
    }

    public float getQuestCompleteSoundVolume() {
        return questCompleteSoundVolume;
    }

    public float getQuestCompleteSoundPitch() {
        return questCompleteSoundPitch;
    }

    public boolean isQuestCompleteFirework() {
        return questCompleteFirework;
    }

    public boolean isQuestCompleteParticles() {
        return questCompleteParticles;
    }

    public int getQuestCompleteTitleFadeIn() {
        return questCompleteTitleFadeIn;
    }

    public int getQuestCompleteTitleStay() {
        return questCompleteTitleStay;
    }

    public int getQuestCompleteTitleFadeOut() {
        return questCompleteTitleFadeOut;
    }

    public String getJournalOpenSound() {
        return journalOpenSound;
    }

    public float getJournalOpenSoundPitch() {
        return journalOpenSoundPitch;
    }

    public String getJournalAcceptSound() {
        return journalAcceptSound;
    }

    public float getJournalAcceptSoundPitch() {
        return journalAcceptSoundPitch;
    }

    public String getJournalTrackSound() {
        return journalTrackSound;
    }

    public float getJournalTrackSoundPitch() {
        return journalTrackSoundPitch;
    }

    public String getJournalClickSound() {
        return journalClickSound;
    }

    public float getJournalSoundVolume() {
        return journalSoundVolume;
    }

    public boolean isWelcomeEnabled() {
        return welcomeEnabled;
    }

    public String getWelcomeTitle() {
        return welcomeTitle;
    }

    public String getWelcomeSubtitle() {
        return welcomeSubtitle;
    }

    public String getWelcomeMessage() {
        return welcomeMessage;
    }

    public String getWelcomeSound() {
        return welcomeSound;
    }

    public float getWelcomeSoundVolume() {
        return welcomeSoundVolume;
    }

    public float getWelcomeSoundPitch() {
        return welcomeSoundPitch;
    }

    public boolean isWelcomeGiveQuestBook() {
        return welcomeGiveQuestBook;
    }

    public int getWelcomeTitleFadeIn() {
        return welcomeTitleFadeIn;
    }

    public int getWelcomeTitleStay() {
        return welcomeTitleStay;
    }

    public int getWelcomeTitleFadeOut() {
        return welcomeTitleFadeOut;
    }
}
