package dev.daniel730.rpgserver.quest;

import dev.daniel730.rpgserver.RpgServerPlugin;
import org.bukkit.entity.Player;

import java.util.Map;

public final class RewardExecutor {

    private final RpgServerPlugin plugin;

    public RewardExecutor(RpgServerPlugin plugin) {
        this.plugin = plugin;
    }

    public void grantRewards(Player player, Quest quest) {
        RewardDefinition rewards = quest.getRewards();
        if (rewards == null || rewards.isEmpty()) {
            return;
        }

        if (rewards.getMoney() > 0 && plugin.getVaultHook().isEnabled()) {
            plugin.getVaultHook().deposit(player, rewards.getMoney());
            plugin.getMessageUtil().send(player,
                    "<green>Recompensa:</green> " + rewards.getMoney() + " moedas");
        }

        for (Map.Entry<String, Double> entry : rewards.getSkillXp().entrySet()) {
            if (plugin.getAuraSkillsHook().addSkillXp(player, entry.getKey(), entry.getValue())) {
                plugin.getMessageUtil().send(player,
                        "<green>Recompensa:</green> +" + entry.getValue().intValue()
                                + " XP em " + entry.getKey());
            }
        }

        if (rewards.getPermission() != null && plugin.getLuckPermsHook().isEnabled()) {
            if (plugin.getLuckPermsHook().grantPermission(player, rewards.getPermission())) {
                plugin.getMessageUtil().send(player,
                        "<green>Desbloqueado:</green> " + rewards.getPermission());
            }
        }

        if (rewards.getEssentialsKit() != null) {
            plugin.getEssentialsHook().giveKit(player, rewards.getEssentialsKit());
        }

        if (rewards.getWarp() != null) {
            plugin.getEssentialsHook().warpPlayer(player, rewards.getWarp());
        }
    }
}
