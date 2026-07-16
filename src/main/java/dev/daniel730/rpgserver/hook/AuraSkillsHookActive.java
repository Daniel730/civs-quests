package dev.daniel730.rpgserver.hook;

import dev.aurelium.auraskills.api.AuraSkillsApi;
import dev.aurelium.auraskills.api.skill.Skill;
import dev.aurelium.auraskills.api.skill.Skills;
import dev.aurelium.auraskills.api.stat.Stat;
import dev.aurelium.auraskills.api.stat.StatModifier;
import dev.aurelium.auraskills.api.stat.Stats;
import dev.aurelium.auraskills.api.user.SkillsUser;
import dev.aurelium.auraskills.api.util.AuraSkillsModifier;
import dev.daniel730.rpgserver.RpgServerPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Locale;

/**
 * Active AuraSkills implementation — loaded only when AuraSkills is present
 * ({@link SoftHookFactory}).
 */
public final class AuraSkillsHookActive extends AuraSkillsHook {

    private AuraSkillsApi api;

    public AuraSkillsHookActive(RpgServerPlugin plugin) {
        super(plugin);
    }

    @Override
    public void enable() {
        if (!plugin.getPluginConfig().isAuraSkillsEnabled()) {
            enabled = false;
            api = null;
            return;
        }
        if (Bukkit.getPluginManager().getPlugin("AuraSkills") == null) {
            plugin.getLogger().warning("AuraSkills não encontrado — objetivos skill_level ficarão inativos.");
            return;
        }
        try {
            api = AuraSkillsApi.get();
            enabled = api != null;
            if (enabled) {
                plugin.getLogger().info("AuraSkills API conectada.");
            }
        } catch (IllegalStateException ex) {
            plugin.getLogger().warning("AuraSkills API indisponível: " + ex.getMessage());
        }
    }

    @Override
    public void refresh() {
        enabled = false;
        api = null;
        enable();
    }

    public AuraSkillsApi getApi() {
        return api;
    }

    @Override
    public boolean addSkillXp(Player player, String skillName, double amount) {
        if (!enabled || player == null || skillName == null || amount <= 0) {
            return false;
        }
        Skill skill = resolveSkill(skillName);
        if (skill == null) {
            plugin.getLogger().warning("Skill AuraSkills desconhecida: " + skillName);
            return false;
        }
        SkillsUser user = api.getUser(player.getUniqueId());
        if (user == null) {
            return false;
        }
        user.addSkillXp(skill, amount);
        return true;
    }

    @Override
    public int getSkillLevel(Player player, String skillName) {
        if (!enabled || player == null || skillName == null) {
            return 0;
        }
        Skill skill = resolveSkill(skillName);
        if (skill == null) {
            return 0;
        }
        SkillsUser user = api.getUser(player.getUniqueId());
        return user == null ? 0 : user.getSkillLevel(skill);
    }

    @Override
    public boolean addStatModifier(Player player, String perkId, String statName, double value, String operation) {
        if (!enabled || player == null || perkId == null || statName == null) {
            return false;
        }
        Stat stat = resolveStat(statName);
        if (stat == null) {
            plugin.getLogger().warning("Stat AuraSkills desconhecido: " + statName);
            return false;
        }
        SkillsUser user = api.getUser(player.getUniqueId());
        if (user == null) {
            return false;
        }
        AuraSkillsModifier.Operation op = resolveOperation(operation);
        user.addStatModifier(new StatModifier(modifierId(perkId), stat, value, op));
        return true;
    }

    @Override
    public boolean removeStatModifier(Player player, String perkId) {
        if (!enabled || player == null || perkId == null) {
            return false;
        }
        SkillsUser user = api.getUser(player.getUniqueId());
        if (user == null) {
            return false;
        }
        user.removeStatModifier(modifierId(perkId));
        return true;
    }

    private Stat resolveStat(String statName) {
        try {
            return Stats.valueOf(statName.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private AuraSkillsModifier.Operation resolveOperation(String operation) {
        if (operation == null || operation.isBlank()) {
            return AuraSkillsModifier.Operation.ADD;
        }
        try {
            return AuraSkillsModifier.Operation.valueOf(operation.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return AuraSkillsModifier.Operation.ADD;
        }
    }

    private Skill resolveSkill(String skillName) {
        try {
            return Skills.valueOf(skillName.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
