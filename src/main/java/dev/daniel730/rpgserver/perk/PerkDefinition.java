package dev.daniel730.rpgserver.perk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class PerkDefinition {

    public enum PerkType {
        AURASKILLS_STAT("auraskills-stat"),
        CIVS_TERRITORIAL("civs-territorial");

        private final String yamlKey;

        PerkType(String yamlKey) {
            this.yamlKey = yamlKey;
        }

        public String getYamlKey() {
            return yamlKey;
        }

        public static PerkType fromYaml(String key) {
            if (key == null) {
                return null;
            }
            for (PerkType type : values()) {
                if (type.yamlKey.equalsIgnoreCase(key)) {
                    return type;
                }
            }
            return null;
        }
    }

    private final String id;
    private final String name;
    private final String archetype;
    private final String branch;
    private final int tier;
    private final String exclusiveGroup;
    private final int essenceCost;
    private final List<String> requiredIds;
    private final PerkType type;
    private final String statKey;
    private final double value;
    private final String operation;

    public PerkDefinition(String id, String name, String archetype, String branch, int tier,
                          String exclusiveGroup, int essenceCost, List<String> requiredIds,
                          PerkType type, String statKey, double value, String operation) {
        this.id = id;
        this.name = name;
        this.archetype = archetype == null ? "" : archetype;
        this.branch = branch == null ? "" : branch;
        this.tier = tier;
        this.exclusiveGroup = exclusiveGroup == null ? "" : exclusiveGroup;
        this.essenceCost = Math.max(0, essenceCost);
        this.requiredIds = Collections.unmodifiableList(new ArrayList<>(requiredIds));
        this.type = type;
        this.statKey = statKey;
        this.value = value;
        this.operation = operation == null ? "add" : operation;
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

    public String getBranch() {
        return branch;
    }

    public int getTier() {
        return tier;
    }

    public String getExclusiveGroup() {
        return exclusiveGroup;
    }

    public int getEssenceCost() {
        return essenceCost;
    }

    public List<String> getRequiredIds() {
        return requiredIds;
    }

    public PerkType getType() {
        return type;
    }

    public String getStatKey() {
        return statKey;
    }

    public double getValue() {
        return value;
    }

    public String getOperation() {
        return operation;
    }
}
