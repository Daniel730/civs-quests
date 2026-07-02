package dev.daniel730.rpgserver.profile;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

public final class PlayerProfile {

    private final UUID uuid;
    private String archetype;
    private final Set<String> activeQuestIds = new LinkedHashSet<>();
    private final Set<String> completedQuestIds = new LinkedHashSet<>();
    private final Set<String> completedObjectiveKeys = new LinkedHashSet<>();

    public PlayerProfile(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getArchetype() {
        return archetype;
    }

    public void setArchetype(String archetype) {
        this.archetype = archetype;
    }

    public Set<String> getActiveQuestIds() {
        return Collections.unmodifiableSet(activeQuestIds);
    }

    public void addActiveQuest(String questId) {
        activeQuestIds.add(questId);
    }

    public Set<String> getCompletedQuestIds() {
        return Collections.unmodifiableSet(completedQuestIds);
    }

    public void markQuestComplete(String questId) {
        completedQuestIds.add(questId);
    }

    public boolean isQuestComplete(String questId) {
        return completedQuestIds.contains(questId);
    }

    public boolean isObjectiveComplete(String questId, String objectiveId) {
        return completedObjectiveKeys.contains(objectiveKey(questId, objectiveId));
    }

    public void completeObjective(String questId, String objectiveId) {
        completedObjectiveKeys.add(objectiveKey(questId, objectiveId));
        addActiveQuest(questId);
    }

    private static String objectiveKey(String questId, String objectiveId) {
        return questId + ":" + objectiveId;
    }

    public Set<String> getCompletedObjectiveKeys() {
        return Collections.unmodifiableSet(completedObjectiveKeys);
    }

    public void setActiveQuestIds(Set<String> ids) {
        activeQuestIds.clear();
        activeQuestIds.addAll(ids);
    }

    public void setCompletedQuestIds(Set<String> ids) {
        completedQuestIds.clear();
        completedQuestIds.addAll(ids);
    }

    public void setCompletedObjectiveKeys(Set<String> keys) {
        completedObjectiveKeys.clear();
        completedObjectiveKeys.addAll(keys);
    }
}
