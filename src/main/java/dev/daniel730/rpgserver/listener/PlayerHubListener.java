package dev.daniel730.rpgserver.listener;

import dev.daniel730.rpgserver.RpgServerPlugin;
import dev.daniel730.rpgserver.gui.PlayerHubGui;
import dev.daniel730.rpgserver.gui.PlayerHubHolder;
import dev.daniel730.rpgserver.gui.QuestJournalGui;
import dev.daniel730.rpgserver.profile.PlayerProfile;
import dev.daniel730.rpgserver.quest.Quest;
import dev.daniel730.rpgserver.quest.QuestManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;

public final class PlayerHubListener implements Listener {

    private final RpgServerPlugin plugin;

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
        plugin.getPlayerHubService().openHub(event.getPlayer());
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
            if (tab != null && tab != holder.getActiveTab()) {
                plugin.getQuestFeedbackService().playJournalClick(player);
                holder.setActiveTab(tab);
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
                holder.setActiveTab(tab);
                PlayerHubGui.render(plugin, player, holder);
            }
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
            case TRACK_NEXT, TRACK_QUEST -> trackQuest(player, holder, click.payload());
            case CLOSE -> player.closeInventory();
            case REFRESH -> PlayerHubGui.render(plugin, player, holder);
        }
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
            } else {
                plugin.getMessageUtil().send(player, plugin.getPluginConfig().getQuestAcceptLocked());
            }
        } else if (status == QuestManager.QuestStatus.IN_PROGRESS) {
            doTrack(player, profile, quest, questManager);
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
