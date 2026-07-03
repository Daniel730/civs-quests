package dev.daniel730.rpgserver.listener;

import dev.daniel730.rpgserver.RpgServerPlugin;
import dev.daniel730.rpgserver.gui.QuestJournalGui;
import dev.daniel730.rpgserver.gui.QuestJournalHolder;
import dev.daniel730.rpgserver.profile.PlayerProfile;
import dev.daniel730.rpgserver.quest.Quest;
import dev.daniel730.rpgserver.quest.QuestManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;

public final class QuestJournalListener implements Listener {

    private final RpgServerPlugin plugin;

    public QuestJournalListener(RpgServerPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory topInventory = event.getView().getTopInventory();
        if (!(topInventory.getHolder() instanceof QuestJournalHolder holder)) {
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
        if (slot == QuestJournalHolder.NAV_PREV_SLOT && holder.getPage() > 0) {
            QuestJournalGui.render(plugin, player, holder, holder.getPage() - 1);
            return;
        }
        if (slot == QuestJournalHolder.NAV_NEXT_SLOT && holder.getPage() < holder.getTotalPages() - 1) {
            QuestJournalGui.render(plugin, player, holder, holder.getPage() + 1);
            return;
        }

        String questId = holder.getQuestId(slot);
        if (questId == null) {
            return;
        }
        Quest quest = plugin.getQuestManager().getQuest(questId);
        if (quest == null) {
            return;
        }

        PlayerProfile profile = plugin.getProfileManager().getOrCreate(player);
        QuestManager questManager = plugin.getQuestManager();
        QuestManager.QuestStatus status = questManager.getQuestStatus(player, profile, quest);

        boolean shiftClick = event.isShiftClick();

        if (shiftClick && status == QuestManager.QuestStatus.IN_PROGRESS) {
            handleAbandon(player, profile, quest, questManager, holder);
            return;
        }

        switch (status) {
            case NOT_STARTED -> handleStart(player, profile, quest, questManager, holder);
            case IN_PROGRESS -> handleTrack(player, profile, quest, questManager, holder);
            case COMPLETED -> plugin.getMessageUtil().send(player,
                    "<aqua>" + quest.getName() + "</aqua> <gray>já foi concluída.</gray>");
            case LOCKED -> plugin.getMessageUtil().send(player,
                    "<red>Esta quest está bloqueada.</red> <gray>Cumpra os requisitos para desbloquear.</gray>");
            default -> {
            }
        }
    }

    private void handleStart(Player player, PlayerProfile profile, Quest quest,
                             QuestManager questManager, QuestJournalHolder holder) {
        QuestManager.StartResult result = questManager.startQuest(player, profile, quest);
        switch (result) {
            case STARTED -> {
                plugin.getMessageUtil().send(player,
                        "<green>Quest aceita:</green> " + quest.getName()
                                + " <dark_gray>(rastreada)</dark_gray>");
                refresh(player, holder);
            }
            case LIMIT_REACHED -> plugin.getMessageUtil().send(player,
                    "<red>Limite de quests ativas atingido</red> <gray>("
                            + plugin.getPluginConfig().getMaxActiveQuests()
                            + "). Conclua ou abandone uma antes.</gray>");
            case NO_PERMISSION -> plugin.getMessageUtil().send(player,
                    "<red>Você ainda não desbloqueou esta quest.</red>");
            case REQUIREMENTS -> plugin.getMessageUtil().send(player,
                    "<red>Você não cumpre os pré-requisitos desta quest.</red>");
            case ALREADY_ACTIVE -> handleTrack(player, profile, quest, questManager, holder);
            case ALREADY_COMPLETE -> plugin.getMessageUtil().send(player,
                    "<aqua>Esta quest já foi concluída.</aqua>");
            default -> {
            }
        }
    }

    private void handleTrack(Player player, PlayerProfile profile, Quest quest,
                             QuestManager questManager, QuestJournalHolder holder) {
        if (quest.getId().equals(profile.getTrackedQuestId())) {
            plugin.getMessageUtil().send(player,
                    "<gray>" + quest.getName() + " já está sendo rastreada.</gray>");
            return;
        }
        if (questManager.setTrackedQuest(player, profile, quest)) {
            plugin.getMessageUtil().send(player,
                    "<gold>Rastreando quest:</gold> " + quest.getName());
            refresh(player, holder);
        }
    }

    private void handleAbandon(Player player, PlayerProfile profile, Quest quest,
                               QuestManager questManager, QuestJournalHolder holder) {
        if (!plugin.getPluginConfig().isAllowAbandon()) {
            plugin.getMessageUtil().send(player,
                    "<red>Abandonar quests está desabilitado neste servidor.</red>");
            return;
        }
        if (questManager.abandonQuest(player, profile, quest)) {
            plugin.getMessageUtil().send(player,
                    "<yellow>Quest abandonada:</yellow> " + quest.getName());
            refresh(player, holder);
        }
    }

    private void refresh(Player player, QuestJournalHolder holder) {
        plugin.getServer().getScheduler().runTask(plugin,
                () -> QuestJournalGui.render(plugin, player, holder, holder.getPage()));
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof QuestJournalHolder) {
            event.setCancelled(true);
        }
    }
}
