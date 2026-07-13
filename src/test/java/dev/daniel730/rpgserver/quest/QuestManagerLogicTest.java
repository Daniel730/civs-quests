package dev.daniel730.rpgserver.quest;

import dev.daniel730.rpgserver.RpgServerPlugin;
import dev.daniel730.rpgserver.profile.PlayerProfile;
import dev.daniel730.rpgserver.testutil.QuestTestFactory;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * Unit tests for the pure archetype/path-conflict/status logic in {@link QuestManager}.
 * These lock in the behaviour behind the "archetype locked on path-quest start" fix and the
 * path-exclusivity rules, without needing a running server.
 */
public class QuestManagerLogicTest {

    private QuestManager questManager;
    private PlayerProfile profile;

    @Before
    public void setUp() {
        questManager = new QuestManager(mock(RpgServerPlugin.class));
        profile = new PlayerProfile(UUID.randomUUID());
    }

    @Test
    public void pathQuestIdsAreRecognised() {
        assertTrue(QuestManager.isPathQuest(QuestTestFactory.pathQuest("warrior_path", "warrior")));
        assertTrue(QuestManager.isPathQuest(QuestTestFactory.pathQuest("merchant_path", "merchant")));
        assertFalse(QuestManager.isPathQuest(QuestTestFactory.quest("daily_scout", "neutral", "kill_mob")));
    }

    @Test
    public void neutralArchetypeIsDetected() {
        assertTrue(QuestManager.isNeutralArchetype("neutral"));
        assertTrue(QuestManager.isNeutralArchetype("NEUTRAL"));
        assertFalse(QuestManager.isNeutralArchetype("warrior"));
        assertFalse(QuestManager.isNeutralArchetype(null));
    }

    @Test
    public void freshPlayerCanWorkOnAnyPathQuestButNotOtherArchetypeStoryQuests() {
        // No archetype chosen yet: path quests are offerable...
        assertTrue(questManager.matchesPlayerArchetype(profile,
                QuestTestFactory.pathQuest("warrior_path", "warrior")));
        // ...but non-path archetype-specific story quests are not, until a path is chosen.
        assertFalse(questManager.matchesPlayerArchetype(profile,
                QuestTestFactory.quest("warrior_champion", "warrior", "kill_mob")));
        // Neutral quests always match.
        assertTrue(questManager.matchesPlayerArchetype(profile,
                QuestTestFactory.quest("welcome", "neutral", "open_hub")));
    }

    @Test
    public void archetypeMatchingRespectsChosenPath() {
        profile.setArchetype("merchant");
        assertTrue(questManager.matchesPlayerArchetype(profile,
                QuestTestFactory.quest("merchant_bazaar", "merchant", "shop_sell")));
        assertFalse(questManager.matchesPlayerArchetype(profile,
                QuestTestFactory.quest("warrior_champion", "warrior", "kill_mob")));
    }

    @Test
    public void startingOnePathQuestConflictsWithOtherPaths() {
        // Simulate merchant_path having been started (as onQuestStarted does).
        profile.markQuestStarted("merchant_path");
        assertTrue(questManager.isConflictingPath(profile,
                QuestTestFactory.pathQuest("warrior_path", "warrior")));
        assertFalse(questManager.isConflictingPath(profile,
                QuestTestFactory.pathQuest("merchant_path", "merchant")));
    }

    @Test
    public void noPathConflictWhenNothingStarted() {
        assertFalse(questManager.isConflictingPath(profile,
                QuestTestFactory.pathQuest("warrior_path", "warrior")));
    }

    @Test
    public void archetypeMismatchConflictsForNonPathQuests() {
        profile.setArchetype("merchant");
        assertTrue(questManager.isConflictingPath(profile,
                QuestTestFactory.quest("warrior_champion", "warrior", "kill_mob")));
        assertFalse(questManager.isConflictingPath(profile,
                QuestTestFactory.quest("merchant_bazaar", "merchant", "shop_sell")));
    }

    @Test
    public void questStatusReflectsProfileState() {
        Quest quest = QuestTestFactory.quest("welcome", "neutral", "open_hub");
        assertEquals(QuestManager.QuestStatus.NOT_STARTED,
                questManager.getQuestStatus(profile, quest));

        profile.addActiveQuest("welcome");
        assertEquals(QuestManager.QuestStatus.IN_PROGRESS,
                questManager.getQuestStatus(profile, quest));

        profile.markQuestComplete("welcome");
        assertEquals(QuestManager.QuestStatus.COMPLETED,
                questManager.getQuestStatus(profile, quest));
    }

    @Test
    public void questStatusLockedWhenPrerequisiteMissing() {
        Quest quest = QuestTestFactory.questRequiring("chapter2", "neutral", List.of("chapter1"));
        assertEquals(QuestManager.QuestStatus.LOCKED,
                questManager.getQuestStatus(profile, quest));

        profile.markQuestComplete("chapter1");
        assertEquals(QuestManager.QuestStatus.NOT_STARTED,
                questManager.getQuestStatus(profile, quest));
    }

    @Test
    public void questCompleteRequiresAllObjectives() {
        Quest quest = QuestTestFactory.quest("welcome", "neutral", "open_hub");
        assertFalse(questManager.isQuestComplete(profile, quest));

        profile.completeObjective("welcome", "welcome_obj");
        assertTrue(questManager.isQuestComplete(profile, quest));
    }
}
