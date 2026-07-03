package dev.daniel730.rpgserver.hook;

import dev.daniel730.rpgserver.RpgServerPlugin;
import dev.daniel730.rpgserver.perk.TerritorialPerk;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.skills.Skill;
import org.redcastlemedia.multitallented.civs.stats.StatManager;
import org.redcastlemedia.multitallented.civs.stats.StatModifier;
import org.redcastlemedia.multitallented.civs.stats.StatOperation;
import org.redcastlemedia.multitallented.civs.stats.TerritorialStat;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class CivsHook {

    private static final String TERRITORIAL_MODIFIER_PREFIX = "rpg_";

    private final RpgServerPlugin plugin;
    private boolean enabled;

    public CivsHook(RpgServerPlugin plugin) {
        this.plugin = plugin;
    }

    public void enable() {
        if (!plugin.getPluginConfig().isCivsEnabled()) {
            return;
        }
        enabled = Bukkit.getPluginManager().getPlugin("Civs") != null;
        if (enabled) {
            plugin.getLogger().info("Civs detectado — região, skills internas, leilão e StatManager habilitados.");
        } else {
            plugin.getLogger().warning("Civs não encontrado — objetivos Civs e perks territoriais ficarão inativos.");
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean hasBuiltRegion(Player player, String regionKey) {
        if (!enabled || player == null || regionKey == null || regionKey.isBlank()) {
            return false;
        }
        String normalized = regionKey.toLowerCase(Locale.ROOT);
        Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
        if (civilian == null) {
            return false;
        }
        Skill building = civilian.getSkills().get("building");
        if (building != null && building.getAccomplishments().getOrDefault(normalized, 0) > 0) {
            return true;
        }
        Map<String, Integer> stash = civilian.getStashItems();
        if (stash != null && stash.getOrDefault(normalized, 0) > 0) {
            return true;
        }
        for (Region region : RegionManager.getInstance().getAllRegions()) {
            if (region.getType().equalsIgnoreCase(normalized)
                    && region.getOwners().contains(player.getUniqueId())) {
                return true;
            }
        }
        return false;
    }

    public int getMobKillCount(Player player, String mobKey) {
        if (!enabled || player == null || mobKey == null || mobKey.isBlank()) {
            return 0;
        }
        Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
        if (civilian == null) {
            return 0;
        }
        String normalizedMob = mobKey.toLowerCase(Locale.ROOT);
        int total = 0;
        for (String combatSkill : List.of("sword", "axe")) {
            Skill skill = civilian.getSkills().get(combatSkill);
            if (skill == null) {
                continue;
            }
            for (Map.Entry<String, Integer> entry : skill.getAccomplishments().entrySet()) {
                String key = entry.getKey().toLowerCase(Locale.ROOT);
                if (key.equals(normalizedMob) || key.contains(normalizedMob)) {
                    total += entry.getValue();
                }
            }
        }
        return total;
    }

    public double getSkillTotalExp(Player player, String skillKey) {
        if (!enabled || player == null) {
            return 0;
        }
        Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
        if (civilian == null) {
            return 0;
        }
        if (skillKey == null || skillKey.isBlank()) {
            double total = 0;
            for (Skill skill : civilian.getSkills().values()) {
                total += skill.getTotalExp();
            }
            return total;
        }
        Skill skill = findSkill(civilian, skillKey);
        return skill == null ? 0 : skill.getTotalExp();
    }

    public int getSkillLevel(Player player, String skillKey) {
        if (!enabled || player == null || skillKey == null || skillKey.isBlank()) {
            return 0;
        }
        Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
        if (civilian == null) {
            return 0;
        }
        Skill skill = findSkill(civilian, skillKey);
        return skill == null ? 0 : skill.getLevel();
    }

    public boolean addTerritorialModifier(Player player, TerritorialPerk perk) {
        if (!enabled || player == null || perk == null) {
            return false;
        }
        TerritorialStat stat = TerritorialStat.fromKey(perk.getStatKey());
        if (stat == null) {
            plugin.getLogger().warning("Stat territorial Civs desconhecido: " + perk.getStatKey());
            return false;
        }
        StatOperation operation = perk.getOperation() == TerritorialPerk.Operation.MULTIPLY
                ? StatOperation.MULTIPLY
                : StatOperation.ADD;
        StatModifier modifier = new StatModifier(
                territorialModifierId(perk.getId()),
                stat,
                perk.getValue(),
                operation);
        StatManager.getInstance().addModifier(player.getUniqueId(), modifier);
        return true;
    }

    public boolean removeTerritorialModifier(Player player, String perkId) {
        if (!enabled || player == null || perkId == null || perkId.isBlank()) {
            return false;
        }
        return StatManager.getInstance().removeModifier(player.getUniqueId(), territorialModifierId(perkId));
    }

    public boolean hasTerritorialModifier(Player player, String perkId) {
        if (!enabled || player == null || perkId == null || perkId.isBlank()) {
            return false;
        }
        return StatManager.getInstance().hasModifier(player.getUniqueId(), territorialModifierId(perkId));
    }

    public static String territorialModifierId(String perkId) {
        if (perkId == null || perkId.isBlank()) {
            throw new IllegalArgumentException("perk id is required");
        }
        return perkId.startsWith(TERRITORIAL_MODIFIER_PREFIX) ? perkId : TERRITORIAL_MODIFIER_PREFIX + perkId;
    }

    private Skill findSkill(Civilian civilian, String name) {
        Skill skill = civilian.getSkills().get(name.toLowerCase(Locale.ROOT));
        if (skill != null) {
            return skill;
        }
        for (Skill current : civilian.getSkills().values()) {
            if (current.getType().equalsIgnoreCase(name)) {
                return current;
            }
        }
        return null;
    }
}
