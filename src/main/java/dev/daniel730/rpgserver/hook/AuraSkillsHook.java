package dev.daniel730.rpgserver.hook;

import dev.aurelium.auraskills.api.AuraSkillsApi;
import dev.aurelium.auraskills.api.skill.Skill;
import dev.aurelium.auraskills.api.skill.Skills;
import dev.aurelium.auraskills.api.user.SkillsUser;
import dev.daniel730.rpgserver.RpgServerPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Locale;

public final class AuraSkillsHook {

    private final RpgServerPlugin plugin;
    private AuraSkillsApi api;
    private boolean enabled;

    public AuraSkillsHook(RpgServerPlugin plugin) {
        this.plugin = plugin;
    }

    public void enable() {
        if (!plugin.getPluginConfig().isAuraSkillsEnabled()) {
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

    public boolean isEnabled() {
        return enabled;
    }

    public AuraSkillsApi getApi() {
        return api;
    }

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

    private Skill resolveSkill(String skillName) {
        try {
            return Skills.valueOf(skillName.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
