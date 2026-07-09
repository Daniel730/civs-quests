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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Random;

public final class QuestManager {

    public static final Set<String> PATH_QUEST_IDS = Set.of(
            "warrior_path", "merchant_path", "builder_path");

    private final RpgServerPlugin plugin;
    private final ObjectiveTypeRegistry objectiveTypeRegistry;
    private final RewardExecutor rewardExecutor;
    private final QuestProgressSync progressSync;
    private final Map<String, Quest> quests = new LinkedHashMap<>();
    private final Map<String, List<Quest>> questsByObjectiveType = new LinkedHashMap<>();

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
        questsByObjectiveType.clear();
        File questsFolder = new File(plugin.getDataFolder(), "quests");
        if (!questsFolder.exists()) {
            questsFolder.mkdirs();
            plugin.saveResource("quests/warrior_path.yml", false);
            plugin.saveResource("quests/builder_path.yml", false);
            plugin.saveResource("quests/merchant_path.yml", false);
            plugin.saveResource("quests/welcome.yml", false);
            plugin.saveResource("quests/first_steps.yml", false);
            plugin.saveResource("quests/hunt_frost_watchtower.yml", false);
            plugin.saveResource("quests/hunt_quarry_depths.yml", false);
            plugin.saveResource("quests/hunt_sunken_caravan.yml", false);
            plugin.saveResource("quests/daily_scout.yml", false);
            plugin.saveResource("quests/daily_boar_hunt.yml", false);
            plugin.saveResource("quests/daily_duel.yml", false);
            plugin.saveResource("quests/daily_pillager.yml", false);
            plugin.saveResource("quests/weekly_explorer.yml", false);
            plugin.saveResource("quests/weekly_deep_delve.yml", false);
            plugin.saveResource("quests/weekly_guild_bounty.yml", false);
            plugin.saveResource("quests/sprint2_civs_skills.yml", false);
            plugin.saveResource("quests/sprint2_auction.yml", false);
            plugin.saveResource("quests/sprint3_daily.yml", false);
            plugin.saveResource("quests/sprint3_boss.yml", false);
            plugin.saveResource("quests/warrior_champion.yml", false);
            plugin.saveResource("quests/daily_quarry.yml", false);
            plugin.saveResource("quests/weekly_warrior.yml", false);
            plugin.saveResource("quests/weekly_merchant.yml", false);
            plugin.saveResource("quests/weekly_builder.yml", false);
            plugin.saveResource("quests/sprint2_spells.yml", false);
            plugin.saveResource("quests/mercador_fortuna.yml", false);
            plugin.saveResource("quests/mercador_mestre.yml", false);
            plugin.saveResource("quests/construtor_armazem.yml", false);
            plugin.saveResource("quests/construtor_mestre.yml", false);
            plugin.saveResource("quests/daily_mercado.yml", false);
            plugin.saveResource("quests/daily_miner.yml", false);
            plugin.saveResource("quests/daily_vendas.yml", false);
            plugin.saveResource("quests/daily_farm.yml", false);
            plugin.saveResource("quests/weekly_boss_hunter.yml", false);
            plugin.saveResource("quests/warrior_siege_prep.yml", false);
            plugin.saveResource("quests/merchant_shop_front.yml", false);
            plugin.saveResource("quests/builder_town_hall.yml", false);
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
        rebuildObjectiveTypeIndex();
        plugin.getLogger().info("Carregadas " + quests.size() + " quests.");
    }

    private void rebuildObjectiveTypeIndex() {
        questsByObjectiveType.clear();
        for (Quest quest : quests.values()) {
            for (Quest.Objective objective : quest.getObjectives()) {
                questsByObjectiveType
                        .computeIfAbsent(objective.getTypeId(), key -> new ArrayList<>())
                        .add(quest);
            }
        }
    }

    public List<Quest> getQuestsByObjectiveType(String typeId) {
        List<Quest> list = questsByObjectiveType.get(typeId);
        return list == null ? List.of() : Collections.unmodifiableList(list);
    }

