package dev.daniel730.rpgserver.command;

import dev.daniel730.rpgserver.RpgServerPlugin;
import dev.daniel730.rpgserver.gui.CodexGui;
import dev.daniel730.rpgserver.gui.QuestJournalGui;
import dev.daniel730.rpgserver.gui.RebirthGui;
import dev.daniel730.rpgserver.gui.SkillTreeGui;
import dev.daniel730.rpgserver.perk.PerkDefinition;
import dev.daniel730.rpgserver.profile.PlayerProfile;
import dev.daniel730.rpgserver.progression.SkillTreeManager;
import dev.daniel730.rpgserver.quest.Quest;
import dev.daniel730.rpgserver.quest.QuestAcceptResult;
import dev.daniel730.rpgserver.quest.QuestManager;
import dev.daniel730.rpgserver.quest.QuestProgressSync;
import dev.daniel730.rpgserver.discovery.PoiDefinition;
import org.bukkit.Bukkit;
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
            case "book" -> handleBookRedirect(sender);
            case "guide" -> handleGuide(sender, args);
            case "journal" -> handleJournal(sender);
            case "hub", "menu" -> handleHub(sender, args);
            case "settings" -> handleSettings(sender, args);
            case "perks" -> handlePerks(sender);
            case "tree" -> handleTree(sender);
            case "codex" -> handleCodex(sender);
            case "rebirth" -> handleRebirth(sender);
            case "poi" -> handlePoi(sender, args);
            case "sync" -> handleSync(sender, args);
            case "sanitize" -> handleSanitize(sender, args);
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
            plugin.getMessageUtil().send(player, "<yellow>Uso:</yellow> /rpg quest <list|status|accept|track>");
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
            case "book" -> {
                plugin.getMessageUtil().send(player,
                        "<gray>Os livros foram substituídos pelo diário GUI.</gray> <yellow>/rpg journal</yellow>");
                yield handleJournal(player);
            }
            case "journal" -> handleJournal(player);
            case "accept" -> {
                handleQuestAccept(player, args);
                yield true;
            }
            case "track" -> {
                handleQuestTrack(player, args);
                yield true;
            }
            default -> {
                plugin.getMessageUtil().send(player,
                        "<yellow>Uso:</yellow> /rpg quest <list|status|accept|track>");
                yield true;
            }
        };
    }

    private void handleQuestAccept(Player player, String[] args) {
        if (args.length < 3) {
            plugin.getMessageUtil().send(player, "<yellow>Uso:</yellow> /rpg quest accept <id>");
            return;
        }
        String questId = args[2];
        QuestAcceptResult result = plugin.getQuestManager().acceptQuest(player, questId);
        Quest quest = plugin.getQuestManager().getQuest(questId);
        String template = plugin.getPluginConfig().getQuestAcceptMessage(result);
        if (result == QuestAcceptResult.MAX_ACTIVE) {
            template = template.replace("{max}", String.valueOf(plugin.getPluginConfig().getMaxActiveQuests()));
        }
        String message = plugin.getQuestFeedbackService().formatAcceptMessage(template, quest);
        plugin.getMessageUtil().send(player, message);
    }

    private void handleQuestTrack(Player player, String[] args) {
        if (args.length < 3) {
            plugin.getMessageUtil().send(player, "<yellow>Uso:</yellow> /rpg quest track <id>");
            return;
        }
        Quest quest = plugin.getQuestManager().getQuest(args[2]);
        if (quest == null) {
            plugin.getMessageUtil().send(player, "<red>Quest não encontrada.</red>");
            return;
        }
        if (plugin.getQuestManager().trackQuest(player, args[2])) {
            plugin.getMessageUtil().send(player,
                    plugin.getQuestFeedbackService().formatAcceptMessage(
                            plugin.getPluginConfig().getQuestTrackSuccess(), quest));
        } else {
            plugin.getMessageUtil().send(player, plugin.getPluginConfig().getQuestTrackFailed());
        }
    }

    private boolean handleJournal(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Este comando só pode ser usado por jogadores.");
            return true;
        }
        if (!sender.hasPermission("rpg.quest")) {
            plugin.getMessageUtil().send(sender, plugin.getPluginConfig().getNoPermissionMessage());
            return true;
        }
        QuestJournalGui.open(plugin, player);
        plugin.getQuestFeedbackService().playJournalOpen(player);
        return true;
    }

    private boolean handleHub(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Este comando só pode ser usado por jogadores.");
            return true;
        }
        if (!sender.hasPermission("rpg.quest")) {
            plugin.getMessageUtil().send(sender, plugin.getPluginConfig().getNoPermissionMessage());
            return true;
        }
        if (args.length >= 2 && args[1].equalsIgnoreCase("refresh")) {
            plugin.getPlayerHubService().refreshHub(player);
            return true;
        }
        if (args.length >= 2 && args[1].equalsIgnoreCase("give")) {
            plugin.getPlayerHubService().giveHubItem(player);
            return true;
        }
        plugin.getPlayerHubService().openHub(player);
        return true;
    }

    private boolean handleBookRedirect(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Este comando só pode ser usado por jogadores.");
            return true;
        }
        if (!sender.hasPermission("rpg.quest")) {
            plugin.getMessageUtil().send(sender, plugin.getPluginConfig().getNoPermissionMessage());
            return true;
        }
        plugin.getMessageUtil().send(player,
                "<gray>Os livros foram substituídos pela Central do Reino.</gray> <yellow>/rpg hub</yellow>");
        plugin.getPlayerHubService().openHub(player);
        return true;
    }

    private boolean handleGuide(CommandSender sender, String[] args) {
        return handleHub(sender, args);
    }

    private boolean handleSettings(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Este comando só pode ser usado por jogadores.");
            return true;
        }
        if (!sender.hasPermission("rpg.profile")) {
            plugin.getMessageUtil().send(sender, plugin.getPluginConfig().getNoPermissionMessage());
            return true;
        }
        if (args.length < 2) {
            plugin.getMessageUtil().send(player,
                    "<yellow>Uso:</yellow> /rpg settings <notifications|bossbar> [on|off|toggle]");
            return true;
        }
        String setting = args[1].toLowerCase(Locale.ROOT);
        boolean toggle = args.length < 3 || args[2].equalsIgnoreCase("toggle");
        boolean enable = args.length >= 3 && (args[2].equalsIgnoreCase("on")
                || args[2].equalsIgnoreCase("ligar") || args[2].equalsIgnoreCase("true"));
        return switch (setting) {
            case "notifications", "notificacoes", "notify" -> {
                if (toggle) {
                    plugin.getQuestFeedbackService().toggleNotifications(player);
                } else {
                    setNotifications(player, enable);
                }
                yield true;
            }
            case "bossbar", "boss-bar", "hud" -> {
                if (toggle) {
                    plugin.getQuestFeedbackService().toggleBossBar(player);
                } else {
                    setBossBar(player, enable);
                }
                yield true;
            }
            default -> {
                plugin.getMessageUtil().send(player,
                        "<yellow>Uso:</yellow> /rpg settings <notifications|bossbar> [on|off|toggle]");
                yield true;
            }
        };
    }

    private void setNotifications(Player player, boolean enable) {
        PlayerProfile profile = plugin.getProfileManager().getOrCreate(player);
        profile.setNotificationsEnabled(enable);
        plugin.getProfileManager().markDirty(player.getUniqueId());
        plugin.getMessageUtil().send(player, enable
                ? plugin.getPluginConfig().getSettingsNotificationsOn()
                : plugin.getPluginConfig().getSettingsNotificationsOff());
        plugin.getQuestFeedbackService().refreshTrackedHud(player);
    }

    private void setBossBar(Player player, boolean enable) {
        PlayerProfile profile = plugin.getProfileManager().getOrCreate(player);
        profile.setBossBarEnabled(enable);
        plugin.getProfileManager().markDirty(player.getUniqueId());
        plugin.getMessageUtil().send(player, enable
                ? plugin.getPluginConfig().getSettingsBossBarOn()
                : plugin.getPluginConfig().getSettingsBossBarOff());
        plugin.getQuestFeedbackService().refreshTrackedHud(player);
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
                String progress = "";
                if (!done && objective.isCountBased()) {
                    int current = profile.getObjectiveProgress(quest.getId(), objective.getId());
                    progress = " <dark_gray>(" + current + "/" + objective.getAmount() + ")</dark_gray>";
                }
                plugin.getMessageUtil().send(player, "  " + mark + " " + objective.getDescription() + progress);
            }
        }
    }

    private boolean handlePerks(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Este comando só pode ser usado por jogadores.");
            return true;
        }
        if (!sender.hasPermission("rpg.perks")) {
            plugin.getMessageUtil().send(sender, plugin.getPluginConfig().getNoPermissionMessage());
            return true;
        }
        PlayerProfile profile = plugin.getProfileManager().getOrCreate(player);
        SkillTreeManager skillTree = plugin.getSkillTreeManager();
        plugin.getMessageUtil().send(player, "<gold>Perks</gold>");
        for (PerkDefinition perk : skillTree.getPerksForArchetype(profile.getArchetype())) {
            SkillTreeManager.PerkStatus status = skillTree.getPerkStatus(profile, perk);
            String color = switch (status) {
                case UNLOCKED -> "<green>";
                case AVAILABLE -> "<yellow>";
                case LOCKED, CHOICE_LOCKED -> "<gray>";
            };
            plugin.getMessageUtil().send(player,
                    color + perk.getName() + "</color> <dark_gray>[" + status.getDisplay() + "]</dark_gray>");
        }
        return true;
    }

    private boolean handleTree(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Este comando só pode ser usado por jogadores.");
            return true;
        }
        if (!sender.hasPermission("rpg.perks")) {
            plugin.getMessageUtil().send(sender, plugin.getPluginConfig().getNoPermissionMessage());
            return true;
        }
        SkillTreeGui.open(plugin, player);
        plugin.getQuestFeedbackService().playJournalOpen(player);
        return true;
    }

    private boolean handleCodex(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Este comando só pode ser usado por jogadores.");
            return true;
        }
        if (!sender.hasPermission("rpg.quest")) {
            plugin.getMessageUtil().send(sender, plugin.getPluginConfig().getNoPermissionMessage());
            return true;
        }
        CodexGui.open(plugin, player);
        plugin.getQuestFeedbackService().playJournalOpen(player);
        return true;
    }

    private boolean handleRebirth(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Este comando só pode ser usado por jogadores.");
            return true;
        }
        if (!sender.hasPermission("rpg.rebirth")) {
            plugin.getMessageUtil().send(sender, plugin.getPluginConfig().getNoPermissionMessage());
            return true;
        }
        if (!plugin.getPluginConfig().isRebirthEnabled()) {
            plugin.getMessageUtil().send(player, "<red>Renascimento desativado.</red>");
            return true;
        }
        if (!plugin.getRebirthService().canRebirth(plugin.getProfileManager().getOrCreate(player))) {
            plugin.getMessageUtil().send(player,
                    "<red>Conclua um capstone (Guerreiro, Mercador ou Construtor) para renascer.</red>");
            return true;
        }
        RebirthGui.open(plugin, player);
        return true;
    }

    private boolean handlePoi(CommandSender sender, String[] args) {
        if (!sender.hasPermission("rpg.admin")) {
            plugin.getMessageUtil().send(sender, plugin.getPluginConfig().getNoPermissionMessage());
            return true;
        }
        if (args.length < 2) {
            plugin.getMessageUtil().send(sender,
                    "<yellow>Uso:</yellow> /rpg poi mark <poi-id> [jogador] | /rpg poi export");
            return true;
        }
        String action = args[1].toLowerCase(Locale.ROOT);
        if ("export".equals(action)) {
            String yaml = plugin.getDiscoveryRegistry().exportYamlSnippet();
            plugin.getMessageUtil().send(sender, "<green>POIs exportados (" + plugin.getDiscoveryRegistry().getAllPois().size() + "):</green>");
            for (String line : yaml.split("\n")) {
                sender.sendMessage(line);
            }
            return true;
        }
        if (!"mark".equals(action) || args.length < 3) {
            plugin.getMessageUtil().send(sender,
                    "<yellow>Uso:</yellow> /rpg poi mark <poi-id> [jogador] | /rpg poi export");
            return true;
        }
        if (!(sender instanceof Player admin)) {
            sender.sendMessage("Especifique um jogador ou use o comando in-game para marcar coordenadas.");
            return true;
        }
        String poiId = args[2];
        PoiDefinition poi = plugin.getDiscoveryRegistry().upsertPoi(poiId, null, admin.getLocation(), 0);
        if (poi == null || !plugin.getDiscoveryRegistry().savePois()) {
            plugin.getMessageUtil().send(sender, "<red>Falha ao salvar POI:</red> " + poiId);
            return true;
        }
        plugin.getMessageUtil().send(sender,
                "<green>POI salvo em pois.yml:</green> <white>" + poi.getId()
                        + "</white> em <gray>" + Math.round(poi.getX()) + ", "
                        + Math.round(poi.getY()) + ", " + Math.round(poi.getZ()) + "</gray>");
        if (args.length >= 4) {
            Player target = Bukkit.getPlayer(args[3]);
            if (target == null) {
                plugin.getMessageUtil().send(sender, "<red>Jogador não encontrado.</red>");
                return true;
            }
            if (plugin.getDiscoveryService().markPoiDiscovered(target, poiId)) {
                plugin.getMessageUtil().send(sender,
                        "<green>Descoberta concedida a</green> <white>" + target.getName() + "</white>");
            }
        }
        return true;
    }

    private boolean handleSync(CommandSender sender, String[] args) {
        if (!sender.hasPermission("rpg.admin")) {
            plugin.getMessageUtil().send(sender, plugin.getPluginConfig().getNoPermissionMessage());
            return true;
        }
        boolean grantRewards = false;
        int playerArgIndex = 1;
        if (args.length > 1 && args[1].equalsIgnoreCase("--rewards")) {
            grantRewards = true;
            playerArgIndex = 2;
        }
        if (args.length <= playerArgIndex) {
            int totalObjectives = 0;
            int totalQuests = 0;
            int players = 0;
            for (Player online : Bukkit.getOnlinePlayers()) {
                plugin.getQuestManager().ensureProfileSanitized(online);
                QuestProgressSync.SyncResult result = plugin.getQuestManager().getProgressSync()
                        .sync(online, grantRewards, true);
                totalObjectives += result.objectivesCompleted();
                totalQuests += result.questsCompleted();
                players++;
            }
            plugin.getMessageUtil().send(sender,
                    plugin.getPluginConfig().getQuestSyncSuccessAll()
                            .replace("{players}", String.valueOf(players))
                            .replace("{objectives}", String.valueOf(totalObjectives))
                            .replace("{quests}", String.valueOf(totalQuests)));
            return true;
        }
        Player target = Bukkit.getPlayer(args[playerArgIndex]);
        if (target == null) {
            plugin.getMessageUtil().send(sender, "<red>Jogador não encontrado ou offline.</red>");
            return true;
        }
        plugin.getQuestManager().ensureProfileSanitized(target);
        QuestProgressSync.SyncResult result = plugin.getQuestManager().getProgressSync()
                .sync(target, grantRewards, true);
        plugin.getMessageUtil().send(sender,
                plugin.getPluginConfig().getQuestSyncSuccessOne()
                        .replace("{player}", target.getName())
                        .replace("{objectives}", String.valueOf(result.objectivesCompleted()))
                        .replace("{quests}", String.valueOf(result.questsCompleted())));
        return true;
    }

    private boolean handleSanitize(CommandSender sender, String[] args) {
        if (!sender.hasPermission("rpg.admin")) {
            plugin.getMessageUtil().send(sender, plugin.getPluginConfig().getNoPermissionMessage());
            return true;
        }
        if (args.length >= 2) {
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                plugin.getMessageUtil().send(sender, "<red>Jogador não encontrado ou offline.</red>");
                return true;
            }
            QuestManager.SanitizeResult result = plugin.getQuestManager().sanitizeProfile(
                    target, plugin.getProfileManager().getOrCreate(target));
            plugin.getMessageUtil().send(sender,
                    "<green>Perfil reparado para</green> <white>" + target.getName()
                            + "</white>: removidos inválidos=" + result.strippedInvalid()
                            + ", completos em ativo=" + result.strippedCompletedActive()
                            + ", excesso=" + result.demotedExcess());
            return true;
        }
        if (!(sender instanceof Player player)) {
            plugin.getMessageUtil().send(sender, "<red>Especifique um jogador: /rpg sanitize <jogador></red>");
            return true;
        }
        QuestManager.SanitizeResult result = plugin.getQuestManager().sanitizeProfile(
                player, plugin.getProfileManager().getOrCreate(player));
        plugin.getMessageUtil().send(player,
                "<green>Perfil reparado:</green> inválidos=" + result.strippedInvalid()
                        + ", completos em ativo=" + result.strippedCompletedActive()
                        + ", excesso=" + result.demotedExcess());
        return true;
    }

    private void sendHelp(CommandSender sender) {
        plugin.getMessageUtil().send(sender, "<gold>Comandos RPG</gold>");
        plugin.getMessageUtil().send(sender, "<gray>/rpg quest list</gray> — listar quests");
        plugin.getMessageUtil().send(sender, "<gray>/rpg quest status</gray> — progresso detalhado");
        plugin.getMessageUtil().send(sender, "<gray>/rpg quest accept &lt;id&gt;</gray> — aceitar quest");
        plugin.getMessageUtil().send(sender, "<gray>/rpg quest track &lt;id&gt;</gray> — rastrear quest");
        plugin.getMessageUtil().send(sender, "<gray>/rpg journal</gray> — diário de quests (GUI)");
        plugin.getMessageUtil().send(sender, "<gray>/rpg hub</gray> — central do reino (GUI)");
        plugin.getMessageUtil().send(sender, "<gray>/rpg hub give|refresh</gray> — item ou atualizar");
        plugin.getMessageUtil().send(sender, "<gray>/rpg guide</gray> — alias da central (GUI)");
        plugin.getMessageUtil().send(sender, "<gray>/rpg settings notifications|bossbar</gray> — preferências pessoais");
        plugin.getMessageUtil().send(sender, "<gray>/rpg perks</gray> — listar perks");
        plugin.getMessageUtil().send(sender, "<gray>/rpg tree</gray> — árvore de perks (GUI)");
        plugin.getMessageUtil().send(sender, "<gray>/rpg codex</gray> — codex de exploração");
        plugin.getMessageUtil().send(sender, "<gray>/rpg rebirth</gray> — renascimento (capstone)");
        plugin.getMessageUtil().send(sender, "<gray>/rpg profile</gray> — ver perfil");
        if (sender.hasPermission("rpg.admin")) {
            plugin.getMessageUtil().send(sender, "<gray>/rpg poi mark &lt;id&gt; [jogador]</gray> — salvar POI na posição atual");
            plugin.getMessageUtil().send(sender, "<gray>/rpg poi export</gray> — exportar POIs como YAML");
            plugin.getMessageUtil().send(sender, "<gray>/rpg reload</gray> — recarregar config");
            plugin.getMessageUtil().send(sender, "<gray>/rpg sync [jogador]</gray> — sincronizar progresso de quests");
            plugin.getMessageUtil().send(sender, "<gray>/rpg sync --rewards [jogador]</gray> — sync e conceder recompensas");
            plugin.getMessageUtil().send(sender, "<gray>/rpg sanitize [jogador]</gray> — reparar estado de quests corrompido");
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> options = new ArrayList<>(Arrays.asList(
                    "help", "quest", "book", "journal", "hub", "menu", "guide", "settings",
                    "perks", "tree", "codex", "rebirth", "profile"));
            if (sender.hasPermission("rpg.admin")) {
                options.add("reload");
                options.add("sync");
                options.add("sanitize");
            }
            return filterPrefix(options, args[0]);
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("sync") && sender.hasPermission("rpg.admin")) {
            List<String> syncOptions = new ArrayList<>(List.of("--rewards"));
            syncOptions.addAll(Bukkit.getOnlinePlayers().stream().map(Player::getName).toList());
            return filterPrefix(syncOptions, args[1]);
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("sync") && sender.hasPermission("rpg.admin")) {
            return filterPrefix(
                    Bukkit.getOnlinePlayers().stream().map(Player::getName).toList(),
                    args[2]);
        }
        if (args.length == 2 && (args[0].equalsIgnoreCase("guide")
                || args[0].equalsIgnoreCase("hub") || args[0].equalsIgnoreCase("menu"))) {
            return filterPrefix(List.of("give", "refresh"), args[1]);
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("settings")) {
            return filterPrefix(List.of("notifications", "bossbar", "toggle"), args[1]);
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("settings")) {
            return filterPrefix(List.of("on", "off", "toggle", "ligar", "desligar"), args[2]);
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("quest")) {
            return filterPrefix(List.of("list", "status", "book", "journal", "accept", "track"), args[1]);
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("quest")
                && (args[1].equalsIgnoreCase("accept") || args[1].equalsIgnoreCase("track"))) {
            return filterPrefix(questIdCompletions(), args[2]);
        }
        return List.of();
    }

    private List<String> questIdCompletions() {
        return plugin.getQuestManager().getAllQuests().stream().map(Quest::getId).toList();
    }

    private List<String> filterPrefix(List<String> options, String prefix) {
        String lower = prefix.toLowerCase(Locale.ROOT);
        return options.stream().filter(option -> option.startsWith(lower)).toList();
    }
}
