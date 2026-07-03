package dev.daniel730.rpgserver.profile;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class PlayerProfile {

    private final UUID uuid;
    private String archetype;
    private String trackedQuestId;
    private final Set<String> activeQuestIds = new LinkedHashSet<>();
    private final Set<String> completedQuestIds = new LinkedHashSet<>();
    private final Set<String> completedObjectiveKeys = new LinkedHashSet<>();
    private final Set<String> startedQuestIds = new LinkedHashSet<>();
    private final Map<String, Integer> objectiveProgress = new HashMap<>();
    private final Map<String, Double> questStartBalances = new HashMap<>();
    private final Map<String, Long> questCompletionEpochMs = new HashMap<>();
    private final Set<String> unlockedPerkIds = new LinkedHashSet<>();
    private String dailyCtaShownDay;
    private boolean welcomeShown;

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

    public String getTrackedQuestId() {
        return trackedQuestId;
    }

    public void setTrackedQuestId(String trackedQuestId) {
        this.trackedQuestId = trackedQuestId;
    }

    public Set<String> getActiveQuestIds() {
        return Collections.unmodifiableSet(activeQuestIds);
    }

    public void addActiveQuest(String questId) {
        activeQuestIds.add(questId);
    }

    public void removeActiveQuest(String questId) {
        activeQuestIds.remove(questId);
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

    public int getObjectiveProgress(String questId, String objectiveId) {
        return objectiveProgress.getOrDefault(objectiveKey(questId, objectiveId), 0);
    }

    public void setObjectiveProgress(String questId, String objectiveId, int progress) {
        objectiveProgress.put(objectiveKey(questId, objectiveId), Math.max(0, progress));
    }

    public void addObjectiveProgress(String questId, String objectiveId, int delta) {
        if (delta <= 0) {
            return;
        }
        setObjectiveProgress(questId, objectiveId, getObjectiveProgress(questId, objectiveId) + delta);
    }

    public Double getQuestStartBalance(String questId) {
        return questStartBalances.get(questId);
    }

    public void setQuestStartBalance(String questId, double balance) {
        questStartBalances.put(questId, balance);
    }

    public Long getQuestCompletedAt(String questId) {
        return questCompletionEpochMs.get(questId);
    }

    public void setQuestCompletedAt(String questId, long epochMs) {
        questCompletionEpochMs.put(questId, epochMs);
    }

    public Map<String, Long> getQuestCompletionTimesSnapshot() {
        return Collections.unmodifiableMap(questCompletionEpochMs);
    }

    public void setQuestCompletionTimes(Map<String, Long> times) {
        questCompletionEpochMs.clear();
        questCompletionEpochMs.putAll(times);
    }

    public void clearQuestState(String questId) {
        completedQuestIds.remove(questId);
        activeQuestIds.remove(questId);
        startedQuestIds.remove(questId);
        questStartBalances.remove(questId);
        questCompletionEpochMs.remove(questId);
        if (questId != null && questId.equals(trackedQuestId)) {
            trackedQuestId = null;
        }
        String prefix = questId + ":";
        completedObjectiveKeys.removeIf(key -> key.startsWith(prefix));
        objectiveProgress.keySet().removeIf(key -> key.startsWith(prefix));
    }

    public boolean markQuestStarted(String questId) {
        return startedQuestIds.add(questId);
    }

    public boolean hasQuestStarted(String questId) {
        return startedQuestIds.contains(questId);
    }

    private static String objectiveKey(String questId, String objectiveId) {
        return questId + ":" + objectiveId;
    }

    public Set<String> getCompletedObjectiveKeys() {
        return Collections.unmodifiableSet(completedObjectiveKeys);
    }

    public Map<String, Integer> getObjectiveProgressSnapshot() {
        return Collections.unmodifiableMap(objectiveProgress);
    }

    public Map<String, Double> getQuestStartBalancesSnapshot() {
        return Collections.unmodifiableMap(questStartBalances);
    }

    public Set<String> getStartedQuestIds() {
        return Collections.unmodifiableSet(startedQuestIds);
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

    public void setStartedQuestIds(Set<String> ids) {
        startedQuestIds.clear();
        startedQuestIds.addAll(ids);
    }

    public void setObjectiveProgressMap(Map<String, Integer> progress) {
        objectiveProgress.clear();
        objectiveProgress.putAll(progress);
    }

    public void setQuestStartBalances(Map<String, Double> balances) {
        questStartBalances.clear();
        questStartBalances.putAll(balances);
    }

    public Set<String> getUnlockedPerkIds() {
        return Collections.unmodifiableSet(unlockedPerkIds);
    }

    public boolean isPerkUnlocked(String perkId) {
        return unlockedPerkIds.contains(perkId);
    }

    public boolean unlockPerk(String perkId) {
        return unlockedPerkIds.add(perkId);
    }

    public void setUnlockedPerkIds(Set<String> ids) {
        unlockedPerkIds.clear();
        unlockedPerkIds.addAll(ids);
    }

    public String getDailyCtaShownDay() {
        return dailyCtaShownDay;
    }

    public void setDailyCtaShownDay(String dailyCtaShownDay) {
        this.dailyCtaShownDay = dailyCtaShownDay;
    }

    public boolean isWelcomeShown() {
        return welcomeShown;
    }

    public void setWelcomeShown(boolean welcomeShown) {
        this.welcomeShown = welcomeShown;
    }
}
