package dev.daniel730.rpgserver.progression;

import dev.daniel730.rpgserver.RpgServerPlugin;
import dev.daniel730.rpgserver.profile.PlayerProfile;
import org.bukkit.entity.Player;

import java.util.Set;

public final class RebirthService {

    private final RpgServerPlugin plugin;

    public RebirthService(RpgServerPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean canRebirth(PlayerProfile profile) {
        if (!plugin.getPluginConfig().isRebirthEnabled()) {
            return false;
        }
        for (String capstoneId : plugin.getPluginConfig().getRebirthCapstoneIds()) {
            if (profile.isQuestComplete(capstoneId)) {
                return true;
            }
        }
        return false;
    }

    public RebirthResult tryRebirth(Player player) {
        PlayerProfile profile = plugin.getProfileManager().getOrCreate(player);
        if (!canRebirth(profile)) {
            return RebirthResult.NOT_ELIGIBLE;
        }
        int refund = (int) Math.floor(profile.getEssenceSpent()
                * plugin.getPluginConfig().getRebirthEssenceRefundPercent());
        Set<String> keptPois = Set.copyOf(profile.getDiscoveredPois());
        Set<String> keptBiomes = Set.copyOf(profile.getDiscoveredBiomes());
        String legacyPerk = profile.getLegacyPerkId();

        for (String perkId : profile.getUnlockedPerkIds()) {
            plugin.getSkillTreeManager().revokePerk(player, perkId);
        }

        String oldArchetype = profile.getArchetype();
        for (String questId : Set.copyOf(profile.getActiveQuestIds())) {
            profile.stripInProgressState(questId);
        }
        for (String questId : Set.copyOf(profile.getStartedQuestIds())) {
            profile.stripInProgressState(questId);
        }
        profile.setArchetype(null);
        profile.setTrackedQuestId(null);
        profile.setUnlockedPerkIds(Set.of());
        profile.setLockedExclusiveGroups(Set.of());
        profile.setHubOpened(false);
        profile.setPathEssence(refund);
        profile.setEssenceSpent(0);
        profile.setRebirthCount(profile.getRebirthCount() + 1);
        profile.setLegacyPerkId(legacyPerk);
        profile.setDiscoveredPois(keptPois);
        profile.setDiscoveredBiomes(keptBiomes);

        plugin.getProfileManager().markDirty(player.getUniqueId());
        plugin.getQuestFeedbackService().refreshTrackedHud(player);
        plugin.getMessageUtil().send(player,
                "<gold>★ Renascimento " + profile.getRebirthCount() + "</gold>");
        if (refund > 0) {
            plugin.getMessageUtil().send(player,
                    "<light_purple>Essência reembolsada:</light_purple> <white>" + refund + "</white>");
        }
        if (oldArchetype != null) {
            plugin.getMessageUtil().send(player,
                    "<gray>Codex preservado. Escolha um novo caminho na Central.</gray>");
        }
        return RebirthResult.SUCCESS;
    }

    public enum RebirthResult {
        SUCCESS,
        NOT_ELIGIBLE
    }
}
