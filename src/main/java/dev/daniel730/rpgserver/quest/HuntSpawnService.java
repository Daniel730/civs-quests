package dev.daniel730.rpgserver.quest;

import dev.daniel730.rpgserver.RpgServerPlugin;
import org.bukkit.entity.Player;

public final class HuntSpawnService {

    private final RpgServerPlugin plugin;

    public HuntSpawnService(RpgServerPlugin plugin) {
        this.plugin = plugin;
    }

    public void spawnOnQuestAccept(Player player, Quest quest) {
        if (!plugin.getCivsHook().isEnabled() || player == null || quest == null) {
            return;
        }
        for (Quest.Objective objective : quest.getObjectives()) {
            if (!objective.isSpawnOnAccept()) {
                continue;
            }
            if (!objective.getTypeId().equals("custom_mob_kill")) {
                continue;
            }
            String mobId = objective.getMob();
            if (mobId == null || mobId.isBlank()) {
                continue;
            }
            String cooldownKey = quest.getId() + ":" + objective.getId();
            if (isOnCooldown(player, cooldownKey)) {
                plugin.getMessageUtil().send(player,
                        "<gray>Caçada em recarga — aguarde antes de aceitar novamente.</gray>");
                continue;
            }
            double radius = plugin.getPluginConfig().getHuntSpawnPartyRadius();
            if (plugin.getCivsHook().spawnQuestMob(player, mobId, radius)) {
                setCooldown(player, cooldownKey);
                plugin.getMessageUtil().send(player,
                        "<red>⚔</red> <white>Alvo apareceu:</white> <yellow>" + mobId + "</yellow>");
            } else {
                plugin.getMessageUtil().send(player,
                        "<red>Não foi possível invocar o alvo da caçada.</red>");
            }
        }
    }

    private boolean isOnCooldown(Player player, String key) {
        var profile = plugin.getProfileManager().getOrCreate(player);
        Long until = profile.getHuntCooldown(key);
        return until != null && until > System.currentTimeMillis();
    }

    private void setCooldown(Player player, String key) {
        var profile = plugin.getProfileManager().getOrCreate(player);
        long seconds = plugin.getPluginConfig().getHuntSpawnCooldownSeconds();
        profile.setHuntCooldown(key, System.currentTimeMillis() + seconds * 1000L);
        plugin.getProfileManager().markDirty(player.getUniqueId());
    }
}
