package dev.daniel730.rpgserver.quest;

import org.bukkit.configuration.ConfigurationSection;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class RewardDefinition {

    private final double money;
    private final Map<String, Double> skillXp;
    private final String permission;
    private final String essentialsKit;
    private final String warp;

    public RewardDefinition(double money, Map<String, Double> skillXp, String permission,
                            String essentialsKit, String warp) {
        this.money = money;
        this.skillXp = Collections.unmodifiableMap(new LinkedHashMap<>(skillXp));
        this.permission = permission;
        this.essentialsKit = essentialsKit;
        this.warp = warp;
    }

    public static RewardDefinition empty() {
        return new RewardDefinition(0, Map.of(), null, null, null);
    }

    public static RewardDefinition fromConfig(ConfigurationSection section) {
        if (section == null) {
            return empty();
        }
        double money = section.getDouble("money", 0);
        Map<String, Double> skillXp = new LinkedHashMap<>();
        ConfigurationSection xpSection = section.getConfigurationSection("skill-xp");
        if (xpSection != null) {
            for (String skill : xpSection.getKeys(false)) {
                skillXp.put(skill.toLowerCase(), xpSection.getDouble(skill));
            }
        }
        String permission = blankToNull(section.getString("permission"));
        String essentialsKit = blankToNull(section.getString("essentials-kit"));
        String warp = blankToNull(section.getString("warp"));
        return new RewardDefinition(money, skillXp, permission, essentialsKit, warp);
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    public boolean isEmpty() {
        return money <= 0
                && skillXp.isEmpty()
                && permission == null
                && essentialsKit == null
                && warp == null;
    }

    public double getMoney() {
        return money;
    }

    public Map<String, Double> getSkillXp() {
        return skillXp;
    }

    public String getPermission() {
        return permission;
    }

    public String getEssentialsKit() {
        return essentialsKit;
    }

    public String getWarp() {
        return warp;
    }
}
