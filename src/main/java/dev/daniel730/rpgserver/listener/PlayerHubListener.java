package dev.daniel730.rpgserver.listener;

import dev.daniel730.rpgserver.RpgServerPlugin;
import dev.daniel730.rpgserver.gui.PlayerHubGui;
import dev.daniel730.rpgserver.gui.PlayerHubHolder;
import dev.daniel730.rpgserver.gui.QuestJournalGui;
import dev.daniel730.rpgserver.gui.RebirthGui;
import dev.daniel730.rpgserver.profile.PlayerProfile;
import dev.daniel730.rpgserver.quest.Quest;
import dev.daniel730.rpgserver.quest.QuestManager;
import dev.daniel730.rpgserver.quest.QuestProgressSync;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PlayerHubListener implements Listener {

    private final RpgServerPlugin plugin;
    private final Set<UUID> civsReturnToHub = ConcurrentHashMap.newKeySet();

    public PlayerHubListener(RpgServerPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onHubItemUse(PlayerInteractEvent event) {
        if (event.getItem() == null) {
            return;
        }
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        if (!plugin.getPlayerHubService().isHubItem(event.getItem())) {
            return;
        }
        event.setCancelled(true);
        Player player = event.getPlayer();
        if (player.isSneaking() && plugin.getCivsHook().openLocationsMenu(player)) {
            civsReturnToHub.add(player.getUniqueId());
            plugin.getQuestFeedbackService().playJournalOpen(player);
            return;
        }
        plugin.getPlayerHubService().openHub(player);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onCivsTopLevelBackClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        UUID uuid = player.getUniqueId();
        if (!civsReturnToHub.contains(uuid) || !plugin.getCivsHook().isEnabled()) {
            return;
        }
        if (!plugin.getCivsHook().isCivsMenuOpen(uuid)
                || plugin.getCivsHook().getCivsMenuHistorySize(uuid) > 1) {
            return;
        }
        if (event.getClickedInventory() == null
                || !event.getClickedInventory().equals(event.getView().getTopInventory())
                || event.getCurrentItem() == null) {
            return;
        }
        if (!plugin.getCivsHook().isCivsBackButtonClick(player, event.getCurrentItem())) {
            return;
        }
        event.setCancelled(true);
        civsReturnToHub.remove(uuid);
        player.closeInventory();
        Bukkit.getScheduler().runTask(plugin,
                () -> plugin.getPlayerHubService().openHub(player, PlayerHubHolder.HubTab.CIVS));
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory topInventory = event.getView().getTopInventory();
        if (!(topInventory.getHolder() instanceof PlayerHubHolder holder)) {
            return;
        }
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        if (event.getClickedInventory() == null || !event.getClickedInventory().equals(topInventory)) {
            return;
        }

        int slot = event.getRawSlot();
        PlayerHubHolder.HubClick click = holder.getAction(slot);
        if (click == null) {
            PlayerHubHolder.HubTab tab = PlayerHubHolder.HubTab.fromTabSlot(slot);
            if (tab != null && (tab != holder.getActiveTab() || holder.getScreen() != PlayerHubHolder.HubScreen.TAB)) {
                plugin.getQuestFeedbackService().playJournalClick(player);
                holder.resetNavigation(tab);
                PlayerHubGui.render(plugin, player, holder);
            }
            return;
        }

        plugin.getQuestFeedbackService().playJournalClick(player);
        handleAction(player, holder, click);
    }

    private void handleAction(Player player, PlayerHubHolder holder, PlayerHubHolder.HubClick click) {
        switch (click.action()) {
            case TAB -> {
                PlayerHubHolder.HubTab tab = PlayerHubHolder.HubTab.valueOf(click.payload());
                holder.resetNavigation(tab);
                PlayerHubGui.render(plugin, player, holder);
            }
            case OPEN_SUBVIEW -> {
                if (click.payload() != null && click.payload().startsWith("PATH_DETAIL:")) {
                    String questId = click.payload().substring("PATH_DETAIL:".length());
                    holder.setSelectedPathQuestId(questId);
                    holder.pushScreen(PlayerHubHolder.HubScreen.PATH_DETAIL);
                } else {
                    PlayerHubHolder.HubScreen screen = PlayerHubHolder.HubScreen.valueOf(click.payload());
                    holder.pushScreen(screen);
                }
                PlayerHubGui.render(plugin, player, holder);
            }
            case BACK -> {
                if (holder.popScreen()) {
                    PlayerHubGui.render(plugin, player, holder);
                }
            }
            case OPEN_CIVS_MENU -> openCivsMenu(player, click.payload());
            case COMMAND -> player.performCommand(stripSlash(click.payload()));
            case TOGGLE_NOTIFICATIONS -> {
                plugin.getQuestFeedbackService().toggleNotifications(player);
                PlayerHubGui.render(plugin, player, holder);
            }
            case TOGGLE_BOSSBAR -> {
                plugin.getQuestFeedbackService().toggleBossBar(player);
                PlayerHubGui.render(plugin, player, holder);
            }
            case OPEN_JOURNAL -> {
                player.closeInventory();
                QuestJournalGui.open(plugin, player);
                plugin.getQuestFeedbackService().playJournalOpen(player);
            }
            case OPEN_SKILL_TREE -> {
                player.closeInventory();
                player.performCommand("rpg tree");
            }
            case OPEN_CODEX -> {
                player.closeInventory();
                player.performCommand("rpg codex");
            }
            case TRACK_NEXT, TRACK_QUEST -> trackQuest(player, holder, click.payload());
            case ACCEPT_PATH -> trackQuest(player, holder, click.payload());
            case OPEN_REBIRTH -> {
                player.closeInventory();
                RebirthGui.open(plugin, player);
            }
            case SYNC -> syncQuests(player, holder);
            case CLOSE -> player.closeInventory();
            case REFRESH -> PlayerHubGui.render(plugin, player, holder);
        }
    }

    private void openCivsMenu(Player player, String menuRef) {
        if (menuRef == null || menuRef.isBlank() || !plugin.getCivsHook().isEnabled()) {
            return;
        }
        boolean opened;
        if ("select-town".equals(menuRef)) {
            opened = plugin.getCivsHook().openMenu(player, menuRef, Map.of(
                    "prevMenu", "town",
                    "uuid", player.getUniqueId().toString()));
        } else if (menuRef.contains("?") || menuRef.contains("$")) {
            opened = plugin.getCivsHook().openMenuFromString(player, menuRef);
        } else {
            opened = plugin.getCivsHook().openMenu(player, menuRef);
        }
        if (opened) {
            civsReturnToHub.add(player.getUniqueId());
            plugin.getQuestFeedbackService().playJournalOpen(player);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onCivsInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }
        UUID uuid = player.getUniqueId();
        if (!civsReturnToHub.contains(uuid)) {
            return;
        }
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (!plugin.getCivsHook().isEnabled()) {
                civsReturnToHub.remove(uuid);
                return;
            }
            if (!plugin.getCivsHook().isCivsMenuOpen(uuid) && !plugin.getPlayerHubService().isHubOpen(player)) {
                civsReturnToHub.remove(uuid);
            }
        });
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        civsReturnToHub.remove(event.getPlayer().getUniqueId());
    }

    private void syncQuests(Player player, PlayerHubHolder holder) {
        plugin.getQuestManager().ensureProfileSanitized(player);
        QuestProgressSync.SyncResult result = plugin.getQuestManager().getProgressSync()
                .sync(player, true, true);
        plugin.getMessageUtil().send(player, plugin.getPluginConfig().getQuestSyncSuccessOne()
                .replace("{player}", player.getName())
                .replace("{objectives}", String.valueOf(result.objectivesCompleted()))
                .replace("{quests}", String.valueOf(result.questsCompleted())));
        PlayerHubGui.render(plugin, player, holder);
    }

    private void trackQuest(Player player, PlayerHubHolder holder, String questId) {
        if (questId == null || questId.isBlank()) {
            QuestManager questManager = plugin.getQuestManager();
            PlayerProfile profile = plugin.getProfileManager().getOrCreate(player);
            questManager.findNextAvailableQuest(player, profile).ifPresentOrElse(
                    quest -> trackQuest(player, holder, quest.getId()),
                    () -> plugin.getMessageUtil().send(player,
                            plugin.getPluginConfig().getHubInicioNoQuest()));
            return;
        }

        Quest quest = plugin.getQuestManager().getQuest(questId);
        if (quest == null) {
            return;
        }
        PlayerProfile profile = plugin.getProfileManager().getOrCreate(player);
        QuestManager questManager = plugin.getQuestManager();
        QuestManager.QuestStatus status = questManager.getQuestStatus(player, profile, quest);

        if (status == QuestManager.QuestStatus.NOT_STARTED) {
            QuestManager.StartResult result = questManager.startQuest(player, profile, quest);
            if (result == QuestManager.StartResult.STARTED) {
                plugin.getQuestFeedbackService().playJournalAccept(player);
                plugin.getMessageUtil().send(player,
                        plugin.getPluginConfig().getQuestAcceptSuccess().replace("{quest}", quest.getName()));
            } else if (result == QuestManager.StartResult.ALREADY_ACTIVE) {
                doTrack(player, profile, quest, questManager);
            } else if (result == QuestManager.StartResult.ARCHETYPE_LOCKED) {
                plugin.getMessageUtil().send(player, plugin.getPluginConfig().getQuestAcceptArchetypeLocked());
            } else {
                plugin.getMessageUtil().send(player, plugin.getPluginConfig().getQuestAcceptLocked());
            }
        } else if (status == QuestManager.QuestStatus.IN_PROGRESS) {
            doTrack(player, profile, quest, questManager);
        } else if (status == QuestManager.QuestStatus.LOCKED) {
            plugin.getMessageUtil().send(player, plugin.getPluginConfig().getQuestAcceptLocked());
            return;
        } else {
            player.closeInventory();
            QuestJournalGui.open(plugin, player);
            plugin.getQuestFeedbackService().playJournalOpen(player);
            return;
        }
        PlayerHubGui.render(plugin, player, holder);
    }

    private void doTrack(Player player, PlayerProfile profile, Quest quest, QuestManager questManager) {
        if (quest.getId().equals(profile.getTrackedQuestId())) {
            plugin.getMessageUtil().send(player,
                    plugin.getPluginConfig().getQuestJournalAlreadyTracked()
                            .replace("{quest}", quest.getName()));
            return;
        }
        if (questManager.setTrackedQuest(player, profile, quest)) {
            plugin.getQuestFeedbackService().playJournalTrack(player);
            plugin.getMessageUtil().send(player,
                    plugin.getPluginConfig().getQuestTrackSuccess().replace("{quest}", quest.getName()));
        }
    }

    private static String stripSlash(String command) {
        if (command != null && command.startsWith("/")) {
            return command.substring(1);
        }
        return command;
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof PlayerHubHolder) {
            event.setCancelled(true);
        }
    }
}
