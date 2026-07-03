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
    private final List<String> requiredIds;
    private final PerkType type;
    private final String statKey;
    private final double value;
    private final String operation;

    public PerkDefinition(String id, String name, String archetype, List<String> requiredIds,
                          PerkType type, String statKey, double value, String operation) {
        this.id = id;
        this.name = name;
        this.archetype = archetype == null ? "" : archetype;
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
