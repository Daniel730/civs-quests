package dev.daniel730.rpgserver.testutil;

import dev.daniel730.rpgserver.quest.Quest;
import dev.daniel730.rpgserver.quest.QuestSchedule;
import dev.daniel730.rpgserver.quest.RewardDefinition;

import java.util.List;

/** Builds minimal {@link Quest} instances for unit tests without touching Bukkit/YAML. */
public final class QuestTestFactory {

    private QuestTestFactory() {
    }

    public static Quest.Objective objective(String id, String typeId) {
        return new Quest.Objective(id, typeId, id, null, null, 0, null, null, 0, false);
    }

    /** A quest with the given archetype and a single instant objective. */
    public static Quest quest(String id, String archetype, String objectiveType) {
        return new Quest(id, id, archetype, "", List.of(), 1, "story",
                null, List.of(), QuestSchedule.NONE, List.of(),
                List.of(objective(id + "_obj", objectiveType)), RewardDefinition.empty());
    }

    /** A path quest (id must be one of {@code warrior_path/merchant_path/builder_path}). */
    public static Quest pathQuest(String id, String archetype) {
        return quest(id, archetype, "build_region");
    }

    /** A quest that requires other quests to be completed first. */
    public static Quest questRequiring(String id, String archetype, List<String> requiredQuestIds) {
        return new Quest(id, id, archetype, "", List.of(), 1, "story",
                null, List.of(), QuestSchedule.NONE, requiredQuestIds,
                List.of(objective(id + "_obj", "open_hub")), RewardDefinition.empty());
    }
}
