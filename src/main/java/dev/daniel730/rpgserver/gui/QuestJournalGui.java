package dev.daniel730.rpgserver.gui;

import dev.daniel730.rpgserver.RpgServerPlugin;
import dev.daniel730.rpgserver.profile.PlayerProfile;
import dev.daniel730.rpgserver.quest.Quest;
import dev.daniel730.rpgserver.quest.QuestManager;
import dev.daniel730.rpgserver.util.MessageUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class QuestJournalGui {

    private static final int INVENTORY_SIZE = 54;
    private static final int CONTENT_START = 9;
    private static final int CONTENT_END = 44;
    public static final int QUESTS_PER_PAGE = CONTENT_END - CONTENT_START + 1;

    private QuestJournalGui() {
    }

    public static void open(RpgServerPlugin plugin, Player player) {
        QuestJournalHolder holder = new QuestJournalHolder();
        Inventory inventory = Bukkit.createInventory(holder, INVENTORY_SIZE,
                plugin.getMessageUtil().parse("<gold>Diário de Quests</gold>"));
        holder.setInventory(inventory);
        render(plugin, player, holder, 0);
        player.openInventory(inventory);
    }

    /**
     * (Re)builds the inventory contents for the given page. Safe to call while the
     * inventory is open — it mutates the existing inventory instead of reopening.
     */
    public static void render(RpgServerPlugin plugin, Player player, QuestJournalHolder holder, int page) {
        PlayerProfile profile = plugin.getProfileManager().getOrCreate(player);
        QuestManager questManager = plugin.getQuestManager();
        MessageUtil messages = plugin.getMessageUtil();
        Inventory inventory = holder.getInventory();

        inventory.clear();
        holder.clearSlotMappings();

        List<Quest> sorted = new ArrayList<>(questManager.getAllQuests());
        sorted.sort(Comparator
                .comparingInt((Quest quest) -> statusSortOrder(
                        questManager.getQuestStatus(player, profile, quest)))
                .thenComparing(Quest::getName, String.CASE_INSENSITIVE_ORDER));

        int totalPages = Math.max(1, (sorted.size() + QUESTS_PER_PAGE - 1) / QUESTS_PER_PAGE);
        int clampedPage = Math.max(0, Math.min(page, totalPages - 1));
        holder.setPage(clampedPage);
        holder.setTotalPages(totalPages);

        inventory.setItem(4, createHeaderItem(messages, clampedPage, totalPages));

        int startIndex = clampedPage * QUESTS_PER_PAGE;
        int slot = CONTENT_START;
        for (int i = startIndex; i < sorted.size() && slot <= CONTENT_END; i++, slot++) {
            Quest quest = sorted.get(i);
            QuestManager.QuestStatus status = questManager.getQuestStatus(player, profile, quest);
            boolean tracked = quest.getId().equals(profile.getTrackedQuestId());
            inventory.setItem(slot, createQuestItem(plugin, player, profile, quest, status, tracked));
            holder.mapSlot(slot, quest.getId());
        }

        if (clampedPage > 0) {
            inventory.setItem(QuestJournalHolder.NAV_PREV_SLOT, createNavItem(messages, false, clampedPage, totalPages));
        }
        if (clampedPage < totalPages - 1) {
            inventory.setItem(QuestJournalHolder.NAV_NEXT_SLOT, createNavItem(messages, true, clampedPage, totalPages));
        }
    }

    private static ItemStack createHeaderItem(MessageUtil messages, int page, int totalPages) {
        ItemStack item = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(messages.parse("<gold><bold>Diário de Quests</bold></gold>"));
        List<Component> lore = new ArrayList<>();
        lore.add(messages.parse("<gray>Quests ordenadas por status.</gray>"));
        lore.add(messages.parse("<green>Em progresso</green> <dark_gray>·</dark_gray> "
                + "<yellow>Não iniciada</yellow> <dark_gray>·</dark_gray> "
                + "<red>Bloqueada</red> <dark_gray>·</dark_gray> "
                + "<aqua>Concluída</aqua>"));
        lore.add(Component.empty());
        lore.add(messages.parse("<yellow>Clique</yellow> <gray>numa quest não iniciada para aceitar.</gray>"));
        lore.add(messages.parse("<yellow>Clique</yellow> <gray>numa quest em progresso para rastrear.</gray>"));
        lore.add(messages.parse("<yellow>Shift-clique</yellow> <gray>para abandonar.</gray>"));
        if (totalPages > 1) {
            lore.add(Component.empty());
            lore.add(messages.parse("<dark_gray>Página " + (page + 1) + "/" + totalPages + "</dark_gray>"));
        }
        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack createNavItem(MessageUtil messages, boolean next, int page, int totalPages) {
        ItemStack item = new ItemStack(next ? Material.SPECTRAL_ARROW : Material.ARROW);
        ItemMeta meta = item.getItemMeta();
        if (next) {
            meta.displayName(messages.parse("<green>Próxima página »</green>"));
        } else {
            meta.displayName(messages.parse("<green>« Página anterior</green>"));
        }
        meta.lore(List.of(messages.parse("<dark_gray>Página " + (page + 1) + "/" + totalPages + "</dark_gray>")));
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack createQuestItem(RpgServerPlugin plugin, Player player, PlayerProfile profile,
                                             Quest quest, QuestManager.QuestStatus status, boolean tracked) {
        MessageUtil messages = plugin.getMessageUtil();
        ItemStack item = new ItemStack(materialForStatus(status));
        ItemMeta meta = item.getItemMeta();
        meta.displayName(messages.parse(statusColor(status) + quest.getName() + "</" + statusTag(status) + ">"));

        List<Component> lore = new ArrayList<>();
        QuestManager questManager = plugin.getQuestManager();

        if (quest.getDescription() != null && !quest.getDescription().isBlank()) {
            lore.add(messages.parse("<gray>" + truncate(quest.getDescription(), 48) + "</gray>"));
        }
        if (quest.isScheduled()) {
            lore.add(messages.parse("<light_purple>Tipo:</light_purple> <white>"
                    + quest.getSchedule().displayName() + "</white>"));
        }
        lore.add(messages.parse("<dark_gray>Status:</dark_gray> " + statusColor(status) + status.getDisplay()
                + "</" + statusTag(status) + ">"));

        if (tracked) {
            lore.add(messages.parse("<gold>➤ Quest rastreada</gold>"));
        }

        if (status == QuestManager.QuestStatus.LOCKED) {
            appendLockedInfo(plugin, profile, quest, lore, messages, questManager, player);
        } else if (status != QuestManager.QuestStatus.COMPLETED) {
            appendObjectives(profile, quest, lore, messages);
        } else {
            lore.add(messages.parse("<green>Todos os objetivos concluídos.</green>"));
        }

        QuestManager.QuestProgress progress = questManager.getQuestProgress(profile, quest);
        if (progress.total() > 0 && status != QuestManager.QuestStatus.LOCKED) {
            lore.add(messages.parse("<dark_gray>Progresso:</dark_gray> <white>"
                    + progress.completed() + "/" + progress.total() + "</white>"));
        }

        appendActionHint(plugin, lore, messages, status, tracked);

        meta.lore(lore);
        if (tracked) {
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        item.setItemMeta(meta);
        return item;
    }

    private static void appendActionHint(RpgServerPlugin plugin, List<Component> lore, MessageUtil messages,
                                         QuestManager.QuestStatus status, boolean tracked) {
        lore.add(Component.empty());
        switch (status) {
            case NOT_STARTED ->
                    lore.add(messages.parse("<yellow>▶ Clique para aceitar a quest.</yellow>"));
            case IN_PROGRESS -> {
                if (!tracked) {
                    lore.add(messages.parse("<yellow>▶ Clique para rastrear esta quest.</yellow>"));
                }
                if (plugin.getPluginConfig().isAllowAbandon()) {
                    lore.add(messages.parse("<red>▶ Shift-clique para abandonar.</red>"));
                }
            }
            case COMPLETED ->
                    lore.add(messages.parse("<aqua>✔ Quest concluída.</aqua>"));
            case LOCKED ->
                    lore.add(messages.parse("<dark_gray>Bloqueada — veja os requisitos acima.</dark_gray>"));
            default -> {
            }
        }
    }

    private static void appendLockedInfo(RpgServerPlugin plugin, PlayerProfile profile, Quest quest,
                                         List<Component> lore, MessageUtil messages, QuestManager questManager,
                                         Player player) {
        if (!questManager.meetsRequirements(profile, quest)) {
            lore.add(messages.parse("<red>Requer:</red>"));
            for (String requiredId : quest.getRequiredQuestIds()) {
                Quest required = questManager.getQuest(requiredId);
                String name = required != null ? required.getName() : requiredId;
                boolean done = profile.isQuestComplete(requiredId);
                String mark = done ? "<green>✓</green>" : "<red>✗</red>";
                lore.add(messages.parse(" " + mark + " <gray>" + name + "</gray>"));
            }
        }
        if (!plugin.getLuckPermsHook().hasQuestPermission(player, quest.getId())) {
            lore.add(messages.parse("<red>Requer desbloqueio (permissão).</red>"));
        }
    }

    private static void appendObjectives(PlayerProfile profile, Quest quest, List<Component> lore,
                                         MessageUtil messages) {
        lore.add(messages.parse("<yellow>Objetivos:</yellow>"));
        for (Quest.Objective objective : quest.getObjectives()) {
            boolean done = profile.isObjectiveComplete(quest.getId(), objective.getId());
            String mark = done ? "<green>✓</green>" : "<red>○</red>";
            String line = " " + mark + " <gray>" + truncate(objective.getDescription(), 40) + "</gray>";
            if (!done && objective.isCountBased()) {
                int current = profile.getObjectiveProgress(quest.getId(), objective.getId());
                line += " <dark_gray>(" + current + "/" + objective.getAmount() + ")</dark_gray>";
            }
            lore.add(messages.parse(line));
        }
    }

    private static Material materialForStatus(QuestManager.QuestStatus status) {
        return switch (status) {
            case IN_PROGRESS -> Material.WRITABLE_BOOK;
            case NOT_STARTED -> Material.BOOK;
            case LOCKED -> Material.BARRIER;
            case COMPLETED -> Material.ENCHANTED_BOOK;
        };
    }

    private static int statusSortOrder(QuestManager.QuestStatus status) {
        return switch (status) {
            case IN_PROGRESS -> 0;
            case NOT_STARTED -> 1;
            case LOCKED -> 2;
            case COMPLETED -> 3;
        };
    }

    private static String statusColor(QuestManager.QuestStatus status) {
        return switch (status) {
            case IN_PROGRESS -> "<green>";
            case NOT_STARTED -> "<yellow>";
            case LOCKED -> "<red>";
            case COMPLETED -> "<aqua>";
        };
    }

    private static String statusTag(QuestManager.QuestStatus status) {
        return switch (status) {
            case IN_PROGRESS -> "green";
            case NOT_STARTED -> "yellow";
            case LOCKED -> "red";
            case COMPLETED -> "aqua";
        };
    }

    private static String truncate(String text, int maxLength) {
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength - 3) + "...";
    }
}
