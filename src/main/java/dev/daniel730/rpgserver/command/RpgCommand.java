package dev.daniel730.rpgserver.command;

import dev.daniel730.rpgserver.RpgServerPlugin;
import dev.daniel730.rpgserver.profile.PlayerProfile;
import dev.daniel730.rpgserver.quest.Quest;
import dev.daniel730.rpgserver.quest.QuestManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public final class RpgCommand implements CommandExecutor, TabCompleter {

    private final RpgServerPlugin plugin;

    public RpgCommand(RpgServerPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);
        return switch (sub) {
            case "help" -> {
                sendHelp(sender);
                yield true;
            }
            case "reload" -> handleReload(sender);
            case "profile" -> handleProfile(sender);
            case "quest" -> handleQuest(sender, args);
            default -> {
                sendHelp(sender);
                yield true;
            }
        };
    }

    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission("rpg.admin")) {
            plugin.getMessageUtil().send(sender, plugin.getPluginConfig().getNoPermissionMessage());
            return true;
        }
        plugin.reloadPlugin();
        plugin.getMessageUtil().send(sender, plugin.getPluginConfig().getReloadSuccessMessage());
        return true;
    }

    private boolean handleProfile(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Este comando só pode ser usado por jogadores.");
            return true;
        }
        if (!sender.hasPermission("rpg.profile")) {
            plugin.getMessageUtil().send(sender, plugin.getPluginConfig().getNoPermissionMessage());
            return true;
        }
        PlayerProfile profile = plugin.getProfileManager().getOrCreate(player);
        String archetype = profile.getArchetype() == null ? "Nenhum" : profile.getArchetype();
        plugin.getMessageUtil().send(player, "<gold>Perfil RPG</gold>");
        plugin.getMessageUtil().send(player, "<gray>Arquetipo:</gray> " + archetype);
        Quest active = plugin.getQuestManager().findPrimaryActiveQuest(profile);
        plugin.getMessageUtil().send(player, "<gray>Quest ativa:</gray> "
                + (active == null ? "Nenhuma" : active.getName()));
        return true;
    }

    private boolean handleQuest(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Este comando só pode ser usado por jogadores.");
            return true;
        }
        if (!sender.hasPermission("rpg.quest")) {
            plugin.getMessageUtil().send(sender, plugin.getPluginConfig().getNoPermissionMessage());
            return true;
        }
        if (args.length < 2) {
            plugin.getMessageUtil().send(player, "<yellow>Uso:</yellow> /rpg quest <list|status>");
            return true;
        }
        String action = args[1].toLowerCase(Locale.ROOT);
        return switch (action) {
            case "list" -> {
                sendQuestList(player);
                yield true;
            }
            case "status" -> {
                sendQuestStatus(player);
                yield true;
            }
            default -> {
                plugin.getMessageUtil().send(player, "<yellow>Uso:</yellow> /rpg quest <list|status>");
                yield true;
            }
        };
    }

    private void sendQuestList(Player player) {
        PlayerProfile profile = plugin.getProfileManager().getOrCreate(player);
        QuestManager questManager = plugin.getQuestManager();
        plugin.getMessageUtil().send(player, "<gold>Quests disponíveis</gold>");
        for (Quest quest : questManager.getAllQuests()) {
            QuestManager.QuestStatus status = questManager.getQuestStatus(profile, quest);
            plugin.getMessageUtil().send(player,
                    "<gray>-</gray> " + quest.getName() + " <dark_gray>(" + status.getDisplay() + ")</dark_gray>");
        }
    }

    private void sendQuestStatus(Player player) {
        PlayerProfile profile = plugin.getProfileManager().getOrCreate(player);
        QuestManager questManager = plugin.getQuestManager();
        plugin.getMessageUtil().send(player, "<gold>Status das quests</gold>");
        for (Quest quest : questManager.getAllQuests()) {
            QuestManager.QuestStatus status = questManager.getQuestStatus(profile, quest);
            plugin.getMessageUtil().send(player,
                    "<yellow>" + quest.getName() + "</yellow> <dark_gray>[" + status.getDisplay() + "]</dark_gray>");
            for (Quest.Objective objective : quest.getObjectives()) {
                boolean done = profile.isObjectiveComplete(quest.getId(), objective.getId());
                String mark = done ? "<green>✓</green>" : "<red>○</red>";
                plugin.getMessageUtil().send(player, "  " + mark + " " + objective.getDescription());
            }
        }
    }

    private void sendHelp(CommandSender sender) {
        plugin.getMessageUtil().send(sender, "<gold>Comandos RPG</gold>");
        plugin.getMessageUtil().send(sender, "<gray>/rpg quest list</gray> — listar quests");
        plugin.getMessageUtil().send(sender, "<gray>/rpg quest status</gray> — progresso detalhado");
        plugin.getMessageUtil().send(sender, "<gray>/rpg profile</gray> — ver perfil");
        if (sender.hasPermission("rpg.admin")) {
            plugin.getMessageUtil().send(sender, "<gray>/rpg reload</gray> — recarregar config");
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> options = new ArrayList<>(Arrays.asList("help", "quest", "profile"));
            if (sender.hasPermission("rpg.admin")) {
                options.add("reload");
            }
            return filterPrefix(options, args[0]);
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("quest")) {
            return filterPrefix(List.of("list", "status"), args[1]);
        }
        return List.of();
    }

    private List<String> filterPrefix(List<String> options, String prefix) {
        String lower = prefix.toLowerCase(Locale.ROOT);
        return options.stream().filter(option -> option.startsWith(lower)).toList();
    }
}
