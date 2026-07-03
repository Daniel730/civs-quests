package dev.daniel730.rpgserver.quest;

import dev.daniel730.rpgserver.RpgServerPlugin;
import dev.daniel730.rpgserver.perk.PerkDefinition;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class RewardExecutor {

    private final RpgServerPlugin plugin;

    public RewardExecutor(RpgServerPlugin plugin) {
        this.plugin = plugin;
    }

    public void grantRewards(Player player, Quest quest) {
        RewardDefinition rewards = quest.getRewards();
        boolean hasPerkUnlock = quest.getUnlocksPerk() != null && !quest.getUnlocksPerk().isBlank();
        if ((rewards == null || rewards.isEmpty()) && !hasPerkUnlock) {
            return;
        }

        List<String> summaryLines = new ArrayList<>();
        if (rewards != null && !rewards.isEmpty()) {
            grantMoney(player, rewards, summaryLines);
            grantAuraSkillXp(player, rewards, summaryLines);
            grantCivsSkillXp(player, rewards, summaryLines);
            grantPermission(player, rewards, summaryLines);
            grantLpGroup(player, rewards, summaryLines);
            grantEssentials(player, rewards, summaryLines);
        }

        if (hasPerkUnlock) {
            grantPerkUnlock(player, quest.getUnlocksPerk(), summaryLines);
        }

        if (!summaryLines.isEmpty()) {
            plugin.getMessageUtil().send(player, plugin.getPluginConfig().getRewardSummaryHeader());
            for (String line : summaryLines) {
                plugin.getMessageUtil().send(player, line);
            }
        }
    }

    private void grantMoney(Player player, RewardDefinition rewards, List<String> summaryLines) {
        double amount = rewards.getMoney() * plugin.getPluginConfig().getRewardMoneyMultiplier();
        if (amount <= 0 || !plugin.getVaultHook().isEnabled()) {
            return;
        }
        if (plugin.getVaultHook().deposit(player, amount)) {
            summaryLines.add("<yellow>💰</yellow> <white>" + formatMoney(amount) + "</white>");
        }
    }

    private void grantAuraSkillXp(Player player, RewardDefinition rewards, List<String> summaryLines) {
        double multiplier = plugin.getPluginConfig().getRewardSkillXpMultiplier();
        for (Map.Entry<String, Double> entry : rewards.getSkillXp().entrySet()) {
            double amount = entry.getValue() * multiplier;
            if (amount <= 0) {
                continue;
            }
            if (plugin.getAuraSkillsHook().addSkillXp(player, entry.getKey(), amount)) {
                summaryLines.add("<aqua>⚔</aqua> <white>+" + formatXp(amount)
                        + " XP em " + displayAuraSkill(entry.getKey()) + "</white>");
            }
        }
    }

    private void grantCivsSkillXp(Player player, RewardDefinition rewards, List<String> summaryLines) {
        double multiplier = plugin.getPluginConfig().getRewardCivsSkillXpMultiplier();
        for (Map.Entry<String, Double> entry : rewards.getCivsSkillXp().entrySet()) {
            double amount = entry.getValue() * multiplier;
            if (amount <= 0) {
                continue;
            }
            if (plugin.getCivsHook().addSkillXp(player, entry.getKey(), amount)) {
                summaryLines.add("<green>🏗</green> <white>+" + formatXp(amount)
                        + " XP Civs em " + displayCivsSkill(entry.getKey()) + "</white>");
            }
        }
    }

    private void grantPermission(Player player, RewardDefinition rewards, List<String> summaryLines) {
        if (rewards.getPermission() == null || !plugin.getLuckPermsHook().isEnabled()) {
            return;
        }
        if (plugin.getLuckPermsHook().grantPermission(player, rewards.getPermission())) {
            summaryLines.add("<gray>🔓</gray> <white>Desbloqueio: " + rewards.getPermission() + "</white>");
        }
    }

    private void grantLpGroup(Player player, RewardDefinition rewards, List<String> summaryLines) {
        if (rewards.getLpGroup() == null || !plugin.getLuckPermsHook().isEnabled()) {
            return;
        }
        if (plugin.getLuckPermsHook().grantGroup(player, rewards.getLpGroup())) {
            summaryLines.add("<light_purple>★</light_purple> <white>Trilha: "
                    + displayLpGroup(rewards.getLpGroup()) + "</white>");
        }
    }

    private void grantEssentials(Player player, RewardDefinition rewards, List<String> summaryLines) {
        if (rewards.getEssentialsKit() != null) {
            if (plugin.getEssentialsHook().giveKit(player, rewards.getEssentialsKit())) {
                summaryLines.add("<gold>📦</gold> <white>Kit: " + rewards.getEssentialsKit() + "</white>");
            }
        }
        if (rewards.getWarp() != null) {
            if (plugin.getEssentialsHook().warpPlayer(player, rewards.getWarp())) {
                summaryLines.add("<blue>🌀</blue> <white>Teleporte: " + rewards.getWarp() + "</white>");
            }
        }
    }

    private void grantPerkUnlock(Player player, String perkId, List<String> summaryLines) {
        if (plugin.getSkillTreeManager().tryUnlock(player, perkId, false)) {
            PerkDefinition perk = plugin.getSkillTreeManager().getPerk(perkId);
            String name = perk != null ? perk.getName() : perkId;
            summaryLines.add("<light_purple>✦</light_purple> <gold>Perk desbloqueado:</gold> <white>" + name + "</white>");
        }
    }

    private String formatMoney(double amount) {
        if (plugin.getVaultHook().isEnabled() && plugin.getVaultHook().getEconomy() != null) {
            return plugin.getVaultHook().getEconomy().format(amount);
        }
        return String.format(Locale.ROOT, "%.0f moedas", amount);
    }

    private static String formatXp(double amount) {
        if (amount == Math.floor(amount)) {
            return String.format(Locale.ROOT, "%.0f", amount);
        }
        return String.format(Locale.ROOT, "%.1f", amount);
    }

    private static String displayAuraSkill(String skill) {
        return switch (skill.toLowerCase(Locale.ROOT)) {
            case "fighting" -> "Combate";
            case "farming" -> "Agricultura";
            case "foraging" -> "Coleta";
            case "mining" -> "Mineração";
            case "fishing" -> "Pesca";
            case "archery" -> "Arqueria";
            case "defense" -> "Defesa";
            case "agility" -> "Agilidade";
            case "excavation" -> "Escavação";
            case "enchanting" -> "Encantamento";
            case "alchemy" -> "Alquimia";
            default -> capitalize(skill);
        };
    }

    private static String displayCivsSkill(String skill) {
        return switch (skill.toLowerCase(Locale.ROOT)) {
            case "mining" -> "Mineração";
            case "building" -> "Construção";
            case "sword", "axe", "bow" -> capitalize(skill);
            default -> capitalize(skill);
        };
    }

    private static String displayLpGroup(String group) {
        return switch (group.toLowerCase(Locale.ROOT)) {
            case "rpg-warrior" -> "Guerreiro";
            case "rpg-builder" -> "Construtor";
            case "rpg-merchant" -> "Mercador";
            default -> group;
        };
    }

    private static String capitalize(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        String lower = value.toLowerCase(Locale.ROOT);
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }
}
