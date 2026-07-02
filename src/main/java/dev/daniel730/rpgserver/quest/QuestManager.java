package dev.daniel730.rpgserver.quest;

import dev.daniel730.rpgserver.RpgServerPlugin;
import dev.daniel730.rpgserver.profile.PlayerProfile;
import org.bukkit.configuration.ConfigurationSection;
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
import java.util.logging.Level;

public final class QuestManager {

    private final RpgServerPlugin plugin;
    private final Map<String, Quest> quests = new LinkedHashMap<>();

    public QuestManager(RpgServerPlugin plugin) {
        this.plugin = plugin;
    }

    public void loadQuests() {
        quests.clear();
        File questsFolder = new File(plugin.getDataFolder(), "quests");
        if (!questsFolder.exists()) {
            questsFolder.mkdirs();
            plugin.saveResource("quests/warrior_path.yml", false);
            plugin.saveResource("quests/builder_path.yml", false);
            plugin.saveResource("quests/merchant_path.yml", false);
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
        List<Map<?, ?>> rawObjectives = config.getMapList("objectives");
        List<Quest.Objective> objectives = new ArrayList<>();
        for (Map<?, ?> raw : rawObjectives) {
            objectives.add(parseObjective(raw));
        }
        return new Quest(id, name, archetype, description, objectives);
    }

    private Quest.Objective parseObjective(Map<?, ?> raw) {
        Object idObj = raw.get("id");
        if (idObj == null) {
            throw new IllegalArgumentException("Objetivo sem 'id'");
        }
        String id = String.valueOf(idObj);
        Object typeObj = raw.get("type");
        Quest.ObjectiveType type = Quest.ObjectiveType.fromConfig(typeObj == null ? null : String.valueOf(typeObj));
        String description = raw.get("description") == null ? id : String.valueOf(raw.get("description"));
        String region = raw.get("region") == null ? null : String.valueOf(raw.get("region"));
        String skill = raw.get("skill") == null ? null : String.valueOf(raw.get("skill"));
        int level = 1;
        Object levelObj = raw.get("level");
        if (levelObj instanceof Number number) {
            level = number.intValue();
        } else if (levelObj != null) {
            level = Integer.parseInt(String.valueOf(levelObj));
        }
        return new Quest.Objective(id, type, description, region, skill, level);
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
        PlayerProfile profile = plugin.getProfileManager().getOrCreate(player);
        boolean changed = false;

        for (Quest quest : quests.values()) {
            if (!plugin.getLuckPermsHook().hasQuestPermission(player, quest.getId())) {
                continue;
            }
            for (Quest.Objective objective : quest.getObjectives()) {
                if (objective.getType() != Quest.ObjectiveType.BUILD_REGION) {
                    continue;
                }
                if (objective.getRegion() != null
                        && objective.getRegion().equalsIgnoreCase(normalizedRegion)
                        && !profile.isObjectiveComplete(quest.getId(), objective.getId())) {
                    profile.completeObjective(quest.getId(), objective.getId());
                    maybeSetArchetype(profile, quest);
                    changed = true;
                    plugin.getMessageUtil().send(player,
                            "<yellow>Objetivo concluído:</yellow> " + objective.getDescription());
                }
            }
        }

        if (changed) {
            checkQuestCompletions(player, profile);
            plugin.getProfileManager().markDirty(player.getUniqueId());
        }
    }

    public void handleSkillLevelUp(Player player, String skillKey, int level) {
        if (skillKey == null || skillKey.isBlank()) {
            return;
        }
        String normalizedSkill = skillKey.toLowerCase(Locale.ROOT);
        PlayerProfile profile = plugin.getProfileManager().getOrCreate(player);
        boolean changed = false;

        for (Quest quest : quests.values()) {
            if (!plugin.getLuckPermsHook().hasQuestPermission(player, quest.getId())) {
                continue;
            }
            for (Quest.Objective objective : quest.getObjectives()) {
                if (objective.getType() != Quest.ObjectiveType.SKILL_LEVEL) {
                    continue;
                }
                if (objective.getSkill() != null
                        && objective.getSkill().equalsIgnoreCase(normalizedSkill)
                        && level >= objective.getTargetLevel()
                        && !profile.isObjectiveComplete(quest.getId(), objective.getId())) {
                    profile.completeObjective(quest.getId(), objective.getId());
                    maybeSetArchetype(profile, quest);
                    changed = true;
                    plugin.getMessageUtil().send(player,
                            "<yellow>Objetivo concluído:</yellow> " + objective.getDescription());
                }
            }
        }

        if (changed) {
            checkQuestCompletions(player, profile);
            plugin.getProfileManager().markDirty(player.getUniqueId());
        }
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
            }
        }
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
        boolean anyProgress = quest.getObjectives().stream()
                .anyMatch(objective -> profile.isObjectiveComplete(quest.getId(), objective.getId()));
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
