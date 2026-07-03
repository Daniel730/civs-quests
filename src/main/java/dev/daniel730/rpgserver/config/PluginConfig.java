package dev.daniel730.rpgserver.config;

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
    private final boolean veinMinerEnabled;
    private final String questPermissionPrefix;
    private final int autosaveMinutes;
    private final int maxActiveQuests;
    private final String messagePrefix;
    private final String noPermissionMessage;
    private final String reloadSuccessMessage;

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
        this.veinMinerEnabled = config.getBoolean("integrations.veinminer.enabled", false);
        this.questPermissionPrefix = config.getString("integrations.luckperms.quest-permission-prefix", "rpg.quest.");
        this.autosaveMinutes = config.getInt("progression.autosave-minutes", 5);
        this.maxActiveQuests = config.getInt("quests.max-active", 3);
        this.messagePrefix = config.getString("messages.prefix", "<gray>[<gold>RPG</gold>]</gray> ");
        this.noPermissionMessage = config.getString("messages.no-permission", "<red>Sem permissão.</red>");
        this.reloadSuccessMessage = config.getString("messages.reload-success", "<green>Configuração recarregada.</green>");
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

    public boolean isVeinMinerEnabled() {
        return veinMinerEnabled;
    }

    public String getQuestPermissionPrefix() {
        return questPermissionPrefix;
    }

    public int getAutosaveMinutes() {
        return autosaveMinutes;
    }

    public int getMaxActiveQuests() {
        return maxActiveQuests;
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
}
