package dev.daniel730.rpgserver.hook;

import dev.daniel730.rpgserver.RpgServerPlugin;
import org.bukkit.entity.Player;

/**
 * No-op AuraSkills bridge that does not link the AuraSkills API. When AuraSkills is
 * installed, {@link SoftHookFactory} loads {@link AuraSkillsHookActive} instead.
 */
public class AuraSkillsHook {

    protected final RpgServerPlugin plugin;
    protected boolean enabled;

    public AuraSkillsHook(RpgServerPlugin plugin) {
        this.plugin = plugin;
    }

    public void enable() {
        enabled = false;
    }

    public void refresh() {
        enabled = false;
        enable();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean addSkillXp(Player player, String skillName, double amount) {
        return false;
    }

    public int getSkillLevel(Player player, String skillName) {
        return 0;
    }

    public boolean addStatModifier(Player player, String perkId, String statName, double value, String operation) {
        return false;
    }

    public boolean removeStatModifier(Player player, String perkId) {
        return false;
    }

    public static String modifierId(String perkId) {
        if (perkId == null) {
            return "rpg_unknown";
        }
        return perkId.startsWith("rpg_") ? perkId : "rpg_" + perkId;
    }
}
