package dev.daniel730.rpgserver;

import dev.daniel730.rpgserver.command.RpgCommand;
import dev.daniel730.rpgserver.config.PluginConfig;
import dev.daniel730.rpgserver.hook.AuraSkillsHook;
import dev.daniel730.rpgserver.hook.ChestShopHook;
import dev.daniel730.rpgserver.hook.CivsCustomMobHook;
import dev.daniel730.rpgserver.hook.CivsHook;
import dev.daniel730.rpgserver.hook.EssentialsHook;
import dev.daniel730.rpgserver.hook.LuckPermsHook;
import dev.daniel730.rpgserver.hook.PlaceholderHook;
import dev.daniel730.rpgserver.hook.VaultHook;
import dev.daniel730.rpgserver.hook.VeinMinerHook;
import dev.daniel730.rpgserver.listener.AuctionQuestListener;
import dev.daniel730.rpgserver.listener.AuraSkillsQuestListener;
import dev.daniel730.rpgserver.listener.BukkitQuestListener;
import dev.daniel730.rpgserver.listener.CivsInternalSkillListener;
import dev.daniel730.rpgserver.listener.CivsQuestListener;
import dev.daniel730.rpgserver.listener.CivsSpellQuestListener;
import dev.daniel730.rpgserver.listener.EconomyQuestListener;
import dev.daniel730.rpgserver.discovery.DiscoveryRegistry;
import dev.daniel730.rpgserver.discovery.DiscoveryService;
import dev.daniel730.rpgserver.listener.CombatQuestListener;
import dev.daniel730.rpgserver.listener.CodexListener;
import dev.daniel730.rpgserver.listener.DiscoveryListener;
import dev.daniel730.rpgserver.listener.GuideNpcQuestListener;
import dev.daniel730.rpgserver.listener.RpgTutorialBridgeListener;
import dev.daniel730.rpgserver.listener.SkillTreeListener;
import dev.daniel730.rpgserver.progression.PathTraitService;
import dev.daniel730.rpgserver.progression.RebirthService;
import dev.daniel730.rpgserver.quest.HuntSpawnService;
import dev.daniel730.rpgserver.quest.LootTableService;
import dev.daniel730.rpgserver.listener.CivsIntegrationLifecycleListener;
import dev.daniel730.rpgserver.listener.PlayerHubListener;
import dev.daniel730.rpgserver.listener.PlayerProfileListener;
import dev.daniel730.rpgserver.listener.QuestJournalListener;
import dev.daniel730.rpgserver.listener.RebirthListener;
import dev.daniel730.rpgserver.profile.ProfileManager;
import dev.daniel730.rpgserver.progression.SkillTreeManager;
import dev.daniel730.rpgserver.quest.PlayerHubService;
import dev.daniel730.rpgserver.quest.QuestFeedbackService;
import dev.daniel730.rpgserver.quest.QuestManager;
import dev.daniel730.rpgserver.error.ErrorReportingConfig;
import dev.daniel730.rpgserver.error.ErrorReportingManager;
import dev.daniel730.rpgserver.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public final class RpgServerPlugin extends JavaPlugin {

    private static RpgServerPlugin instance;

    private PluginConfig pluginConfig;
    private VaultHook vaultHook;
    private CivsHook civsHook;
    private AuraSkillsHook auraSkillsHook;
    private ChestShopHook chestShopHook;
    private CivsCustomMobHook civsCustomMobHook;
    private EssentialsHook essentialsHook;
    private PlaceholderHook placeholderHook;
    private LuckPermsHook luckPermsHook;
    private VeinMinerHook veinMinerHook;
    private ProfileManager profileManager;
    private QuestManager questManager;
    private PlayerHubService playerHubService;
    private QuestFeedbackService questFeedbackService;
    private SkillTreeManager skillTreeManager;
    private DiscoveryRegistry discoveryRegistry;
    private DiscoveryService discoveryService;
    private HuntSpawnService huntSpawnService;
    private RebirthService rebirthService;
    private PathTraitService pathTraitService;
    private LootTableService lootTableService;
    private MessageUtil messageUtil;
    private ErrorReportingManager errorReportingManager;

    private CivsQuestListener civsQuestListener;
    private CivsInternalSkillListener civsInternalSkillListener;
    private AuctionQuestListener auctionQuestListener;
    private CivsSpellQuestListener civsSpellQuestListener;
    private AuraSkillsQuestListener auraSkillsQuestListener;
    private EconomyQuestListener economyQuestListener;
    private CombatQuestListener combatQuestListener;
    private GuideNpcQuestListener guideNpcQuestListener;
    private RpgTutorialBridgeListener rpgTutorialBridgeListener;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        pluginConfig = new PluginConfig(getConfig());
        messageUtil = new MessageUtil(pluginConfig);

        vaultHook = new VaultHook(this);
        civsHook = new CivsHook(this);
        auraSkillsHook = new AuraSkillsHook(this);
        chestShopHook = new ChestShopHook(this);
        civsCustomMobHook = new CivsCustomMobHook(this);
        essentialsHook = new EssentialsHook(this);
        placeholderHook = new PlaceholderHook(this);
        luckPermsHook = new LuckPermsHook(this);
        veinMinerHook = new VeinMinerHook(this);

        vaultHook.enable();
        civsHook.enable();
        auraSkillsHook.enable();
        essentialsHook.enable();
        placeholderHook.enable();
        luckPermsHook.enable();

        profileManager = new ProfileManager(this);
        questManager = new QuestManager(this);
        playerHubService = new PlayerHubService(this);
        questFeedbackService = new QuestFeedbackService(this);
        skillTreeManager = new SkillTreeManager(this);
        discoveryRegistry = new DiscoveryRegistry(this);
        discoveryService = new DiscoveryService(this, discoveryRegistry);
        huntSpawnService = new HuntSpawnService(this);
        rebirthService = new RebirthService(this);
        pathTraitService = new PathTraitService(this);
        lootTableService = new LootTableService(this);
        discoveryRegistry.load();
        lootTableService.load();
        questManager.loadQuests();
        skillTreeManager.loadPerks();

        profileManager.loadOnlinePlayers();
        for (org.bukkit.entity.Player online : Bukkit.getOnlinePlayers()) {
            questFeedbackService.refreshTrackedHud(online);
        }

        chestShopHook.enable();
        civsCustomMobHook.enable();
        veinMinerHook.enable();

        RpgCommand rpgCommand = new RpgCommand(this);
        getCommand("rpg").setExecutor(rpgCommand);
        getCommand("rpg").setTabCompleter(rpgCommand);

        getServer().getPluginManager().registerEvents(new PlayerProfileListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerHubListener(this), this);
        getServer().getPluginManager().registerEvents(new CivsIntegrationLifecycleListener(this), this);
        getServer().getPluginManager().registerEvents(new QuestJournalListener(this), this);
        getServer().getPluginManager().registerEvents(new BukkitQuestListener(this), this);
        getServer().getPluginManager().registerEvents(new DiscoveryListener(this), this);
        getServer().getPluginManager().registerEvents(new SkillTreeListener(this), this);
        getServer().getPluginManager().registerEvents(new CodexListener(this), this);
        getServer().getPluginManager().registerEvents(new RebirthListener(this), this);
        registerIntegrationListeners();

        errorReportingManager = new ErrorReportingManager(this);
        errorReportingManager.enable(new ErrorReportingConfig(getConfig()));

        int autosaveMinutes = pluginConfig.getAutosaveMinutes();
        long autosaveTicks = autosaveMinutes * 60L * 20L;
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, profileManager::saveDirtyProfiles, autosaveTicks, autosaveTicks);

        Bukkit.getScheduler().runTaskTimer(this, this::resetScheduledQuestsForOnlinePlayers,
                20L * 60L, 20L * 60L);

        getLogger().info("RPGServer habilitado (v" + getDescription().getVersion() + ").");
    }

    private void resetScheduledQuestsForOnlinePlayers() {
        // Re-check economy objectives here too: earn_money/balance_min otherwise only advance on
        // join and after ChestShop transactions, so money earned during a session would not count
        // toward those objectives until relog.
        boolean checkEconomy = vaultHook != null && vaultHook.isEnabled();
        for (org.bukkit.entity.Player online : Bukkit.getOnlinePlayers()) {
            questManager.resetExpiredScheduledQuests(online);
            if (checkEconomy) {
                questManager.checkEconomyObjectives(online);
            }
        }
    }

    @Override
    public void onDisable() {
        if (errorReportingManager != null) {
            errorReportingManager.disable();
        }
        if (questFeedbackService != null) {
            questFeedbackService.clearAll();
        }
        if (profileManager != null) {
            profileManager.saveAllSync();
        }
        if (chestShopHook != null) {
            chestShopHook.disable();
        }
        if (civsCustomMobHook != null) {
            civsCustomMobHook.disable();
        }
        if (veinMinerHook != null) {
            veinMinerHook.disable();
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
        skillTreeManager.loadPerks();
        discoveryRegistry.load();
        lootTableService.load();
        refreshIntegrationHooks();
        for (org.bukkit.entity.Player online : Bukkit.getOnlinePlayers()) {
            questManager.ensureProfileSanitized(online);
            questFeedbackService.refreshTrackedHud(online);
        }
        reregisterIntegrationListeners();
        if (errorReportingManager != null) {
            errorReportingManager.reload(new ErrorReportingConfig(getConfig()));
        }
    }

    private void refreshIntegrationHooks() {
        vaultHook.refresh();
        civsHook.refresh();
        auraSkillsHook.refresh();
        essentialsHook.refresh();
        luckPermsHook.refresh();
        placeholderHook.refresh();
    }

    private void registerIntegrationListeners() {
        if (vaultHook.isEnabled()) {
            economyQuestListener = new EconomyQuestListener(this);
            getServer().getPluginManager().registerEvents(economyQuestListener, this);
        }
        if (civsHook.isEnabled()) {
            civsQuestListener = new CivsQuestListener(this);
            civsInternalSkillListener = new CivsInternalSkillListener(this);
            auctionQuestListener = new AuctionQuestListener(this);
            civsSpellQuestListener = new CivsSpellQuestListener(this);
            getServer().getPluginManager().registerEvents(civsQuestListener, this);
            getServer().getPluginManager().registerEvents(civsInternalSkillListener, this);
            getServer().getPluginManager().registerEvents(auctionQuestListener, this);
            getServer().getPluginManager().registerEvents(civsSpellQuestListener, this);
            combatQuestListener = new CombatQuestListener(this);
            getServer().getPluginManager().registerEvents(combatQuestListener, this);
            guideNpcQuestListener = new GuideNpcQuestListener(this);
            getServer().getPluginManager().registerEvents(guideNpcQuestListener, this);
            rpgTutorialBridgeListener = new RpgTutorialBridgeListener(this);
            getServer().getPluginManager().registerEvents(rpgTutorialBridgeListener, this);
        }
        if (auraSkillsHook.isEnabled()) {
            auraSkillsQuestListener = new AuraSkillsQuestListener(this);
            getServer().getPluginManager().registerEvents(auraSkillsQuestListener, this);
        }
    }

    public void reregisterCivsIntegrationListeners() {
        reregisterIntegrationListeners();
    }

    private void reregisterIntegrationListeners() {
        if (chestShopHook != null) {
            chestShopHook.disable();
            chestShopHook.enable();
        }
        if (civsCustomMobHook != null) {
            civsCustomMobHook.disable();
            civsCustomMobHook.enable();
        }
        if (veinMinerHook != null) {
            veinMinerHook.disable();
            veinMinerHook.enable();
        }
        unregisterListener(economyQuestListener);
        unregisterListener(civsQuestListener);
        unregisterListener(civsInternalSkillListener);
        unregisterListener(auctionQuestListener);
        unregisterListener(civsSpellQuestListener);
        unregisterListener(combatQuestListener);
        unregisterListener(guideNpcQuestListener);
        unregisterListener(rpgTutorialBridgeListener);
        unregisterListener(auraSkillsQuestListener);
        economyQuestListener = null;
        civsQuestListener = null;
        civsInternalSkillListener = null;
        auctionQuestListener = null;
        civsSpellQuestListener = null;
        combatQuestListener = null;
        guideNpcQuestListener = null;
        rpgTutorialBridgeListener = null;
        auraSkillsQuestListener = null;
        registerIntegrationListeners();
    }

    private void unregisterListener(Listener listener) {
        if (listener != null) {
            HandlerList.unregisterAll(listener);
        }
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

    public ChestShopHook getChestShopHook() {
        return chestShopHook;
    }

    public CivsCustomMobHook getCivsCustomMobHook() {
        return civsCustomMobHook;
    }

    public EssentialsHook getEssentialsHook() {
        return essentialsHook;
    }

    public PlaceholderHook getPlaceholderHook() {
        return placeholderHook;
    }

    public LuckPermsHook getLuckPermsHook() {
        return luckPermsHook;
    }

    public VeinMinerHook getVeinMinerHook() {
        return veinMinerHook;
    }

    public ProfileManager getProfileManager() {
        return profileManager;
    }

    public QuestManager getQuestManager() {
        return questManager;
    }

    public PlayerHubService getPlayerHubService() {
        return playerHubService;
    }

    public QuestFeedbackService getQuestFeedbackService() {
        return questFeedbackService;
    }

    public SkillTreeManager getSkillTreeManager() {
        return skillTreeManager;
    }

    public MessageUtil getMessageUtil() {
        return messageUtil;
    }

    public DiscoveryService getDiscoveryService() {
        return discoveryService;
    }

    public DiscoveryRegistry getDiscoveryRegistry() {
        return discoveryRegistry;
    }

    public HuntSpawnService getHuntSpawnService() {
        return huntSpawnService;
    }

    public RebirthService getRebirthService() {
        return rebirthService;
    }

    public PathTraitService getPathTraitService() {
        return pathTraitService;
    }

    public LootTableService getLootTableService() {
        return lootTableService;
    }
}
