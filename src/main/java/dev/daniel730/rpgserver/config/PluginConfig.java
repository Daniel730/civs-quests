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
}