    private Quest parseQuest(YamlConfiguration config) {
        String id = config.getString("id");
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Campo 'id' obrigatório");
        }
        String name = config.getString("name", id);
        String archetype = config.getString("archetype", "");
        String description = config.getString("description", "");
        String unlocksPerk = config.getString("unlocks-perk");
        List<String> unlocksPerkChoice = config.getStringList("unlocks-perk-choice");
        List<String> lore = config.getStringList("lore");
        int tier = config.getInt("tier", 0);
        String category = config.getString("category", "");
        QuestSchedule schedule = QuestSchedule.fromYaml(config.getString("schedule"));
        List<String> requiredQuestIds = config.getStringList("requires");
        List<Map<?, ?>> rawObjectives = config.getMapList("objectives");
        List<Quest.Objective> objectives = new ArrayList<>();
        for (Map<?, ?> raw : rawObjectives) {
            objectives.add(objectiveTypeRegistry.parseObjective(raw));
        }
        RewardDefinition rewards = RewardDefinition.fromConfig(config.getConfigurationSection("rewards"));
        return new Quest(id, name, archetype, description, lore, tier, category,
                unlocksPerk, unlocksPerkChoice, schedule, requiredQuestIds, objectives, rewards);
    }

    public void resetExpiredScheduledQuests(Player player) {
        PlayerProfile profile = plugin.getProfileManager().getOrCreate(player);
        ZoneId zone = resolveResetZone();
        boolean changed = false;
        int resetCount = 0;

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
                resetCount++;
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

    public static boolean isPathQuest(Quest quest) {
        return quest != null && PATH_QUEST_IDS.contains(quest.getId());
    }

    public List<Quest> getPathQuests() {
        List<Quest> pathQuests = new ArrayList<>();
        for (String id : PATH_QUEST_IDS) {
            Quest quest = quests.get(id);
            if (quest != null) {
                pathQuests.add(quest);
            }
        }
        pathQuests.sort(Comparator.comparing(Quest::getName, String.CASE_INSENSITIVE_ORDER));
        return pathQuests;
    }

    public List<Quest> getArchetypeStoryQuests(Player player, PlayerProfile profile, String archetype) {
        if (archetype == null || archetype.isBlank()) {
            return List.of();
        }
        List<Quest> result = new ArrayList<>();
        for (Quest quest : quests.values()) {
            if (quest.getSchedule() != QuestSchedule.NONE) {
                continue;
            }
            if (!archetype.equalsIgnoreCase(quest.getArchetype())) {
                continue;
            }
            result.add(quest);
        }
        return sortForJournal(player, profile, result);
    }

    public List<Quest> getPathQuests(Player player, PlayerProfile profile) {
        List<Quest> available = new ArrayList<>();
        for (Quest path : getPathQuests()) {
            if (!isConflictingPath(profile, path)) {
                available.add(path);
            }
        }
        return sortForJournal(player, profile, available);
    }

    public List<Quest> getScheduledQuests(Player player, PlayerProfile profile, QuestSchedule schedule) {
        List<Quest> result = new ArrayList<>();
        Set<String> rotationIds = getRotationQuestIds(schedule);
        for (Quest quest : quests.values()) {
            if (quest.getSchedule() != schedule || !matchesPlayerArchetype(profile, quest)) {
                continue;
            }
            if (!rotationIds.isEmpty() && !rotationIds.contains(quest.getId())) {
                continue;
            }
            result.add(quest);
        }
        return sortForJournal(player, profile, result);
    }

    public Set<String> getRotationQuestIds(QuestSchedule schedule) {
        List<String> pool = switch (schedule) {
            case DAILY -> plugin.getPluginConfig().getDailyRotationPool();
            case WEEKLY -> plugin.getPluginConfig().getWeeklyRotationPool();
            case NONE -> List.of();
        };
        int count = switch (schedule) {
            case DAILY -> plugin.getPluginConfig().getDailyRotationCount();
            case WEEKLY -> plugin.getPluginConfig().getWeeklyRotationCount();
            case NONE -> 0;
        };
        if (pool.isEmpty() || count <= 0) {
            return Set.of();
        }
        ZoneId zone = resolveResetZone();
        String periodKey = schedule == QuestSchedule.DAILY
                ? currentPeriodDay(zone)
                : currentPeriodWeek(zone);
        List<String> shuffled = new ArrayList<>(pool);
        Collections.shuffle(shuffled, new Random(periodKey.hashCode()));
        return Set.copyOf(shuffled.subList(0, Math.min(count, shuffled.size())));
    }

    public String currentPeriodWeek(ZoneId zone) {
        var now = java.time.LocalDate.now(zone);
        return now.get(java.time.temporal.IsoFields.WEEK_BASED_YEAR) + "-W"
                + now.get(java.time.temporal.IsoFields.WEEK_OF_WEEK_BASED_YEAR);
    }

    public boolean isInRotation(Quest quest) {
        if (quest == null || !quest.isScheduled()) {
            return true;
        }
        Set<String> rotation = getRotationQuestIds(quest.getSchedule());
        return rotation.isEmpty() || rotation.contains(quest.getId());
    }

    public List<Quest> getMiscQuests(Player player, PlayerProfile profile, Set<String> excludeIds) {
        List<Quest> result = new ArrayList<>();
        for (Quest quest : quests.values()) {
            if (excludeIds.contains(quest.getId())) {
                continue;
            }
            if (quest.getSchedule() != QuestSchedule.NONE) {
                continue;
            }
            if (!matchesPlayerArchetype(profile, quest)) {
                continue;
            }
            if (isPathQuest(quest) && (profile.getArchetype() == null || profile.getArchetype().isBlank())) {
                continue;
            }
            result.add(quest);
        }
        result.sort(Comparator
                .comparingInt((Quest quest) -> statusSortOrder(getQuestStatus(player, profile, quest)))
                .thenComparing(Quest::getName, String.CASE_INSENSITIVE_ORDER));
        return result;
    }

    public List<Quest> sortForJournal(Player player, PlayerProfile profile, List<Quest> questList) {
        List<Quest> sorted = new ArrayList<>(questList);
        sorted.sort(Comparator
                .comparingInt((Quest quest) -> statusSortOrder(getQuestStatus(player, profile, quest)))
                .thenComparing(Quest::getName, String.CASE_INSENSITIVE_ORDER));
        return sorted;
    }

    private static int statusSortOrder(QuestStatus status) {
        return switch (status) {
            case IN_PROGRESS -> 0;
            case NOT_STARTED -> 1;
            case LOCKED -> 2;
            case COMPLETED -> 3;
        };
    }

    public List<String> formatMissingPrerequisiteNames(PlayerProfile profile, Quest quest) {
        List<String> names = new ArrayList<>();
        for (String requiredId : quest.getRequiredQuestIds()) {
            if (!profile.isQuestComplete(requiredId)) {
                Quest required = quests.get(requiredId);
                names.add(required != null ? required.getName() : requiredId);
            }
        }
        return names;
    }

    public Optional<Quest> findNextAvailableQuest(Player player, PlayerProfile profile) {
        String archetype = profile.getArchetype();
        if (archetype == null || archetype.isBlank()) {
            return Optional.empty();
        }
        for (Quest quest : getArchetypeStoryQuests(player, profile, archetype)) {
            QuestStatus status = getQuestStatus(player, profile, quest);
            if (status == QuestStatus.NOT_STARTED || status == QuestStatus.IN_PROGRESS) {
                return Optional.of(quest);
            }
        }
        return Optional.empty();
    }

    public String formatNextQuestName(Player player, PlayerProfile profile) {
        return findNextAvailableQuest(player, profile)
                .map(Quest::getName)
                .orElse("Nenhuma");
    }

    public Optional<Quest> findNextQuestInChain(Player player, PlayerProfile profile, Quest quest) {
        if (!profile.isQuestComplete(quest.getId())) {
            return Optional.empty();
        }
        List<Quest> followUps = new ArrayList<>();
        for (Quest candidate : quests.values()) {
            if (profile.isQuestComplete(candidate.getId())) {
                continue;
            }
            if (!candidate.getRequiredQuestIds().contains(quest.getId())) {
                continue;
            }
            if (!meetsRequirements(profile, candidate)) {
                continue;
            }
            if (!hasQuestUnlock(player, profile, candidate)) {
                continue;
            }
            QuestStatus status = getQuestStatus(player, profile, candidate);
            if (status == QuestStatus.NOT_STARTED || status == QuestStatus.IN_PROGRESS) {
                followUps.add(candidate);
            }
        }
        followUps.sort(Comparator.comparing(Quest::getName, String.CASE_INSENSITIVE_ORDER));
        return followUps.isEmpty() ? Optional.empty() : Optional.of(followUps.getFirst());
    }

    public boolean hasAvailableDailyQuest(Player player, PlayerProfile profile) {
        for (Quest quest : quests.values()) {
            if (quest.getSchedule() != QuestSchedule.DAILY) {
                continue;
            }
            if (!matchesPlayerArchetype(profile, quest)) {
                continue;
            }
            QuestStatus status = getQuestStatus(player, profile, quest);
            if (status == QuestStatus.COMPLETED || status == QuestStatus.LOCKED) {
                continue;
            }
            return true;
        }
        return false;
    }

    public static boolean isNeutralArchetype(String archetype) {
        return archetype != null && "neutral".equalsIgnoreCase(archetype);
    }

    public boolean matchesPlayerArchetype(PlayerProfile profile, Quest quest) {
        String questArchetype = quest.getArchetype();
        String playerArchetype = profile.getArchetype();

        if (questArchetype == null || questArchetype.isBlank()) {
            return playerArchetype == null || playerArchetype.isBlank();
        }
        if (isNeutralArchetype(questArchetype)) {
            return true;
        }
        if (playerArchetype == null || playerArchetype.isBlank()) {
            return isPathQuest(quest);
        }
        return questArchetype.equalsIgnoreCase(playerArchetype);
    }

    public boolean isConflictingPath(PlayerProfile profile, Quest quest) {
        if (!isPathQuest(quest)) {
            if (profile.getArchetype() == null || profile.getArchetype().isBlank()) {
                return false;
            }
            String questArchetype = quest.getArchetype();
            if (questArchetype == null || questArchetype.isBlank() || isNeutralArchetype(questArchetype)) {
                return false;
            }
            return !questArchetype.equalsIgnoreCase(profile.getArchetype());
        }
        for (String pathId : PATH_QUEST_IDS) {
            if (pathId.equals(quest.getId())) {
                continue;
            }
            if (profile.hasQuestStarted(pathId) || profile.isQuestComplete(pathId)) {
                return true;
            }
        }
        String playerArchetype = profile.getArchetype();
        if (playerArchetype != null && !playerArchetype.isBlank()
                && quest.getArchetype() != null
                && !quest.getArchetype().equalsIgnoreCase(playerArchetype)) {
            return true;
        }
        return false;
    }

    public String currentPeriodDay(ZoneId zone) {
        return java.time.LocalDate.now(zone).toString();
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

    public void handleJoinTown(Player player, String townKey) {
        if (!plugin.getCivsHook().isEnabled()) {
            return;
        }
        String normalizedTown = townKey == null ? null : townKey.toLowerCase(Locale.ROOT);
        processInstantObjectives(player, ObjectiveTypes.JOIN_TOWN, objective -> {
            if (objective.getRegion() == null || objective.getRegion().isBlank()) {
                return true;
            }
            return normalizedTown != null
                    && objective.getRegion().equalsIgnoreCase(normalizedTown);
        });
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
                        || objective.getMob().equalsIgnoreCase(normalizedMob), 1);
    }

    public void handleCustomMobKill(Player player, String mobId) {
        if (!plugin.getCivsHook().isEnabled() || mobId == null || mobId.isBlank()) {
            return;
        }
        String normalizedMob = mobId.toLowerCase(Locale.ROOT);
        processCountObjectives(player, ObjectiveTypes.CUSTOM_MOB_KILL, objective ->
                objective.getMob() != null
                        && objective.getMob().equalsIgnoreCase(normalizedMob), 1);
    }

    public void handleDiscoverPoi(Player player, String poiId) {
        if (poiId == null || poiId.isBlank()) {
            return;
        }
        String normalized = poiId.toLowerCase(Locale.ROOT);
        processInstantObjectives(player, ObjectiveTypes.DISCOVER_POI, objective ->
                objective.getRegion() != null
                        && objective.getRegion().equalsIgnoreCase(normalized));
    }

    public void handleDiscoverBiome(Player player, String biomeId) {
        if (biomeId == null || biomeId.isBlank()) {
            return;
        }
        String normalized = biomeId.toLowerCase(Locale.ROOT);
        processInstantObjectives(player, ObjectiveTypes.DISCOVER_BIOME, objective ->
                objective.getBlock() != null
                        && objective.getBlock().equalsIgnoreCase(normalized));
    }

    public void handleEnterCombat(Player player) {
        if (!plugin.getCivsHook().isEnabled()) {
            return;
        }
        processCountObjectives(player, ObjectiveTypes.ENTER_COMBAT, objective -> true, 1);
    }

    public void handleOpenHub(Player player) {
        PlayerProfile profile = plugin.getProfileManager().getOrCreate(player);
        if (!profile.isHubOpened()) {
            profile.setHubOpened(true);
            plugin.getProfileManager().markDirty(player.getUniqueId());
        }
        processInstantObjectives(player, ObjectiveTypes.OPEN_HUB, objective -> true);
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

    /** Backfills join_town from Civs roster (founders, members, and prior invites). */
    public void checkTownMembership(Player player) {
        if (!plugin.getCivsHook().isEnabled()) {
            return;
        }
        processInstantObjectives(player, ObjectiveTypes.JOIN_TOWN, objective ->
                plugin.getCivsHook().isTownMember(player, objective.getRegion()));
    }

    /** Backfills build_region from Civs accomplishments and owned regions. */
    public void checkBuiltRegions(Player player) {
        if (!plugin.getCivsHook().isEnabled()) {
            return;
        }
        processInstantObjectives(player, ObjectiveTypes.BUILD_REGION, objective ->
                objective.getRegion() != null
                        && plugin.getCivsHook().hasBuiltRegion(player, objective.getRegion()));
    }

    /** Silent Civs backfill for join, builds, and internal skill gates. */
    public void backfillCivsState(Player player) {
        checkTownMembership(player);
        checkBuiltRegions(player);
        checkCivsSkillLevels(player);
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
                } else if (quest.getId().equals(profile.getTrackedQuestId())) {
                    maybeNotifyPartialProgress(player, quest, objective, progress);
                }
                changed = true;
            }
        }

        if (changed) {
            finalizeProgress(player, profile);
        }
    }

    private void maybeNotifyPartialProgress(Player player, Quest quest, Quest.Objective objective, int current) {
        int interval = plugin.getPluginConfig().getQuestProgressNotifyInterval();
        if (interval <= 0 || !plugin.getPluginConfig().isQuestNotificationsEnabled()) {
            return;
        }
        if (current % interval != 0) {
            return;
        }
        plugin.getQuestFeedbackService().notifyObjectiveProgress(
                player, quest, objective, current, objective.getAmount());
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
        if (profile.hasQuestStarted(quest.getId()) || profile.getActiveQuestIds().contains(quest.getId())) {
            return;
        }
        int limit = plugin.getPluginConfig().getMaxActiveQuests();
        if (limit > 0 && countActiveInProgressQuests(player, profile) >= limit) {
            return;
        }
        if (!profile.markQuestStarted(quest.getId())) {
            return;
        }
        onQuestStarted(player, profile, quest);
    }

    private void onQuestStarted(Player player, PlayerProfile profile, Quest quest) {
        profile.addActiveQuest(quest.getId());
        if (profile.getTrackedQuestId() == null) {
            profile.setTrackedQuestId(quest.getId());
        }
        if (plugin.getVaultHook().isEnabled()) {
            profile.setQuestStartBalance(quest.getId(), plugin.getVaultHook().getBalance(player));
        }
        plugin.getHuntSpawnService().spawnOnQuestAccept(player, quest);
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
            case ARCHETYPE_LOCKED -> QuestAcceptResult.ARCHETYPE_LOCKED;
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
        plugin.getPlayerHubService().refreshIfOpen(player);
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
            grantQuestCompletionPermission(player, quest.getId());
            unlockFollowUpQuestPermissions(player, quest.getId());
            plugin.getPathTraitService().onPathQuestComplete(player, quest);
            if (!quest.getUnlocksPerkChoice().isEmpty()) {
                plugin.getMessageUtil().send(player,
                        "<light_purple>Escolha um perk capstone em</light_purple> <yellow>/rpg tree</yellow>");
            }
        }
        return 1;
    }

    /** Always grants {@code rpg.quest.<id>} so completion is recorded in LuckPerms. */
    private void grantQuestCompletionPermission(Player player, String questId) {
        if (!plugin.getLuckPermsHook().isEnabled()) {
            return;
        }
        plugin.getLuckPermsHook().grantPermission(
                player, plugin.getLuckPermsHook().questPermission(questId));
    }

    /**
     * Grants {@code rpg.quest.<id>} for quests that list the completed quest in {@code requires},
     * so follow-up chains unlock without duplicating permission nodes on every parent reward.
     */
    private void unlockFollowUpQuestPermissions(Player player, String completedQuestId) {
        if (!plugin.getLuckPermsHook().isEnabled()) {
            return;
        }
        for (Quest followUp : quests.values()) {
            if (followUp.getRequiredQuestIds().contains(completedQuestId)) {
                plugin.getLuckPermsHook().grantPermission(
                        player, plugin.getLuckPermsHook().questPermission(followUp.getId()));
            }
        }
    }

    private void maybeSetArchetype(PlayerProfile profile, Quest quest) {
        if (quest.getArchetype() != null && !quest.getArchetype().isBlank()
                && !isNeutralArchetype(quest.getArchetype())) {
            if (isPathQuest(quest)) {
                profile.setArchetype(quest.getArchetype());
            } else if (profile.getArchetype() == null || profile.getArchetype().isBlank()) {
                profile.setArchetype(quest.getArchetype());
            }
        }
        profile.addActiveQuest(quest.getId());
    }

    public boolean canWorkOnQuest(Player player, PlayerProfile profile, Quest quest) {
        if (profile.isQuestComplete(quest.getId())) {
            return false;
        }
        if (isConflictingPath(profile, quest)) {
            return false;
        }
        if (!matchesPlayerArchetype(profile, quest)) {
            return false;
        }
        if (!hasQuestUnlock(player, profile, quest)) {
            return false;
        }
        return meetsRequirements(profile, quest);
    }

    private boolean hasQuestUnlock(Player player, PlayerProfile profile, Quest quest) {
        if (!plugin.getLuckPermsHook().isEnabled()) {
            return true;
        }
        if (quest.getRequiredQuestIds().isEmpty() || isPathQuest(quest)) {
            return true;
        }
        if (plugin.getLuckPermsHook().hasQuestPermission(player, quest.getId())) {
            return true;
        }
        return quest.getRequiredQuestIds().stream().allMatch(profile::isQuestComplete);
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
        if (isConflictingPath(profile, quest)) {
            return QuestStatus.LOCKED;
        }
        if (!meetsRequirements(profile, quest)) {
            return QuestStatus.LOCKED;
        }
        if (!hasQuestUnlock(player, profile, quest)) {
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

    public Quest findTrackedQuest(PlayerProfile profile) {
        String trackedId = profile.getTrackedQuestId();
        if (trackedId == null) {
            return null;
        }
        Quest tracked = quests.get(trackedId);
        if (tracked == null || profile.isQuestComplete(trackedId)) {
            return null;
        }
        return tracked;
    }

    public String formatTrackedQuestName(PlayerProfile profile) {
        Quest quest = findTrackedQuest(profile);
        return quest == null ? "Nenhuma" : quest.getName();
    }

    public String formatTrackedQuestProgress(PlayerProfile profile) {
        Quest quest = findTrackedQuest(profile);
        if (quest == null) {
            return "";
        }
        Optional<Quest.Objective> current = findCurrentObjective(profile, quest);
        if (current.isEmpty()) {
            return formatQuestProgress(profile, quest);
        }
        Quest.Objective objective = current.get();
        if (objective.isCountBased()) {
            int currentCount = profile.getObjectiveProgress(quest.getId(), objective.getId());
            return objective.getDescription() + " (" + currentCount + "/" + objective.getAmount() + ")";
        }
        return objective.getDescription();
    }

    public Optional<Quest.Objective> findCurrentObjective(PlayerProfile profile, Quest quest) {
        for (Quest.Objective objective : quest.getObjectives()) {
            if (!profile.isObjectiveComplete(quest.getId(), objective.getId())) {
                return Optional.of(objective);
            }
        }
        return Optional.empty();
    }

    /**
     * Returns the display name of the first incomplete prerequisite quest, if any.
     */
    public Optional<String> findFirstMissingPrerequisite(PlayerProfile profile, Quest quest) {
        for (String requiredId : quest.getRequiredQuestIds()) {
            if (!profile.isQuestComplete(requiredId)) {
                Quest required = quests.get(requiredId);
                return Optional.of(required != null ? required.getName() : requiredId);
            }
        }
        return Optional.empty();
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

    public int countActiveInProgressQuests(Player player, PlayerProfile profile) {
        int count = 0;
        for (Quest quest : quests.values()) {
            if (getQuestStatus(player, profile, quest) == QuestStatus.IN_PROGRESS) {
                count++;
            }
        }
        return count;
    }

    public SanitizeResult sanitizeProfile(Player player, PlayerProfile profile) {
        int strippedInvalid = 0;
        int strippedCompletedActive = 0;
        int demotedExcess = 0;

        Set<String> questIds = new LinkedHashSet<>();
        questIds.addAll(profile.getActiveQuestIds());
        questIds.addAll(profile.getStartedQuestIds());
        for (String key : profile.getCompletedObjectiveKeys()) {
            int colon = key.indexOf(':');
            if (colon > 0) {
                questIds.add(key.substring(0, colon));
            }
        }
        for (String key : profile.getObjectiveProgressSnapshot().keySet()) {
            int colon = key.indexOf(':');
            if (colon > 0) {
                questIds.add(key.substring(0, colon));
            }
        }

        for (String questId : questIds) {
            Quest quest = quests.get(questId);
            if (quest == null) {
                profile.clearQuestState(questId);
                strippedInvalid++;
                continue;
            }
            if (profile.isQuestComplete(questId)) {
                if (profile.getActiveQuestIds().contains(questId)
                        || profile.getStartedQuestIds().contains(questId)) {
                    profile.stripInProgressState(questId);
                    strippedCompletedActive++;
                }
                continue;
            }
            if (!canWorkOnQuest(player, profile, quest)) {
                profile.stripInProgressState(questId);
                strippedInvalid++;
            }
        }

        int limit = plugin.getPluginConfig().getMaxActiveQuests();
        if (limit > 0) {
            List<String> inProgressActive = new ArrayList<>();
            for (String questId : profile.getActiveQuestIds()) {
                Quest quest = quests.get(questId);
                if (quest != null
                        && getQuestStatus(player, profile, quest) == QuestStatus.IN_PROGRESS) {
                    inProgressActive.add(questId);
                }
            }
            if (inProgressActive.size() > limit) {
                String trackedId = profile.getTrackedQuestId();
                inProgressActive.sort((a, b) -> {
                    if (a.equals(trackedId)) {
                        return -1;
                    }
                    if (b.equals(trackedId)) {
                        return 1;
                    }
                    Quest qa = quests.get(a);
                    Quest qb = quests.get(b);
                    int pa = qa == null ? 0 : getQuestProgress(profile, qa).completed();
                    int pb = qb == null ? 0 : getQuestProgress(profile, qb).completed();
                    return Integer.compare(pb, pa);
                });
                for (int i = limit; i < inProgressActive.size(); i++) {
                    profile.removeActiveQuest(inProgressActive.get(i));
                    demotedExcess++;
                }
            }
        }

        String trackedId = profile.getTrackedQuestId();
        if (trackedId != null) {
            Quest tracked = quests.get(trackedId);
            if (tracked == null
                    || profile.isQuestComplete(trackedId)
                    || getQuestStatus(player, profile, tracked) != QuestStatus.IN_PROGRESS) {
                profile.setTrackedQuestId(null);
            }
        }

        if (strippedInvalid > 0 || strippedCompletedActive > 0 || demotedExcess > 0) {
            plugin.getProfileManager().markDirty(player.getUniqueId());
        }
        return new SanitizeResult(strippedInvalid, strippedCompletedActive, demotedExcess);
    }

    public record SanitizeResult(int strippedInvalid, int strippedCompletedActive, int demotedExcess) {
    }

    public void ensureProfileSanitized(Player player) {
        sanitizeProfile(player, plugin.getProfileManager().getOrCreate(player));
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
        if (isConflictingPath(profile, quest)) {
            return StartResult.ARCHETYPE_LOCKED;
        }
        if (!hasQuestUnlock(player, profile, quest)) {
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
        if (limit > 0 && countActiveInProgressQuests(player, profile) >= limit) {
            return StartResult.LIMIT_REACHED;
        }
        ensureQuestStarted(player, profile, quest);
        profile.addActiveQuest(quest.getId());
        maybeSetArchetype(profile, quest);
        profile.setTrackedQuestId(quest.getId());
        progressSync.sync(player, true, true);
        plugin.getProfileManager().markDirty(player.getUniqueId());
        plugin.getQuestFeedbackService().refreshBossBar(player);
        plugin.getPlayerHubService().refreshIfOpen(player);
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
        plugin.getPlayerHubService().refreshIfOpen(player);
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
        plugin.getQuestFeedbackService().refreshTrackedHud(player);
        return true;
    }

    public enum StartResult {
        STARTED,
        ALREADY_ACTIVE,
        ALREADY_COMPLETE,
        NO_PERMISSION,
        REQUIREMENTS,
        LIMIT_REACHED,
        ARCHETYPE_LOCKED
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
