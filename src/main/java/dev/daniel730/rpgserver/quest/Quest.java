package dev.daniel730.rpgserver.quest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Quest {

    public static final class Objective {
        private final String id;
        private final String typeId;
        private final String description;
        private final String region;
        private final String skill;
        private final int targetLevel;
        private final String mob;
        private final String block;
        private final int amount;
        private final boolean countBased;

        public Objective(String id, String typeId, String description, String region, String skill,
                         int targetLevel, String mob, String block, int amount, boolean countBased) {
            this.id = id;
            this.typeId = typeId;
            this.description = description;
            this.region = region;
            this.skill = skill;
            this.targetLevel = targetLevel;
            this.mob = mob;
            this.block = block;
            this.amount = amount;
            this.countBased = countBased;
        }

        public String getId() {
            return id;
        }

        public String getTypeId() {
            return typeId;
        }

        public String getDescription() {
            return description;
        }

        public String getRegion() {
            return region;
        }

        public String getSkill() {
            return skill;
        }

        public int getTargetLevel() {
            return targetLevel;
        }

        public String getMob() {
            return mob;
        }

        public String getBlock() {
            return block;
        }

        public int getAmount() {
            return amount;
        }

        public boolean isCountBased() {
            return countBased;
        }
    }

    private final String id;
    private final String name;
    private final String archetype;
    private final String description;
    private final String loreBook;
    private final String unlocksPerk;
    private final QuestSchedule schedule;
    private final List<String> requiredQuestIds;
    private final List<Objective> objectives;
    private final RewardDefinition rewards;

    public Quest(String id, String name, String archetype, String description, String loreBook,
                 String unlocksPerk, QuestSchedule schedule, List<String> requiredQuestIds,
                 List<Objective> objectives, RewardDefinition rewards) {
        this.id = id;
        this.name = name;
        this.archetype = archetype;
        this.description = description;
        this.loreBook = loreBook;
        this.unlocksPerk = unlocksPerk;
        this.schedule = schedule == null ? QuestSchedule.NONE : schedule;
        this.requiredQuestIds = Collections.unmodifiableList(new ArrayList<>(requiredQuestIds));
        this.objectives = Collections.unmodifiableList(new ArrayList<>(objectives));
        this.rewards = rewards == null ? RewardDefinition.empty() : rewards;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getArchetype() {
        return archetype;
    }

    public String getDescription() {
        return description;
    }

    public String getLoreBook() {
        return loreBook;
    }

    public String getUnlocksPerk() {
        return unlocksPerk;
    }

    public QuestSchedule getSchedule() {
        return schedule;
    }

    public boolean isScheduled() {
        return schedule != QuestSchedule.NONE;
    }

    public List<String> getRequiredQuestIds() {
        return requiredQuestIds;
    }

    public List<Objective> getObjectives() {
        return objectives;
    }

    public RewardDefinition getRewards() {
        return rewards;
    }
}
