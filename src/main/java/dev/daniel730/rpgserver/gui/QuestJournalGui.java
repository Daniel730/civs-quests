package dev.daniel730.rpgserver.gui;

import dev.daniel730.rpgserver.RpgServerPlugin;
import dev.daniel730.rpgserver.config.PluginConfig;
import dev.daniel730.rpgserver.profile.PlayerProfile;
import dev.daniel730.rpgserver.quest.Quest;
import dev.daniel730.rpgserver.quest.QuestManager;
import dev.daniel730.rpgserver.quest.QuestSchedule;
import dev.daniel730.rpgserver.util.ArchetypeUtil;
import dev.daniel730.rpgserver.util.MessageUtil;
import dev.daniel730.rpgserver.util.ProgressBarUtil;
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
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class QuestJournalGui {

    private static final int INVENTORY_SIZE = 54;
    private static final int CONTENT_START = 9;
    private static final int CONTENT_END = 44;
    public static final int QUESTS_PER_PAGE = CONTENT_END - CONTENT_START + 1;
    public static final int PAGE_INDICATOR_SLOT = 49;

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

    public static void render(RpgServerPlugin plugin, Player player, QuestJournalHolder holder, int page) {
        PlayerProfile profile = plugin.getProfileManager().getOrCreate(player);
        QuestManager questManager = plugin.getQuestManager();
        MessageUtil messages = plugin.getMessageUtil();
        PluginConfig config = plugin.getPluginConfig();
        Inventory inventory = holder.getInventory();

        inventory.clear();
        holder.clearSlotMappings();

        List<JournalEntry> entries = buildJournalEntries(plugin, player, profile, questManager);
        int totalPages = Math.max(1, (entries.size() + QUESTS_PER_PAGE - 1) / QUESTS_PER_PAGE);
        int clampedPage = Math.max(0, Math.min(page, totalPages - 1));
        holder.setPage(clampedPage);
        holder.setTotalPages(totalPages);

        fillBorder(inventory, profile.getArchetype());
        inventory.setItem(4, createHeaderItem(messages, config, profile, clampedPage, totalPages));

        int startIndex = clampedPage * QUESTS_PER_PAGE;
        int slot = CONTENT_START;
        for (int i = startIndex; i < entries.size() && slot <= CONTENT_END; i++, slot++) {
            JournalEntry entry = entries.get(i);
            if (entry instanceof SectionHeader section) {
                inventory.setItem(slot, createSectionHeader(messages, section));
            } else if (entry instanceof QuestSlot questSlot) {
                Quest quest = questSlot.quest();
                QuestManager.QuestStatus status = questManager.getQuestStatus(player, profile, quest);
                boolean tracked = quest.getId().equals(profile.getTrackedQuestId());
                inventory.setItem(slot, createQuestItem(plugin, player, profile, quest, status, tracked));
                holder.mapSlot(slot, quest.getId());
            }
        }

        if (totalPages > 1) {
            inventory.setItem(PAGE_INDICATOR_SLOT, createPageIndicator(messages, config, clampedPage, totalPages));
            renderPageDots(inventory, clampedPage, totalPages);
        }

        if (clampedPage > 0) {
            inventory.setItem(QuestJournalHolder.NAV_PREV_SLOT, createNavItem(messages, false, clampedPage, totalPages));
        }
        if (clampedPage < totalPages - 1) {
            inventory.setItem(QuestJournalHolder.NAV_NEXT_SLOT, createNavItem(messages, true, clampedPage, totalPages));
        }
    }

    private static List<JournalEntry> buildJournalEntries(RpgServerPlugin plugin, Player player,
                                                          PlayerProfile profile, QuestManager questManager) {
        List<JournalEntry> entries = new ArrayList<>();
        Set<String> placed = new HashSet<>();
        String archetype = profile.getArchetype();

        if (archetype == null || archetype.isBlank()) {
            List<Quest> pathQuests = questManager.getPathQuests(player, profile);
            if (!pathQuests.isEmpty()) {
                entries.add(new SectionHeader("Escolha seu caminho", Material.MAP));
                for (Quest quest : pathQuests) {
                    entries.add(new QuestSlot(quest));
                    placed.add(quest.getId());
                }
            }
        } else {
            List<Quest> storyQuests = questManager.getArchetypeStoryQuests(player, profile, archetype);
            if (!storyQuests.isEmpty()) {
                entries.add(new SectionHeader("Seu Caminho — " + archetypeDisplay(archetype), Material.SHIELD));
                for (Quest quest : storyQuests) {
                    entries.add(new QuestSlot(quest));
                    placed.add(quest.getId());
                }
            }
        }

        List<Quest> dailies = questManager.getScheduledQuests(player, profile, QuestSchedule.DAILY);
        if (!dailies.isEmpty()) {
            entries.add(new SectionHeader("Missões Diárias", Material.SUNFLOWER));
            for (Quest quest : dailies) {
                entries.add(new QuestSlot(quest));
                placed.add(quest.getId());
            }
        }

        List<Quest> weeklies = questManager.getScheduledQuests(player, profile, QuestSchedule.WEEKLY);
        if (!weeklies.isEmpty()) {
            entries.add(new SectionHeader("Missões Semanais", Material.CLOCK));
            for (Quest quest : weeklies) {
                entries.add(new QuestSlot(quest));
                placed.add(quest.getId());
            }
        }

        List<Quest> misc = questManager.getMiscQuests(player, profile, placed);
        if (!misc.isEmpty()) {
            entries.add(new SectionHeader("Expansão Territorial", Material.GRASS_BLOCK));
            for (Quest quest : misc) {
                entries.add(new QuestSlot(quest));
            }
        }

        return entries;
    }

    private static String archetypeDisplay(String archetype) {
        return switch (archetype.toLowerCase(Locale.ROOT)) {
            case "warrior" -> "Guerreiro";
            case "merchant" -> "Mercador";
            case "builder" -> "Construtor";
            default -> archetype.substring(0, 1).toUpperCase(Locale.ROOT) + archetype.substring(1);
        };
    }

    private sealed interface JournalEntry permits SectionHeader, QuestSlot {
    }

    private record SectionHeader(String title, Material icon) implements JournalEntry {
    }

    private record QuestSlot(Quest quest) implements JournalEntry {
    }

    private static void fillBorder(Inventory inventory, String playerArchetype) {
        ItemStack filler = createFiller(ArchetypeUtil.glassPane(playerArchetype));
        for (int slot = 0; slot < 9; slot++) {
            if (slot == 1) {
                inventory.setItem(slot, createArchetypeLegend(messagesFromSlot(inventory), "warrior"));
            } else if (slot == 3) {
                inventory.setItem(slot, createArchetypeLegend(messagesFromSlot(inventory), "merchant"));
            } else if (slot == 5) {
                inventory.setItem(slot, createArchetypeLegend(messagesFromSlot(inventory), "builder"));
            } else if (slot != 4) {
                inventory.setItem(slot, filler);
            }
        }
        for (int row = 1; row <= 4; row++) {
            inventory.setItem(row * 9, filler);
            inventory.setItem(row * 9 + 8, filler);
        }
        for (int slot = 45; slot < 54; slot++) {
            if (slot != QuestJournalHolder.NAV_PREV_SLOT
                    && slot != QuestJournalHolder.NAV_NEXT_SLOT
                    && slot != PAGE_INDICATOR_SLOT) {
                inventory.setItem(slot, filler);
            }
        }
    }

    private static MessageUtil messagesFromSlot(Inventory inventory) {
        return RpgServerPlugin.getInstance().getMessageUtil();
    }

    private static ItemStack createArchetypeLegend(MessageUtil messages, String archetype) {
        ItemStack item = new ItemStack(ArchetypeUtil.glassPane(archetype));
        ItemMeta meta = item.getItemMeta();
        meta.displayName(messages.parse(ArchetypeUtil.coloredDisplayName(archetype)));
        meta.lore(List.of(messages.parse("<dark_gray>Arquetipo</dark_gray>")));
        item.setItemMeta(meta);
        return item;
    }

    private static void renderPageDots(Inventory inventory, int page, int totalPages) {
        int[] dotSlots = {46, 47, 48, 50, 51, 52};
        int maxDots = Math.min(dotSlots.length, totalPages);
        for (int i = 0; i < maxDots; i++) {
            Material material = i == page ? Material.LIME_STAINED_GLASS_PANE : Material.GRAY_STAINED_GLASS_PANE;
            inventory.setItem(dotSlots[i], createFiller(material));
        }
    }

    private static ItemStack createFiller(Material material) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.empty());
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack createSectionHeader(MessageUtil messages, SectionHeader section) {
        ItemStack item = new ItemStack(section.icon());
        ItemMeta meta = item.getItemMeta();
        meta.displayName(messages.parse("<gold><bold>" + section.title() + "</bold></gold>"));
        meta.lore(List.of(messages.parse("<dark_gray>━━━━━━━━━━━━━━━━</dark_gray>")));
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack createHeaderItem(MessageUtil messages, PluginConfig config, PlayerProfile profile,
                                              int page, int totalPages) {
        ItemStack item = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(messages.parse("<gold><bold>Diário de Quests</bold></gold>"));
        List<Component> lore = new ArrayList<>();
        if (profile.getArchetype() == null || profile.getArchetype().isBlank()) {
            lore.add(messages.parse("<yellow>Escolha um caminho para começar sua lenda territorial.</yellow>"));
        } else {
            lore.add(messages.parse("<gray>Arquétipo:</gray> <white>"
                    + archetypeDisplay(profile.getArchetype()) + "</white>"));
        }
        lore.add(messages.parse("<green>Em progresso</green> <dark_gray>·</dark_gray> "
                + "<yellow>Disponível</yellow> <dark_gray>·</dark_gray> "
                + "<red>Bloqueada</red> <dark_gray>·</dark_gray> "
                + "<aqua>Concluída</aqua>"));
        lore.add(Component.empty());
        lore.add(messages.parse(config.getQuestJournalAcceptHint()));
        lore.add(messages.parse(config.getQuestJournalTrackHint()));
        lore.add(messages.parse(config.getQuestJournalAbandonHint()));
        if (totalPages > 1) {
            lore.add(Component.empty());
            lore.add(messages.parse(pageIndicatorText(config, page, totalPages)));
        }
        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack createPageIndicator(MessageUtil messages, PluginConfig config, int page, int totalPages) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(messages.parse(pageIndicatorText(config, page, totalPages)));
        meta.lore(List.of(messages.parse("<gray>Use as setas para navegar.</gray>")));
        item.setItemMeta(meta);
        return item;
    }

    private static String pageIndicatorText(PluginConfig config, int page, int totalPages) {
        return config.getQuestJournalPageIndicator()
                .replace("{page}", String.valueOf(page + 1))
                .replace("{total}", String.valueOf(totalPages));
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
        PluginConfig config = plugin.getPluginConfig();
        ItemStack item = new ItemStack(materialForQuest(quest, status, tracked));
        ItemMeta meta = item.getItemMeta();

        String archetypeTag = quest.getArchetype() != null && !quest.getArchetype().isBlank()
                ? ArchetypeUtil.miniColorTag(quest.getArchetype()) : "";
        String archetypeClose = quest.getArchetype() != null && !quest.getArchetype().isBlank()
                ? ArchetypeUtil.miniColorCloseTag(quest.getArchetype()) : "";
        String namePrefix = tracked ? "<gold><bold>★ </bold></gold>" : "";
        meta.displayName(messages.parse(namePrefix + archetypeTag + statusColor(status) + quest.getName()
                + "</" + statusTag(status) + ">" + archetypeClose));

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
            lore.add(messages.parse(config.getQuestJournalTrackedBadge()));
        }

        if (status == QuestManager.QuestStatus.LOCKED) {
            appendLockedInfo(plugin, profile, quest, lore, messages, config, questManager, player);
        } else if (status != QuestManager.QuestStatus.COMPLETED) {
            appendObjectives(profile, quest, lore, messages);
        } else {
            lore.add(messages.parse("<green>Todos os objetivos concluídos.</green>"));
            questManager.findNextQuestInChain(player, profile, quest).ifPresent(next ->
                    lore.add(messages.parse(config.getQuestJournalChainNext()
                            .replace("{quest}", next.getName()))));
        }

        QuestManager.QuestProgress progress = questManager.getQuestProgress(profile, quest);
        if (progress.total() > 0 && status != QuestManager.QuestStatus.LOCKED) {
            lore.add(messages.parse(ProgressBarUtil.miniMessageBar(progress.completed(), progress.total())));
            lore.add(messages.parse("<dark_gray>Progresso:</dark_gray> <white>"
                    + progress.completed() + "/" + progress.total() + "</white>"
                    + " <dark_gray>(" + ProgressBarUtil.percent(progress.completed(), progress.total()) + "%)</dark_gray>"));
        }

        appendActionHint(config, lore, messages, status, tracked);

        meta.lore(lore);
        if (tracked || status == QuestManager.QuestStatus.NOT_STARTED) {
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        item.setItemMeta(meta);
        return item;
    }

    private static void appendActionHint(PluginConfig config, List<Component> lore, MessageUtil messages,
                                         QuestManager.QuestStatus status, boolean tracked) {
        lore.add(Component.empty());
        switch (status) {
            case NOT_STARTED -> lore.add(messages.parse(config.getQuestJournalAcceptHint()));
            case IN_PROGRESS -> {
                if (!tracked) {
                    lore.add(messages.parse(config.getQuestJournalTrackHint()));
                }
                if (config.isAllowAbandon()) {
                    lore.add(messages.parse(config.getQuestJournalAbandonHint()));
                }
            }
            case COMPLETED -> lore.add(messages.parse(config.getQuestJournalCompletedHint()));
            case LOCKED -> lore.add(messages.parse("<dark_gray>Bloqueada — veja os requisitos acima.</dark_gray>"));
            default -> {
            }
        }
    }

    private static void appendLockedInfo(RpgServerPlugin plugin, PlayerProfile profile, Quest quest,
                                         List<Component> lore, MessageUtil messages, PluginConfig config,
                                         QuestManager questManager, Player player) {
        if (!questManager.meetsRequirements(profile, quest)) {
            List<String> missing = questManager.formatMissingPrerequisiteNames(profile, quest);
            if (!missing.isEmpty()) {
                lore.add(messages.parse(config.getQuestJournalChainRequires()
                        .replace("{quests}", String.join(", ", missing))));
            }
        }
        if (!plugin.getLuckPermsHook().hasQuestPermission(player, quest.getId())) {
            lore.add(messages.parse(config.getQuestJournalLockedPermission()));
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

    private static Material materialForQuest(Quest quest, QuestManager.QuestStatus status, boolean tracked) {
        if (tracked && status == QuestManager.QuestStatus.IN_PROGRESS) {
            return Material.COMPASS;
        }
        if (status == QuestManager.QuestStatus.NOT_STARTED
                && quest.getArchetype() != null && !quest.getArchetype().isBlank()) {
            return switch (quest.getArchetype().toLowerCase(Locale.ROOT)) {
                case "warrior" -> Material.IRON_SWORD;
                case "merchant" -> Material.GOLD_INGOT;
                case "builder" -> Material.BRICKS;
                default -> Material.BOOK;
            };
        }
        return materialForStatus(status, tracked);
    }

    private static Material materialForStatus(QuestManager.QuestStatus status, boolean tracked) {
        return switch (status) {
            case IN_PROGRESS -> Material.WRITABLE_BOOK;
            case NOT_STARTED -> Material.BOOK;
            case LOCKED -> Material.BARRIER;
            case COMPLETED -> Material.ENCHANTED_BOOK;
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
