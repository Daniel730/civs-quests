package dev.daniel730.rpgserver.quest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public final class Quest {

    public enum ObjectiveType {
        BUILD_REGION,
        SKILL_LEVEL;

        public static ObjectiveType fromConfig(String value) {
            if (value == null) {
                throw new IllegalArgumentException("Tipo de objetivo ausente");
            }
            return switch (value.toLowerCase(Locale.ROOT)) {
                case "build_region" -> BUILD_REGION;
                case "skill_level" -> SKILL_LEVEL;
                default -> throw new IllegalArgumentException("Tipo de objetivo desconhecido: " + value);
            };
        }
    }

    public static final class Objective {
        private final String id;
        private final ObjectiveType type;
        private final String description;
        private final String region;
        private final String skill;
        private final int targetLevel;

        public Objective(String id, ObjectiveType type, String description, String region, String skill, int targetLevel) {
            this.id = id;
            this.type = type;
            this.description = description;
            this.region = region;
            this.skill = skill;
            this.targetLevel = targetLevel;
        }

        public String getId() {
            return id;
        }

        public ObjectiveType getType() {
            return type;
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
    }

    private final String id;
    private final String name;
    private final String archetype;
    private final String description;
    private final List<Objective> objectives;

    public Quest(String id, String name, String archetype, String description, List<Objective> objectives) {
        this.id = id;
        this.name = name;
        this.archetype = archetype;
        this.description = description;
        this.objectives = Collections.unmodifiableList(new ArrayList<>(objectives));
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

    public List<Objective> getObjectives() {
        return objectives;
    }
}
