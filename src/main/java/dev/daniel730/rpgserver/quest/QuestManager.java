package dev.daniel730.rpgserver.quest;

import dev.daniel730.rpgserver.RpgServerPlugin;
import dev.daniel730.rpgserver.profile.PlayerProfile;
import dev.daniel730.rpgserver.quest.objective.ObjectiveTypeRegistry;
import dev.daniel730.rpgserver.quest.objective.ObjectiveTypes;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Predicate;
import java.util.logging.Level;

public final class QuestManager {

    private final RpgServerPlugin plugin;
    private final ObjectiveTypeRegistry objectiveTypeRegistry;
    private final RewardExecutor rewardExecutor;
    private final QuestProgressSync progressSync;
    private final Map<String, Quest> quests = new LinkedHashMap<>();

    public QuestManager(RpgServerPlugin plugin) {
        this.plugin = plugin;
        this.objectiveTypeRegistry = new ObjectiveTypeRegistry();
        this.objectiveTypeRegistry.registerDefaults();
        this.rewardExecutor = new RewardExecutor(plugin);
        this.progressSync = new QuestProgressSync(plugin);
    }

    public QuestProgressSync getProgressSync() {
        return progressSync;
    }

    public ObjectiveTypeRegistry getObjectiveTypeRegistry() {
        return objectiveTypeRegistry;
    }

    public void loadQuests() {
        quests.clear();
        File questsFolder = new File(plugin.getDataFolder(), "quests");
        if (!questsFolder.exists()) {
            questsFolder.mkdirs();
            plugin.saveResource("quests/warrior_path.yml", false);
            plugin.saveResource("quests/builder_path.yml", false);
            plugin.saveResource("quests/merchant_path.yml", false);
            plugin.saveResource("quests/sprint1_examples.yml", false);
            plugin.saveResource("quests/sprint2_examples.yml", false);
            plugin.saveResource("quests/sprint2_auction.yml", false);
            plugin.saveResource("quests/sprint3_daily.yml", false);
            plugin.saveResource("quests/sprint3_boss.yml", false);
            plugin.saveResource("quests/warrior_champion.yml", false);
            plugin.saveResource("quests/daily_quarry.yml", false);
            plugin.saveResource("quests/weekly_warrior.yml", false);
            plugin.saveResource("quests/weekly_merchant.yml", false);
            plugin.saveResource("quests/weekly_builder.yml", false);
            plugin.saveResource("quests/sprint2_spells.yml", false);
        }

        File[] files = questsFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) {
            return;
        }

