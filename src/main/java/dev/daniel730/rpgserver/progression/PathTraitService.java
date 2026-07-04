package dev.daniel730.rpgserver.progression;

import dev.daniel730.rpgserver.RpgServerPlugin;
import dev.daniel730.rpgserver.config.PluginConfig;
import dev.daniel730.rpgserver.quest.Quest;
import dev.daniel730.rpgserver.quest.QuestManager;
import org.bukkit.entity.Player;

import java.util.Locale;

public final class PathTraitService {

    private static final String TRAIT_MODIFIER_PREFIX = "rpg_trait_";

    private final RpgServerPlugin plugin;

    public PathTraitService(RpgServerPlugin plugin) {
        this.plugin = plugin;
    }

    public void applyPathTraits(Player player, String archetype) {
        if (player == null || archetype == null || archetype.isBlank()) {
            return;
        }
        if (!plugin.getAuraSkillsHook().isEnabled()) {
            return;
        }
        PluginConfig.PathTraitConfig trait = plugin.getPluginConfig().getPathTrait(archetype);
        if (trait == null) {
            return;
        }
        applyTrait(player, archetype, "buff", trait.buffStat(), trait.buffValue(), trait.buffOperation());
        applyTrait(player, archetype, "debuff", trait.debuffStat(), trait.debuffValue(), trait.debuffOperation());
    }

    public void reapplyPathTraits(Player player) {
        var profile = plugin.getProfileManager().getOrCreate(player);
        if (profile.getArchetype() != null && !profile.getArchetype().isBlank()) {
            applyPathTraits(player, profile.getArchetype());
        }
    }

    public void onPathQuestComplete(Player player, Quest quest) {
        if (!QuestManager.isPathQuest(quest)) {
            return;
        }
        applyPathTraits(player, quest.getArchetype());
        int essence = plugin.getPluginConfig().getPathEssencePerTier() * Math.max(1, quest.getTier());
        if (essence > 0) {
            var profile = plugin.getProfileManager().getOrCreate(player);
            profile.addPathEssence(essence);
            plugin.getProfileManager().markDirty(player.getUniqueId());
            plugin.getMessageUtil().send(player,
                    "<light_purple>✦</light_purple> <white>+" + essence + " Essência de Caminho</white>");
        }
    }

    private void applyTrait(Player player, String archetype, String kind,
                            String stat, double value, String operation) {
        if (stat == null || stat.isBlank() || value == 0) {
            return;
        }
        String modifierId = TRAIT_MODIFIER_PREFIX + archetype.toLowerCase(Locale.ROOT) + "_" + kind;
        plugin.getAuraSkillsHook().addStatModifier(player, modifierId, stat, value, operation);
    }
}
