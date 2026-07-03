package dev.daniel730.rpgserver.quest;

import dev.daniel730.rpgserver.RpgServerPlugin;
import dev.daniel730.rpgserver.profile.PlayerProfile;
import dev.daniel730.rpgserver.quest.objective.ObjectiveTypeRegistry;
import dev.daniel730.rpgserver.quest.objective.ObjectiveTypes;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
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
    private final Map<String, Quest> quests = new LinkedHashMap<>();

    public QuestManager(RpgServerPlugin plugin) {
        this.plugin = plugin;
        this.objectiveTypeRegistry = new ObjectiveTypeRegistry();
        this.objectiveTypeRegistry.registerDefaults();
        this.rewardExecutor = new RewardExecutor(plugin);
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
        List<String> requiredQuestIds = config.getStringList("requires");
        List<Map<?, ?>> rawObjectives = config.getMapList("objectives");
        List<Quest.Objective> objectives = new ArrayList<>();
        for (Map<?, ?> raw : rawObjectives) {
            objectives.add(objectiveTypeRegistry.parseObjective(raw));
        }
        RewardDefinition rewards = RewardDefinition.fromConfig(config.getConfigurationSection("rewards"));
        return new Quest(id, name, archetype, description, loreBook, requiredQuestIds, objectives, rewards);
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

    public void handleMineBlock(Player player, String blockKey) {
        if (blockKey == null || blockKey.isBlank()) {
            return;
        }
        String normalizedBlock = blockKey.toLowerCase(Locale.ROOT);
        processCountObjectives(player, ObjectiveTypes.MINE_BLOCK, objective ->
                objective.getBlock() == null
                        || objective.getBlock().equalsIgnoreCase(normalizedBlock), 1);
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
        if (profile.isObjectiveComplete(quest.getId(), objective.getId())) {
            return;
        }
        profile.completeObjective(quest.getId(), objective.getId());
        maybeSetArchetype(profile, quest);
        plugin.getMessageUtil().send(player,
                "<yellow>Objetivo concluído:</yellow> " + objective.getDescription());
    }

    private void ensureQuestStarted(Player player, PlayerProfile profile, Quest quest) {
        if (!profile.markQuestStarted(quest.getId())) {
            return;
        }
        if (quest.getLoreBook() != null && !quest.getLoreBook().isBlank()) {
            plugin.getInteractiveBooksHook().grantLoreBook(player, quest.getLoreBook());
        }
        if (plugin.getVaultHook().isEnabled()) {
            profile.setQuestStartBalance(quest.getId(), plugin.getVaultHook().getBalance(player));
        }
    }

    private void finalizeProgress(Player player, PlayerProfile profile) {
        checkQuestCompletions(player, profile);
        plugin.getProfileManager().markDirty(player.getUniqueId());
    }

    private void maybeSetArchetype(PlayerProfile profile, Quest quest) {
        if (quest.getArchetype() != null && !quest.getArchetype().isBlank()) {
            if (profile.getArchetype() == null || profile.getArchetype().isBlank()) {
                profile.setArchetype(quest.getArchetype());
            }
        }
        profile.addActiveQuest(quest.getId());
    }

    private void checkQuestCompletions(Player player, PlayerProfile profile) {
        for (Quest quest : quests.values()) {
            if (isQuestComplete(profile, quest) && !profile.isQuestComplete(quest.getId())) {
                profile.markQuestComplete(quest.getId());
                plugin.getMessageUtil().send(player, "<green>Quest concluída:</green> " + quest.getName());
                rewardExecutor.grantRewards(player, quest);
            }
        }
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

    public Quest findPrimaryActiveQuest(PlayerProfile profile) {
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