        for (File file : files) {
            try {
                Quest quest = parseQuest(YamlConfiguration.loadConfiguration(file));
                quests.put(quest.getId(), quest);
            } catch (Exception ex) {
                plugin.getLogger().log(Level.WARNING, "Falha ao carregar quest " + file.getName(), ex);
            }
        }
        plugin.getLogger().info("Carregadas " + quests.size() + " quests.");
    }

    private Quest parseQuest(YamlConfiguration config) {
        String id = config.getString("id");
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Campo 'id' obrigatório");
        }
        String name = config.getString("name", id);
        String archetype = config.getString("archetype", "");
        String description = config.getString("description", "");
        String loreBook = config.getString("lore-book");
        String unlocksPerk = config.getString("unlocks-perk");
        QuestSchedule schedule = QuestSchedule.fromYaml(config.getString("schedule"));
        List<String> requiredQuestIds = config.getStringList("requires");
        List<Map<?, ?>> rawObjectives = config.getMapList("objectives");
        List<Quest.Objective> objectives = new ArrayList<>();
        for (Map<?, ?> raw : rawObjectives) {
            objectives.add(objectiveTypeRegistry.parseObjective(raw));
        }
        RewardDefinition rewards = RewardDefinition.fromConfig(config.getConfigurationSection("rewards"));
        return new Quest(id, name, archetype, description, loreBook, unlocksPerk, schedule,
                requiredQuestIds, objectives, rewards);
    }

    public void resetExpiredScheduledQuests(Player player) {
        PlayerProfile profile = plugin.getProfileManager().getOrCreate(player);
        ZoneId zone = resolveResetZone();
        boolean changed = false;

        for (Quest quest : quests.values()) {
            if (!quest.isScheduled() || !profile.isQuestComplete(quest.getId())) {
                continue;
            }
            Long completedAt = profile.getQuestCompletedAt(quest.getId());
            if (completedAt == null) {
                continue;
            }
            if (QuestScheduleReset.isPeriodExpired(quest.getSchedule(), completedAt, zone)) {
                profile.clearQuestState(quest.getId());
                changed = true;
            }
        }

        if (changed) {
            plugin.getProfileManager().markDirty(player.getUniqueId());
        }
    }

    private ZoneId resolveResetZone() {
        try {
            return ZoneId.of(plugin.getPluginConfig().getQuestResetTimezone());
        } catch (Exception ex) {
            plugin.getLogger().warning("quests.reset-timezone inválido; usando UTC.");
            return ZoneId.of("UTC");
        }
    }

    public Quest getQuest(String id) {
        return quests.get(id);
    }

    public Collection<Quest> getAllQuests() {
        return Collections.unmodifiableCollection(quests.values());
    }

    public void handleRegionBuilt(Player player, String regionKey) {
        if (regionKey == null || regionKey.isBlank()) {
            return;
        }
        String normalizedRegion = regionKey.toLowerCase(Locale.ROOT);
        processInstantObjectives(player, ObjectiveTypes.BUILD_REGION, objective ->
                objective.getRegion() != null
                        && objective.getRegion().equalsIgnoreCase(normalizedRegion));
    }

    public void handleSkillLevelUp(Player player, String skillKey, int level) {
        if (skillKey == null || skillKey.isBlank()) {
            return;
        }
        String normalizedSkill = skillKey.toLowerCase(Locale.ROOT);
        processInstantObjectives(player, ObjectiveTypes.SKILL_LEVEL, objective ->
                objective.getSkill() != null
                        && objective.getSkill().equalsIgnoreCase(normalizedSkill)
                        && level >= objective.getTargetLevel());
    }

    public void handleKillMob(Player player, String mobKey) {
        if (mobKey == null || mobKey.isBlank()) {
            return;
        }
        String normalizedMob = mobKey.toLowerCase(Locale.ROOT);
        processCountObjectives(player, ObjectiveTypes.KILL_MOB, objective ->
                objective.getMob() == null
                        || objective.getMob().equalsIgnoreCase(normalizedMob)
                        || normalizedMob.contains(objective.getMob().toLowerCase(Locale.ROOT)), 1);
        checkEconomyObjectives(player);
    }

    public void handleCustomMobKill(Player player, String mobId) {
        if (!plugin.getCivsHook().isEnabled() || mobId == null || mobId.isBlank()) {
            return;
        }
        String normalizedMob = mobId.toLowerCase(Locale.ROOT);
        processCountObjectives(player, ObjectiveTypes.CUSTOM_MOB_KILL, objective ->
                objective.getMob() != null
                        && (objective.getMob().equalsIgnoreCase(normalizedMob)
                        || normalizedMob.contains(objective.getMob().toLowerCase(Locale.ROOT))), 1);
    }

    public void handleMineBlock(Player player, String blockKey) {
        if (blockKey == null || blockKey.isBlank()) {
            return;
        }
        String normalizedBlock = blockKey.toLowerCase(Locale.ROOT);
        processCountObjectives(player, ObjectiveTypes.MINE_BLOCK, objective ->
                objective.getBlock() == null
                        || objective.getBlock().equalsIgnoreCase(normalizedBlock), 1);
    }

    public void handleVeinMine(Player player, String blockKey, int increment) {
        if (!plugin.getPluginConfig().isVeinMinerEnabled() || blockKey == null || blockKey.isBlank()
                || increment <= 0) {
            return;
        }
        String normalizedBlock = blockKey.toLowerCase(Locale.ROOT);
        processCountObjectives(player, ObjectiveTypes.VEIN_MINE, objective ->
                objective.getBlock() == null
                        || objective.getBlock().equalsIgnoreCase(normalizedBlock), increment);
    }

    public void handleShopBuy(Player player, int increment) {
        processCountObjectives(player, ObjectiveTypes.SHOP_BUY, objective -> true, increment);
        checkEconomyObjectives(player);
    }

    public void handleShopSell(Player player, int increment) {
        processCountObjectives(player, ObjectiveTypes.SHOP_SELL, objective -> true, increment);
        checkEconomyObjectives(player);
    }

    public void handleShopRevenue(Player player, int increment) {
        processCountObjectives(player, ObjectiveTypes.SHOP_REVENUE, objective -> true, increment);
        checkEconomyObjectives(player);
    }

    public void handleCivsSkillXp(Player player, String skillKey, double xp) {
        if (!plugin.getCivsHook().isEnabled() || skillKey == null || skillKey.isBlank() || xp <= 0) {
            return;
        }
        String normalizedSkill = skillKey.toLowerCase(Locale.ROOT);
        int increment = (int) Math.floor(xp);
        if (increment <= 0) {
            return;
        }
        processCountObjectives(player, ObjectiveTypes.CIVS_SKILL_XP, objective ->
                objective.getSkill() == null
                        || objective.getSkill().equalsIgnoreCase(normalizedSkill), increment);
    }

    public void checkCivsSkillLevels(Player player) {
        if (!plugin.getCivsHook().isEnabled()) {
            return;
        }
        processInstantObjectives(player, ObjectiveTypes.CIVS_SKILL_LEVEL, objective ->
                objective.getSkill() != null
                        && plugin.getCivsHook().getSkillLevel(player, objective.getSkill())
                        >= objective.getTargetLevel());
    }

    public void handleAuctionList(Player player, int increment) {
        if (!plugin.getCivsHook().isEnabled() || increment <= 0) {
            return;
        }
        processCountObjectives(player, ObjectiveTypes.AUCTION_LIST, objective -> true, increment);
    }

    public void handleAuctionBuy(Player player, int increment) {
        if (!plugin.getCivsHook().isEnabled() || increment <= 0) {
            return;
        }
        processCountObjectives(player, ObjectiveTypes.AUCTION_BUY, objective -> true, increment);
    }

    public void handleSpellCast(Player player, String spellId) {
        if (!plugin.getCivsHook().isEnabled() || spellId == null || spellId.isBlank()) {
            return;
        }
        String normalizedSpell = spellId.toLowerCase(Locale.ROOT);
        processCountObjectives(player, ObjectiveTypes.CAST_SPELL, objective ->
                objective.getMob() == null
                        || objective.getMob().equalsIgnoreCase(normalizedSpell), 1);
    }

    public void checkEconomyObjectives(Player player) {
        if (!plugin.getVaultHook().isEnabled()) {
            return;
        }
        PlayerProfile profile = plugin.getProfileManager().getOrCreate(player);
        double balance = plugin.getVaultHook().getBalance(player);
        boolean changed = false;

        for (Quest quest : quests.values()) {
            if (!canWorkOnQuest(player, profile, quest)) {
                continue;
            }
            ensureQuestStarted(player, profile, quest);

            for (Quest.Objective objective : quest.getObjectives()) {
                if (profile.isObjectiveComplete(quest.getId(), objective.getId())) {
                    continue;
                }
                if (ObjectiveTypes.EARN_MONEY.equals(objective.getTypeId())) {
                    Double startBalance = profile.getQuestStartBalance(quest.getId());
                    if (startBalance == null) {
                        profile.setQuestStartBalance(quest.getId(), balance);
                        startBalance = balance;
                    }
                    int earned = (int) Math.floor(balance - startBalance);
                    profile.setObjectiveProgress(quest.getId(), objective.getId(), earned);
                    if (earned >= objective.getAmount()) {
                        completeObjective(player, profile, quest, objective);
                        changed = true;
                    }
                } else if (ObjectiveTypes.BALANCE_MIN.equals(objective.getTypeId())) {
                    if (balance >= objective.getAmount()) {
                        completeObjective(player, profile, quest, objective);
                        changed = true;
                    }
                }
            }
        }

        if (changed) {
            finalizeProgress(player, profile);
        }
    }

    private void processInstantObjectives(Player player, String typeId, Predicate<Quest.Objective> matcher) {
        PlayerProfile profile = plugin.getProfileManager().getOrCreate(player);
        boolean changed = false;

        for (Quest quest : quests.values()) {
            if (!canWorkOnQuest(player, profile, quest)) {
                continue;
            }
            ensureQuestStarted(player, profile, quest);

            for (Quest.Objective objective : quest.getObjectives()) {
                if (!typeId.equals(objective.getTypeId())) {
                    continue;
                }
                if (profile.isObjectiveComplete(quest.getId(), objective.getId())) {
                    continue;
                }
                if (matcher.test(objective)) {
                    completeObjective(player, profile, quest, objective);
                    changed = true;
                }
            }
        }

        if (changed) {
            finalizeProgress(player, profile);
        }
    }

    private void processCountObjectives(Player player, String typeId, Predicate<Quest.Objective> matcher, int increment) {
        if (increment <= 0) {
            return;
        }
        PlayerProfile profile = plugin.getProfileManager().getOrCreate(player);
        boolean changed = false;

        for (Quest quest : quests.values()) {
            if (!canWorkOnQuest(player, profile, quest)) {
                continue;
            }
            ensureQuestStarted(player, profile, quest);

            for (Quest.Objective objective : quest.getObjectives()) {
                if (!typeId.equals(objective.getTypeId())) {
                    continue;
                }
                if (!objective.isCountBased()) {
                    continue;
                }
                if (profile.isObjectiveComplete(quest.getId(), objective.getId())) {
                    continue;
                }
                if (!matcher.test(objective)) {
                    continue;
                }
                profile.addObjectiveProgress(quest.getId(), objective.getId(), increment);
                int progress = profile.getObjectiveProgress(quest.getId(), objective.getId());
                if (progress >= objective.getAmount()) {
                    completeObjective(player, profile, quest, objective);
                }
                changed = true;
            }
        }

        if (changed) {
            finalizeProgress(player, profile);
        }
    }

    private void completeObjective(Player player, PlayerProfile profile, Quest quest, Quest.Objective objective) {
        completeObjective(player, profile, quest, objective, true, true);
    }

    void completeObjective(Player player, PlayerProfile profile, Quest quest, Quest.Objective objective,
                           boolean grantRewards, boolean notify) {
        if (profile.isObjectiveComplete(quest.getId(), objective.getId())) {
            return;
        }
        profile.completeObjective(quest.getId(), objective.getId());
        maybeSetArchetype(profile, quest);
        if (notify) {
            if (plugin.getPluginConfig().isQuestNotificationsEnabled()) {
                plugin.getQuestFeedbackService().notifyObjectiveComplete(player, quest, objective);
            } else {
                plugin.getMessageUtil().send(player,
                        "<yellow>Objetivo concluído:</yellow> " + objective.getDescription());
            }
        }
    }

    void ensureQuestStartedForSync(PlayerProfile profile, Quest quest) {
        if (profile.hasQuestStarted(quest.getId())) {
            return;
        }
        profile.markQuestStarted(quest.getId());
        if (plugin.getVaultHook().isEnabled()) {
            Player player = plugin.getServer().getPlayer(profile.getUuid());
            if (player != null) {
                profile.setQuestStartBalance(quest.getId(), plugin.getVaultHook().getBalance(player));
            }
        }
    }

    private void ensureQuestStarted(Player player, PlayerProfile profile, Quest quest) {
        if (!profile.markQuestStarted(quest.getId())) {
            return;
        }
        onQuestStarted(player, profile, quest);
    }

    private void onQuestStarted(Player player, PlayerProfile profile, Quest quest) {
        if (quest.getLoreBook() != null && !quest.getLoreBook().isBlank()) {
            plugin.getInteractiveBooksHook().grantLoreBook(player, quest.getLoreBook());
        }
        plugin.getQuestBookService().grantOnQuestStart(player, quest);
        profile.addActiveQuest(quest.getId());
        if (profile.getTrackedQuestId() == null) {
            profile.setTrackedQuestId(quest.getId());
        }
        if (plugin.getVaultHook().isEnabled()) {
            profile.setQuestStartBalance(quest.getId(), plugin.getVaultHook().getBalance(player));
        }
        plugin.getQuestFeedbackService().refreshBossBar(player);
    }

    public QuestAcceptResult acceptQuest(Player player, String questId) {
        Quest quest = getQuest(questId);
        if (quest == null) {
            return QuestAcceptResult.NOT_FOUND;
        }
        PlayerProfile profile = plugin.getProfileManager().getOrCreate(player);
        return switch (startQuest(player, profile, quest)) {
            case STARTED -> QuestAcceptResult.SUCCESS;
            case ALREADY_ACTIVE -> QuestAcceptResult.ALREADY_STARTED;
            case ALREADY_COMPLETE -> QuestAcceptResult.ALREADY_COMPLETE;
            case NO_PERMISSION -> QuestAcceptResult.NO_PERMISSION;
            case REQUIREMENTS -> QuestAcceptResult.LOCKED;
            case LIMIT_REACHED -> QuestAcceptResult.MAX_ACTIVE;
        };
    }

    public boolean trackQuest(Player player, String questId) {
        Quest quest = getQuest(questId);
        if (quest == null) {
            return false;
        }
        PlayerProfile profile = plugin.getProfileManager().getOrCreate(player);
        return setTrackedQuest(player, profile, quest);
    }

    private void finalizeProgress(Player player, PlayerProfile profile) {
        checkQuestCompletions(player, profile, true, true);
        plugin.getSkillTreeManager().checkAutoUnlocks(player);
        plugin.getProfileManager().markDirty(player.getUniqueId());
        plugin.getQuestFeedbackService().refreshBossBar(player);
    }

    private void checkQuestCompletions(Player player, PlayerProfile profile) {
        checkQuestCompletions(player, profile, true, true);
    }

    private void checkQuestCompletions(Player player, PlayerProfile profile, boolean grantRewards, boolean notify) {
        for (Quest quest : quests.values()) {
            completeQuest(player, profile, quest, grantRewards, notify);
        }
    }

    int completeQuest(Player player, PlayerProfile profile, Quest quest, boolean grantRewards, boolean notify) {
        if (!isQuestComplete(profile, quest) || profile.isQuestComplete(quest.getId())) {
            return 0;
        }
        profile.markQuestComplete(quest.getId());
        if (quest.isScheduled()) {
            profile.setQuestCompletedAt(quest.getId(), System.currentTimeMillis());
        }
        if (notify) {
            if (plugin.getPluginConfig().isQuestNotificationsEnabled()) {
                plugin.getQuestFeedbackService().notifyQuestComplete(player, quest);
            } else {
                plugin.getMessageUtil().send(player, "<green>Quest concluída:</green> " + quest.getName());
            }
        }
        if (grantRewards) {
            rewardExecutor.grantRewards(player, quest);
            if (quest.getUnlocksPerk() != null && !quest.getUnlocksPerk().isBlank()) {
                plugin.getSkillTreeManager().tryUnlock(player, quest.getUnlocksPerk());
            }
        }
        return 1;
    }

    private void maybeSetArchetype(PlayerProfile profile, Quest quest) {
        if (quest.getArchetype() != null && !quest.getArchetype().isBlank()) {
            if (profile.getArchetype() == null || profile.getArchetype().isBlank()) {
                profile.setArchetype(quest.getArchetype());
            }
        }
        profile.addActiveQuest(quest.getId());
    }

    public boolean canWorkOnQuest(Player player, PlayerProfile profile, Quest quest) {
        if (profile.isQuestComplete(quest.getId())) {
            return false;
        }
        if (!plugin.getLuckPermsHook().hasQuestPermission(player, quest.getId())) {
            return false;
        }
        return meetsRequirements(profile, quest);
    }

    public boolean meetsRequirements(PlayerProfile profile, Quest quest) {
        for (String requiredId : quest.getRequiredQuestIds()) {
            if (!profile.isQuestComplete(requiredId)) {
                return false;
            }
        }
        return true;
    }

    public boolean isQuestComplete(PlayerProfile profile, Quest quest) {
        for (Quest.Objective objective : quest.getObjectives()) {
            if (!profile.isObjectiveComplete(quest.getId(), objective.getId())) {
                return false;
            }
        }
        return !quest.getObjectives().isEmpty();
    }

    public QuestStatus getQuestStatus(PlayerProfile profile, Quest quest) {
        if (profile.isQuestComplete(quest.getId())) {
            return QuestStatus.COMPLETED;
        }
        if (!meetsRequirements(profile, quest)) {
            return QuestStatus.LOCKED;
        }
        boolean anyProgress = quest.getObjectives().stream()
                .anyMatch(objective -> profile.isObjectiveComplete(quest.getId(), objective.getId())
                        || profile.getObjectiveProgress(quest.getId(), objective.getId()) > 0);
        if (anyProgress || profile.getActiveQuestIds().contains(quest.getId())) {
            return QuestStatus.IN_PROGRESS;
        }
        return QuestStatus.NOT_STARTED;
    }

    public QuestStatus getQuestStatus(Player player, PlayerProfile profile, Quest quest) {
        if (profile.isQuestComplete(quest.getId())) {
            return QuestStatus.COMPLETED;
        }
        if (!meetsRequirements(profile, quest)) {
            return QuestStatus.LOCKED;
        }
        if (!plugin.getLuckPermsHook().hasQuestPermission(player, quest.getId())) {
            return QuestStatus.LOCKED;
        }
        return getQuestStatus(profile, quest);
    }

    public QuestProgress getQuestProgress(PlayerProfile profile, Quest quest) {
        int total = quest.getObjectives().size();
        int completed = 0;
        for (Quest.Objective objective : quest.getObjectives()) {
            if (profile.isObjectiveComplete(quest.getId(), objective.getId())) {
                completed++;
            }
        }
        return new QuestProgress(completed, total);
    }

    public String formatQuestProgress(PlayerProfile profile, Quest quest) {
        if (quest == null) {
            return "Nenhuma";
        }
        QuestProgress progress = getQuestProgress(profile, quest);
        if (progress.total() == 0) {
            return quest.getName();
        }
        return quest.getName() + ": " + progress.completed() + "/" + progress.total();
    }

    public String formatPrimaryQuestProgress(PlayerProfile profile) {
        Quest quest = findPrimaryActiveQuest(profile);
        return formatQuestProgress(profile, quest);
    }

    public Quest findPrimaryActiveQuest(PlayerProfile profile) {
        String trackedId = profile.getTrackedQuestId();
        if (trackedId != null) {
            Quest tracked = quests.get(trackedId);
            if (tracked != null && !profile.isQuestComplete(trackedId)) {
                return tracked;
            }
        }
        for (String questId : profile.getActiveQuestIds()) {
            Quest quest = quests.get(questId);
            if (quest != null && !profile.isQuestComplete(questId)) {
                return quest;
            }
        }
        for (Quest quest : quests.values()) {
            if (getQuestStatus(profile, quest) == QuestStatus.IN_PROGRESS) {
                return quest;
            }
        }
        return null;
    }

    public int countActiveInProgressQuests(PlayerProfile profile) {
        int count = 0;
        for (Quest quest : quests.values()) {
            if (getQuestStatus(profile, quest) == QuestStatus.IN_PROGRESS) {
                count++;
            }
        }
        return count;
    }

    /**
     * Manually starts a NOT_STARTED quest for the player from the journal GUI.
     * Respects requirements, permission and the quests.max-active limit.
     *
     * @return a result describing what happened, for GUI feedback.
     */
    public StartResult startQuest(Player player, PlayerProfile profile, Quest quest) {
        if (profile.isQuestComplete(quest.getId())) {
            return StartResult.ALREADY_COMPLETE;
        }
        if (!plugin.getLuckPermsHook().hasQuestPermission(player, quest.getId())) {
            return StartResult.NO_PERMISSION;
        }
        if (!meetsRequirements(profile, quest)) {
            return StartResult.REQUIREMENTS;
        }
        QuestStatus status = getQuestStatus(profile, quest);
        if (status == QuestStatus.IN_PROGRESS) {
            return StartResult.ALREADY_ACTIVE;
        }
        int limit = plugin.getPluginConfig().getMaxActiveQuests();
        if (limit > 0 && countActiveInProgressQuests(profile) >= limit) {
            return StartResult.LIMIT_REACHED;
        }
        ensureQuestStarted(player, profile, quest);
        profile.addActiveQuest(quest.getId());
        maybeSetArchetype(profile, quest);
        profile.setTrackedQuestId(quest.getId());
        plugin.getProfileManager().markDirty(player.getUniqueId());
        plugin.getQuestFeedbackService().refreshBossBar(player);
        return StartResult.STARTED;
    }

    public boolean setTrackedQuest(Player player, PlayerProfile profile, Quest quest) {
        if (profile.isQuestComplete(quest.getId())
                || getQuestStatus(player, profile, quest) != QuestStatus.IN_PROGRESS) {
            return false;
        }
        profile.setTrackedQuestId(quest.getId());
        plugin.getProfileManager().markDirty(player.getUniqueId());
        plugin.getQuestFeedbackService().refreshBossBar(player);
        return true;
    }

    public boolean abandonQuest(Player player, PlayerProfile profile, Quest quest) {
        if (!plugin.getPluginConfig().isAllowAbandon()) {
            return false;
        }
        if (profile.isQuestComplete(quest.getId())) {
            return false;
        }
        profile.clearQuestState(quest.getId());
        plugin.getProfileManager().markDirty(player.getUniqueId());
        return true;
    }

    public enum StartResult {
        STARTED,
        ALREADY_ACTIVE,
        ALREADY_COMPLETE,
        NO_PERMISSION,
        REQUIREMENTS,
        LIMIT_REACHED
    }

    public record QuestProgress(int completed, int total) {
    }

    public enum QuestStatus {
        LOCKED("Bloqueada"),
        NOT_STARTED("Não iniciada"),
        IN_PROGRESS("Em progresso"),
        COMPLETED("Concluída");

        private final String display;

        QuestStatus(String display) {
            this.display = display;
        }

        public String getDisplay() {
            return display;
        }
    }
}
