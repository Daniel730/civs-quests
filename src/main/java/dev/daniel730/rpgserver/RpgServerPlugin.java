package dev.daniel730.rpgserver;

import dev.daniel730.rpgserver.command.RpgCommand;
import dev.daniel730.rpgserver.config.PluginConfig;
import dev.daniel730.rpgserver.hook.AuraSkillsHook;
import dev.daniel730.rpgserver.hook.CivsHook;
import dev.daniel730.rpgserver.hook.LuckPermsHook;
import dev.daniel730.rpgserver.hook.PlaceholderHook;
import dev.daniel730.rpgserver.hook.VaultHook;
import dev.daniel730.rpgserver.listener.AuraSkillsQuestListener;
import dev.daniel730.rpgserver.listener.CivsQuestListener;
import dev.daniel730.rpgserver.listener.PlayerProfileListener;
import dev.daniel730.rpgserver.profile.ProfileManager;
import dev.daniel730.rpgserver.quest.QuestManager;
import dev.daniel730.rpgserver.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class RpgServerPlugin extends JavaPlugin {

    private static RpgServerPlugin instance;

    private PluginConfig pluginConfig;
    private VaultHook vaultHook;
    private CivsHook civsHook;
    private AuraSkillsHook auraSkillsHook;
    private PlaceholderHook placeholderHook;
    private LuckPermsHook luckPermsHook;
    private ProfileManager profileManager;
    private QuestManager questManager;
    private MessageUtil messageUtil;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        pluginConfig = new PluginConfig(getConfig());
        messageUtil = new MessageUtil(pluginConfig);

        vaultHook = new VaultHook(this);
        civsHook = new CivsHook(this);
        auraSkillsHook = new AuraSkillsHook(this);
        placeholderHook = new PlaceholderHook(this);
        luckPermsHook = new LuckPermsHook(this);

        vaultHook.enable();
        civsHook.enable();
        auraSkillsHook.enable();
        placeholderHook.enable();
        luckPermsHook.enable();

        profileManager = new ProfileManager(this);
        questManager = new QuestManager(this);
        questManager.loadQuests();

        profileManager.loadOnlinePlayers();

        RpgCommand rpgCommand = new RpgCommand(this);
        getCommand("rpg").setExecutor(rpgCommand);
        getCommand("rpg").setTabCompleter(rpgCommand);

        getServer().getPluginManager().registerEvents(new PlayerProfileListener(this), this);
        if (civsHook.isEnabled()) {
            getServer().getPluginManager().registerEvents(new CivsQuestListener(this), this);
        }
        if (auraSkillsHook.isEnabled()) {
            getServer().getPluginManager().registerEvents(new AuraSkillsQuestListener(this), this);
        }

        int autosaveMinutes = pluginConfig.getAutosaveMinutes();
        long autosaveTicks = autosaveMinutes * 60L * 20L;
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, profileManager::saveDirtyProfiles, autosaveTicks, autosaveTicks);

        getLogger().info("RPGServer habilitado (v" + getDescription().getVersion() + ").");
    }

    @Override
    public void onDisable() {
        if (profileManager != null) {
            profileManager.saveAllSync();
        }
        if (placeholderHook != null) {
            placeholderHook.disable();
        }
        instance = null;
    }

    public void reloadPlugin() {
        reloadConfig();
        pluginConfig = new PluginConfig(getConfig());
        messageUtil = new MessageUtil(pluginConfig);
        questManager.loadQuests();
    }

    public static RpgServerPlugin getInstance() {
        return instance;
    }

    public PluginConfig getPluginConfig() {
        return pluginConfig;
    }

    public VaultHook getVaultHook() {
        return vaultHook;
    }

    public CivsHook getCivsHook() {
        return civsHook;
    }

    public AuraSkillsHook getAuraSkillsHook() {
        return auraSkillsHook;
    }

    public PlaceholderHook getPlaceholderHook() {
        return placeholderHook;
    }

    public LuckPermsHook getLuckPermsHook() {
        return luckPermsHook;
    }

    public ProfileManager getProfileManager() {
        return profileManager;
    }

    public QuestManager getQuestManager() {
        return questManager;
    }

    public MessageUtil getMessageUtil() {
        return messageUtil;
    }
}
