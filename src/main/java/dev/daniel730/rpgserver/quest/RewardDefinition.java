package dev.daniel730.rpgserver.quest;

import org.bukkit.configuration.ConfigurationSection;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class RewardDefinition {

    private final double money;
    private final Map<String, Double> skillXp;
    private final Map<String, Double> civsSkillXp;
    private final String permission;
    private final String lpGroup;
    private final String essentialsKit;
    private final String warp;
    private final String lootTable;

    public RewardDefinition(double money, Map<String, Double> skillXp, Map<String, Double> civsSkillXp,
                            String permission, String lpGroup, String essentialsKit, String warp, String lootTable) {
        this.money = money;
        this.skillXp = Collections.unmodifiableMap(new LinkedHashMap<>(skillXp));
        this.civsSkillXp = Collections.unmodifiableMap(new LinkedHashMap<>(civsSkillXp));
        this.permission = permission;
        this.lpGroup = lpGroup;
        this.essentialsKit = essentialsKit;
        this.warp = warp;
        this.lootTable = lootTable;
    }

    public static RewardDefinition empty() {
        return new RewardDefinition(0, Map.of(), Map.of(), null, null, null, null, null);
    }

    public static RewardDefinition fromConfig(ConfigurationSection section) {
        if (section == null) {
            return empty();
        }
        double money = section.getDouble("money", 0);
        Map<String, Double> skillXp = parseSkillMap(section.getConfigurationSection("skill-xp"));
        Map<String, Double> civsSkillXp = parseSkillMap(section.getConfigurationSection("civs-skill-xp"));
        String permission = blankToNull(section.getString("permission"));
        String lpGroup = blankToNull(section.getString("lp-group"));
        String essentialsKit = blankToNull(section.getString("essentials-kit"));
        String warp = blankToNull(section.getString("warp"));
        String lootTable = blankToNull(section.getString("loot-table"));
        return new RewardDefinition(money, skillXp, civsSkillXp, permission, lpGroup, essentialsKit, warp, lootTable);
    }

    private static Map<String, Double> parseSkillMap(ConfigurationSection section) {
        Map<String, Double> map = new LinkedHashMap<>();
        if (section != null) {
            for (String skill : section.getKeys(false)) {
                map.put(skill.toLowerCase(), section.getDouble(skill));
            }
        }
        return map;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    public boolean isEmpty() {
        return money <= 0
                && skillXp.isEmpty()
                && civsSkillXp.isEmpty()
                && permission == null
                && lpGroup == null
                && essentialsKit == null
                && warp == null
                && lootTable == null;
    }

    public double getMoney() {
        return money;
    }

    public Map<String, Double> getSkillXp() {
        return skillXp;
    }

    public Map<String, Double> getCivsSkillXp() {
        return civsSkillXp;
    }

    public String getPermission() {
        return permission;
    }

    public String getLpGroup() {
        return lpGroup;
    }

    public String getEssentialsKit() {
        return essentialsKit;
    }

    public String getWarp() {
        return warp;
    }

    public String getLootTable() {
        return lootTable;
    }
}
