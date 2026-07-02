package dev.daniel730.rpgserver.hook;

import dev.aurelium.auraskills.api.AuraSkillsApi;
import dev.daniel730.rpgserver.RpgServerPlugin;
import org.bukkit.Bukkit;

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
}
