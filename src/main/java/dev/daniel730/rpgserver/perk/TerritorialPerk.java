package dev.daniel730.rpgserver.perk;

/**
 * Territorial stat perk applied via Civs {@code StatManager}. Full SkillTreeManager YAML loading is RPG-011 follow-up.
 */
public final class TerritorialPerk {

    public enum Operation {
        ADD,
        MULTIPLY
    }

    private final String id;
    private final String statKey;
    private final double value;
    private final Operation operation;

    public TerritorialPerk(String id, String statKey, double value, Operation operation) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("perk id is required");
        }
        if (statKey == null || statKey.isBlank()) {
            throw new IllegalArgumentException("stat key is required");
        }
        this.id = id;
        this.statKey = statKey;
        this.value = value;
        this.operation = operation == null ? Operation.ADD : operation;
    }

    public String getId() {
        return id;
    }

    public String getStatKey() {
        return statKey;
    }

    public double getValue() {
        return value;
    }

    public Operation getOperation() {
        return operation;
    }
}
