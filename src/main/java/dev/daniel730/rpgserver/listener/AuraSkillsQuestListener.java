package dev.daniel730.rpgserver.listener;

import dev.aurelium.auraskills.api.event.skill.SkillLevelUpEvent;
import dev.aurelium.auraskills.api.skill.Skill;
import dev.daniel730.rpgserver.RpgServerPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public final class AuraSkillsQuestListener implements Listener {

    private final RpgServerPlugin plugin;

    public AuraSkillsQuestListener(RpgServerPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSkillLevelUp(SkillLevelUpEvent event) {
        Player player = event.getPlayer();
        Skill skill = event.getSkill();
        if (player == null || skill == null) {
            return;
        }
        plugin.getQuestManager().handleSkillLevelUp(player, skill.name().toLowerCase(), event.getLevel());
    }
}
