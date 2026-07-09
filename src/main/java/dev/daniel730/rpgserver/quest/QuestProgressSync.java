package dev.daniel730.rpgserver.quest;

import dev.daniel730.rpgserver.RpgServerPlugin;
import dev.daniel730.rpgserver.profile.PlayerProfile;
import dev.daniel730.rpgserver.quest.objective.ObjectiveTypes;
import org.bukkit.entity.Player;

import java.util.Locale;
import java.util.Map;

/**
 * Backfills RPG quest step state from Civs, AuraSkills, and Vault — never grants skill XP.
 */
public final class QuestProgressSync {

    public record SyncResult(int objectivesCompleted, int questsCompleted) {
    }

    private final RpgServerPlugin plugin;

    public QuestProgressSync(RpgServerPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * @param grantRewards when false, only YAML step state is updated (no money, XP, or permissions)
     * @param notify       send chat messages for completions
     */
    public SyncResult sync(Player player, boolean grantRewards, boolean notify) {
        PlayerProfile profile = plugin.getProfileManager().getOrCreate(player);
        QuestManager questManager = plugin.getQuestManager();
        int objectivesCompleted = 0;
        int questsCompleted = 0;
        boolean changed;

        do {
            changed = false;
            for (Quest quest : questManager.getAllQuests()) {
                if (profile.isQuestComplete(quest.getId())) {
                    continue;
                }
                if (!questManager.canWorkOnQuest(player, profile, quest)) {
                    continue;
                }
                if (!shouldSyncQuest(profile, quest)) {
                    continue;
                }
                if (!questManager.meetsRequirements(profile, quest)) {
                    continue;
                }
                questManager.ensureQuestStartedForSync(profile, quest);

                for (Quest.Objective objective : quest.getObjectives()) {
                    if (profile.isObjectiveComplete(quest.getId(), objective.getId())) {
                        continue;
                    }
                    if (!isObjectiveSatisfied(player, profile, quest, objective)) {
                        continue;
                    }
                    applyObjectiveProgress(player, profile, quest, objective);
                    questManager.completeObjective(player, profile, quest, objective, grantRewards, notify);
                    objectivesCompleted++;
                    changed = true;
                }

                if (questManager.isQuestComplete(profile, quest) && !profile.isQuestComplete(quest.getId())) {
                    int completed = questManager.completeQuest(player, profile, quest, grantRewards, notify);
                    if (completed > 0) {
                        questsCompleted += completed;
                        changed = true;
                    }
                }
            }
        } while (changed);

        plugin.getSkillTreeManager().checkAutoUnlocks(player);
        plugin.getProfileManager().markDirty(player.getUniqueId());
        plugin.getQuestFeedbackService().refreshBossBar(player);
        return new SyncResult(objectivesCompleted, questsCompleted);
    }

    private boolean shouldSyncQuest(PlayerProfile profile, Quest quest) {
        if (profile.hasQuestStarted(quest.getId())) {
            return true;
        }
        if (profile.getActiveQuestIds().contains(quest.getId())) {
            return true;
        }
        return quest.getObjectives().stream()
                .anyMatch(objective -> profile.isObjectiveComplete(quest.getId(), objective.getId())
                        || profile.getObjectiveProgress(quest.getId(), objective.getId()) > 0);
    }

    private void applyObjectiveProgress(Player player, PlayerProfile profile, Quest quest, Quest.Objective objective) {
        if (!objective.isCountBased()) {
            return;
        }
        int current = resolveCurrentCount(player, profile, quest, objective);
        profile.setObjectiveProgress(quest.getId(), objective.getId(), Math.max(current, objective.getAmount()));
    }

    private int resolveCurrentCount(Player player, PlayerProfile profile, Quest quest, Quest.Objective objective) {
        String typeId = objective.getTypeId();
        // Incremental count objectives use RPG profile progress only — never Civs lifetime totals
        // (live handlers increment per event; backfilling totals would instant-complete quests).
        if (ObjectiveTypes.KILL_MOB.equals(typeId) || ObjectiveTypes.CIVS_SKILL_XP.equals(typeId)) {
            return profile.getObjectiveProgress(quest.getId(), objective.getId());
        }
        if (ObjectiveTypes.EARN_MONEY.equals(typeId) && plugin.getVaultHook().isEnabled()) {
            Double startBalance = profile.getQuestStartBalance(quest.getId());
            if (startBalance == null) {
                double balance = plugin.getVaultHook().getBalance(player);
                profile.setQuestStartBalance(quest.getId(), balance);
                startBalance = balance;
            }
            return (int) Math.floor(plugin.getVaultHook().getBalance(player) - startBalance);
        }
        return profile.getObjectiveProgress(quest.getId(), objective.getId());
    }

    private boolean isObjectiveSatisfied(Player player, PlayerProfile profile, Quest quest, Quest.Objective objective) {
        String typeId = objective.getTypeId();
        if (ObjectiveTypes.BUILD_REGION.equals(typeId)) {
            return plugin.getCivsHook().isEnabled()
                    && plugin.getCivsHook().hasBuiltRegion(player, objective.getRegion());
        }
        if (ObjectiveTypes.SKILL_LEVEL.equals(typeId)) {
            return plugin.getAuraSkillsHook().isEnabled()
                    && plugin.getAuraSkillsHook().getSkillLevel(player, objective.getSkill())
                    >= objective.getTargetLevel();
        }
        if (ObjectiveTypes.CIVS_SKILL_LEVEL.equals(typeId)) {
            return plugin.getCivsHook().isEnabled()
                    && plugin.getCivsHook().getSkillLevel(player, objective.getSkill())
                    >= objective.getTargetLevel();
        }
        if (ObjectiveTypes.BALANCE_MIN.equals(typeId)) {
            return plugin.getVaultHook().isEnabled()
                    && plugin.getVaultHook().getBalance(player) >= objective.getAmount();
        }
        if (ObjectiveTypes.JOIN_TOWN.equals(typeId)) {
            return plugin.getCivsHook().isEnabled()
                    && plugin.getCivsHook().isTownMember(player, objective.getRegion());
        }
        if (ObjectiveTypes.DISCOVER_POI.equals(typeId)) {
            return objective.getRegion() != null && profile.hasDiscoveredPoi(objective.getRegion());
        }
        if (ObjectiveTypes.DISCOVER_BIOME.equals(typeId)) {
            return objective.getBlock() != null && profile.hasDiscoveredBiome(objective.getBlock());
        }
        if (ObjectiveTypes.OPEN_HUB.equals(typeId)) {
            return profile.isHubOpened();
        }
        if (objective.isCountBased()) {
            return resolveCurrentCount(player, profile, quest, objective) >= objective.getAmount();
        }
        return false;
    }
}
