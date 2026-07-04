package dev.daniel730.rpgserver.gui;

import dev.daniel730.rpgserver.RpgServerPlugin;
import dev.daniel730.rpgserver.config.PluginConfig;
import dev.daniel730.rpgserver.profile.PlayerProfile;
import dev.daniel730.rpgserver.progression.SkillTreeManager;
import dev.daniel730.rpgserver.quest.Quest;
import dev.daniel730.rpgserver.quest.QuestManager;
import dev.daniel730.rpgserver.quest.QuestSchedule;
import dev.daniel730.rpgserver.util.ArchetypeUtil;
import dev.daniel730.rpgserver.util.MessageUtil;
import dev.daniel730.rpgserver.util.ProgressBarUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class PlayerHubGui {

    private static final int INVENTORY_SIZE = 54;
    private static final int CONTENT_START = 9;
    private static final int CONTENT_END = 44;

    private PlayerHubGui() {
    }

    public static void open(RpgServerPlugin plugin, Player player) {
        open(plugin, player, PlayerHubHolder.HubTab.INICIO);
    }

    public static void open(RpgServerPlugin plugin, Player player, PlayerHubHolder.HubTab tab) {
        PlayerHubHolder holder = new PlayerHubHolder();
        holder.setActiveTab(tab);
        Inventory inventory = Bukkit.createInventory(holder, INVENTORY_SIZE,
                plugin.getMessageUtil().parse(plugin.getPluginConfig().getHubTitle()));
        holder.setInventory(inventory);
        render(plugin, player, holder);
        player.openInventory(inventory);
    }

    public static void render(RpgServerPlugin plugin, Player player, PlayerHubHolder holder) {
        PlayerProfile profile = plugin.getProfileManager().getOrCreate(player);
        MessageUtil messages = plugin.getMessageUtil();
        PluginConfig config = plugin.getPluginConfig();
        var inventory = holder.getInventory();

        inventory.clear();
        holder.clearActions();

        fillBorder(inventory, profile.getArchetype());
        renderTabs(plugin, holder, messages, config);
        renderFooter(plugin, holder, messages, config);

        switch (holder.getActiveTab()) {
            case INICIO -> renderInicioTab(plugin, player, profile, holder, messages, config);
            case CIVS -> renderCivsTab(holder, messages, config);
            case RPG -> renderRpgTab(plugin, player, profile, holder, messages, config);
            case CONFIG -> renderConfigTab(plugin, player, profile, holder, messages, config);
            case QUESTS -> renderQuestsTab(plugin, player, profile, holder, messages, config);
        }
    }

    private static void renderTabs(RpgServerPlugin plugin, PlayerHubHolder holder,
                                   MessageUtil messages, PluginConfig config) {
        for (PlayerHubHolder.HubTab tab : PlayerHubHolder.HubTab.values()) {
            int slot = tab.getTabSlot();
            boolean active = tab == holder.getActiveTab();
            inventorySet(holder, slot, createTabItem(messages, config, tab, active));
            if (!active) {
                holder.mapAction(slot, PlayerHubHolder.HubClick.tab(tab));
            }
        }
    }

    private static void renderFooter(RpgServerPlugin plugin, PlayerHubHolder holder,
                                     MessageUtil messages, PluginConfig config) {
        holder.mapAction(PlayerHubHolder.FOOTER_REFRESH_SLOT, PlayerHubHolder.HubClick.of(PlayerHubHolder.HubAction.REFRESH));
        inventorySet(holder, PlayerHubHolder.FOOTER_REFRESH_SLOT,
                actionItem(Material.SPECTRAL_ARROW, messages.parse(config.getHubFooterRefresh()), true));

        holder.mapAction(PlayerHubHolder.FOOTER_CLOSE_SLOT, PlayerHubHolder.HubClick.of(PlayerHubHolder.HubAction.CLOSE));
        inventorySet(holder, PlayerHubHolder.FOOTER_CLOSE_SLOT,
                actionItem(Material.BARRIER, messages.parse(config.getHubFooterClose()), false));

        holder.mapAction(PlayerHubHolder.FOOTER_JOURNAL_SLOT, PlayerHubHolder.HubClick.of(PlayerHubHolder.HubAction.OPEN_JOURNAL));
        inventorySet(holder, PlayerHubHolder.FOOTER_JOURNAL_SLOT,
                actionItem(Material.WRITABLE_BOOK, messages.parse(config.getHubFooterJournal()), true));
    }

    private static void renderInicioTab(RpgServerPlugin plugin, Player player, PlayerProfile profile,
                                        PlayerHubHolder holder, MessageUtil messages, PluginConfig config) {
        QuestManager questManager = plugin.getQuestManager();

        inventorySet(holder, 13, createProfileItem(plugin, player, profile, messages, config, questManager));

        holder.mapAction(21, PlayerHubHolder.HubClick.of(PlayerHubHolder.HubAction.OPEN_JOURNAL));
        inventorySet(holder, 21, actionItem(Material.MAP,
                messages.parse(config.getHubInicioChoosePath()),
                List.of(messages.parse(config.getHubInicioChoosePathLore())), true));

        Optional<Quest> next = questManager.findNextAvailableQuest(player, profile);
        if (next.isEmpty() && profile.getArchetype() == null) {
            next = questManager.getPathQuests(player, profile).stream().findFirst();
        }
        if (next.isPresent()) {
            Quest quest = next.get();
            holder.mapAction(23, PlayerHubHolder.HubClick.trackQuest(quest.getId()));
            inventorySet(holder, 23, actionItem(Material.COMPASS,
                    messages.parse(config.getHubInicioNextQuest()),
                    List.of(messages.parse(config.getHubInicioNextQuestLore()
                            .replace("{quest}", quest.getName()))), true));
        } else {
            inventorySet(holder, 23, infoItem(Material.GRAY_DYE,
                    messages.parse(config.getHubInicioNoQuest()),
                    List.of(messages.parse("<dark_gray>Nenhuma missão pendente.</dark_gray>"))));
        }

        Quest tracked = questManager.findTrackedQuest(profile);
        if (tracked != null) {
            QuestManager.QuestProgress progress = questManager.getQuestProgress(profile, tracked);
            inventorySet(holder, 31, createProgressItem(messages, tracked, progress));
        }
    }

    private static void renderCivsTab(PlayerHubHolder holder, MessageUtil messages, PluginConfig config) {
        placeCommandAction(holder, 10, Material.GOLDEN_HELMET, config.getHubCivsTownTitle(),
                config.getHubCivsTownLore(), "/cv menu", true);
        placeCommandAction(holder, 12, Material.EMERALD, config.getHubCivsTownInfoTitle(),
                config.getHubCivsTownInfoLore(), "/cv town", true);
        placeCommandAction(holder, 14, Material.GRASS_BLOCK, config.getHubCivsRegionsTitle(),
                config.getHubCivsRegionsLore(), "/cv menu", true);
        placeCommandAction(holder, 16, Material.GOLD_INGOT, config.getHubCivsAuctionTitle(),
                config.getHubCivsAuctionLore(), "/cv auction", true);
        placeCommandAction(holder, 19, Material.ENCHANTING_TABLE, config.getHubCivsSpellsTitle(),
                config.getHubCivsSpellsLore(), "/cv menu", true);
        placeCommandAction(holder, 21, Material.WHEAT, config.getHubCivsFarmsTitle(),
                config.getHubCivsFarmsLore(), "/cv menu", true);
        placeCommandAction(holder, 23, Material.IRON_SWORD, config.getHubCivsCombatTitle(),
                config.getHubCivsCombatLore(), "/cv menu", true);
        inventorySet(holder, 25, infoItem(Material.OAK_SIGN, messages.parse(config.getHubCivsChestShopTitle()),
                List.of(messages.parse(config.getHubCivsChestShopLore()))));
    }

    private static void renderRpgTab(RpgServerPlugin plugin, Player player, PlayerProfile profile,
                                     PlayerHubHolder holder, MessageUtil messages, PluginConfig config) {
        SkillTreeManager skillTree = plugin.getSkillTreeManager();
        long unlocked = skillTree.getAllPerks().stream()
                .filter(p -> skillTree.getPerkStatus(profile, p) == SkillTreeManager.PerkStatus.UNLOCKED)
                .count();
        long total = skillTree.getAllPerks().size();

        holder.mapAction(11, PlayerHubHolder.HubClick.of(PlayerHubHolder.HubAction.OPEN_JOURNAL));
        inventorySet(holder, 11, actionItem(Material.BOOK,
                messages.parse(config.getHubRpgJournalTitle()),
                List.of(messages.parse(config.getHubRpgJournalLore())), true));

        holder.mapAction(13, PlayerHubHolder.HubClick.command("/rpg perks"));
        inventorySet(holder, 13, actionItem(Material.NETHER_STAR,
                messages.parse(config.getHubRpgPerksTitle()),
                List.of(messages.parse(config.getHubRpgPerksLore())), true));

        inventorySet(holder, 15, infoItem(Material.EXPERIENCE_BOTTLE,
                messages.parse(config.getHubRpgPerkSummaryTitle()
                        .replace("{unlocked}", String.valueOf(unlocked))
                        .replace("{total}", String.valueOf(total))),
                List.of(messages.parse(config.getHubRpgPerkSummaryLore()))));

        inventorySet(holder, 20, createScheduleStatusItem(plugin, player, profile, messages, config, QuestSchedule.DAILY));
        inventorySet(holder, 24, createScheduleStatusItem(plugin, player, profile, messages, config, QuestSchedule.WEEKLY));

        holder.mapAction(31, PlayerHubHolder.HubClick.command("/rpg profile"));
        inventorySet(holder, 31, actionItem(Material.PLAYER_HEAD,
                messages.parse(config.getHubRpgProfileTitle()),
                List.of(messages.parse(config.getHubRpgProfileLore())), true));
    }

    private static void renderConfigTab(RpgServerPlugin plugin, Player player, PlayerProfile profile,
                                        PlayerHubHolder holder, MessageUtil messages, PluginConfig config) {
        boolean notifications = plugin.getQuestFeedbackService().isNotificationsEnabled(profile);
        boolean bossBar = plugin.getQuestFeedbackService().isBossBarEnabled(profile);

        holder.mapAction(20, PlayerHubHolder.HubClick.of(PlayerHubHolder.HubAction.TOGGLE_NOTIFICATIONS));
        inventorySet(holder, 20, toggleItem(messages, config.getHubConfigNotificationsTitle(),
                config.getHubConfigNotificationsLore(), notifications));

        holder.mapAction(24, PlayerHubHolder.HubClick.of(PlayerHubHolder.HubAction.TOGGLE_BOSSBAR));
        inventorySet(holder, 24, toggleItem(messages, config.getHubConfigBossBarTitle(),
                config.getHubConfigBossBarLore(), bossBar));

        inventorySet(holder, 13, infoItem(Material.OAK_SIGN,
                messages.parse(config.getHubConfigHintTitle()),
                List.of(messages.parse(config.getHubConfigHintLore()))));
    }

    private static void renderQuestsTab(RpgServerPlugin plugin, Player player, PlayerProfile profile,
                                        PlayerHubHolder holder, MessageUtil messages, PluginConfig config) {
        QuestManager questManager = plugin.getQuestManager();
        List<Quest> preview = collectQuestPreview(plugin, player, profile, questManager);

        int slot = CONTENT_START;
        for (Quest quest : preview) {
            if (slot > CONTENT_END) {
                break;
            }
            QuestManager.QuestStatus status = questManager.getQuestStatus(player, profile, quest);
            boolean tracked = quest.getId().equals(profile.getTrackedQuestId());
            inventorySet(holder, slot, createQuestPreviewItem(plugin, player, profile, quest, status, tracked));
            if (status == QuestManager.QuestStatus.NOT_STARTED) {
                holder.mapAction(slot, PlayerHubHolder.HubClick.of(PlayerHubHolder.HubAction.OPEN_JOURNAL));
            } else if (status == QuestManager.QuestStatus.IN_PROGRESS && !tracked) {
                holder.mapAction(slot, PlayerHubHolder.HubClick.trackQuest(quest.getId()));
            } else {
                holder.mapAction(slot, PlayerHubHolder.HubClick.of(PlayerHubHolder.HubAction.OPEN_JOURNAL));
            }
            slot++;
        }

        holder.mapAction(40, PlayerHubHolder.HubClick.of(PlayerHubHolder.HubAction.OPEN_JOURNAL));
        inventorySet(holder, 40, actionItem(Material.WRITABLE_BOOK,
                messages.parse(config.getHubQuestsOpenJournalTitle()),
                List.of(messages.parse(config.getHubQuestsOpenJournalLore())), true));
    }

    private static List<Quest> collectQuestPreview(RpgServerPlugin plugin, Player player,
                                                   PlayerProfile profile, QuestManager questManager) {
        List<Quest> preview = new ArrayList<>();
        String archetype = profile.getArchetype();
        if (archetype == null || archetype.isBlank()) {
            preview.addAll(questManager.getPathQuests(player, profile));
        } else {
            preview.addAll(questManager.getArchetypeStoryQuests(player, profile, archetype));
        }
        preview.addAll(questManager.getScheduledQuests(player, profile, QuestSchedule.DAILY));
        preview.addAll(questManager.getScheduledQuests(player, profile, QuestSchedule.WEEKLY));
        return preview.stream().limit(28).toList();
    }

    private static ItemStack createProfileItem(RpgServerPlugin plugin, Player player, PlayerProfile profile,
                                               MessageUtil messages, PluginConfig config,
                                               QuestManager questManager) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        meta.setOwningPlayer(player);
        meta.displayName(messages.parse(config.getHubInicioProfileTitle().replace("{player}", player.getName())));

        List<Component> lore = new ArrayList<>();
        String archetype = profile.getArchetype();
        if (archetype == null || archetype.isBlank()) {
            lore.add(messages.parse("<yellow>Caminho:</yellow> <gray>Não escolhido</gray>"));
        } else {
            lore.add(messages.parse("<gray>Caminho:</gray> " + ArchetypeUtil.coloredDisplayName(archetype)));
        }

        Quest tracked = questManager.findTrackedQuest(profile);
        if (tracked != null) {
            lore.add(messages.parse("<gray>Rastreando:</gray> <aqua>" + tracked.getName() + "</aqua>"));
        }

        String nextName = questManager.formatNextQuestName(player, profile);
        if (!"Nenhuma".equals(nextName)) {
            lore.add(messages.parse("<gray>Próxima:</gray> <white>" + nextName + "</white>"));
        }

        lore.add(Component.empty());
        lore.add(messages.parse("<dark_gray>Central do Reino — seu painel rápido.</dark_gray>"));
        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack createProgressItem(MessageUtil messages, Quest quest,
                                                QuestManager.QuestProgress progress) {
        ItemStack item = new ItemStack(Material.EXPERIENCE_BOTTLE);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(messages.parse("<gold><bold>Progresso da Quest</bold></gold>"));
        meta.lore(List.of(
                messages.parse("<aqua>" + quest.getName() + "</aqua>"),
                messages.parse(ProgressBarUtil.miniMessageBar(progress.completed(), progress.total())),
                messages.parse("<dark_gray>" + progress.completed() + "/" + progress.total()
                        + " (" + ProgressBarUtil.percent(progress.completed(), progress.total()) + "%)</dark_gray>")
        ));
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack createScheduleStatusItem(RpgServerPlugin plugin, Player player,
                                                      PlayerProfile profile, MessageUtil messages,
                                                      PluginConfig config, QuestSchedule schedule) {
        QuestManager questManager = plugin.getQuestManager();
        List<Quest> scheduled = questManager.getScheduledQuests(player, profile, schedule);
        boolean anyAvailable = scheduled.stream()
                .anyMatch(q -> {
                    QuestManager.QuestStatus s = questManager.getQuestStatus(player, profile, q);
                    return s == QuestManager.QuestStatus.NOT_STARTED || s == QuestManager.QuestStatus.IN_PROGRESS;
                });
        boolean allDone = !scheduled.isEmpty() && scheduled.stream()
                .allMatch(q -> questManager.getQuestStatus(player, profile, q) == QuestManager.QuestStatus.COMPLETED);

        Material material = switch (schedule) {
            case DAILY -> anyAvailable ? Material.SUNFLOWER : (allDone ? Material.DEAD_BUSH : Material.SUNFLOWER);
            case WEEKLY -> anyAvailable ? Material.CLOCK : (allDone ? Material.BARRIER : Material.CLOCK);
            case NONE -> Material.PAPER;
        };

        String titleKey = schedule == QuestSchedule.DAILY ? config.getHubRpgDailyTitle() : config.getHubRpgWeeklyTitle();
        String statusLine;
        if (scheduled.isEmpty()) {
            statusLine = config.getHubRpgScheduleNone();
        } else if (allDone) {
            statusLine = config.getHubRpgScheduleDone();
        } else if (anyAvailable) {
            statusLine = config.getHubRpgScheduleAvailable();
        } else {
            statusLine = config.getHubRpgScheduleLocked();
        }

        return infoItem(material, messages.parse(titleKey),
                List.of(messages.parse(statusLine), messages.parse(config.getHubRpgScheduleHint())));
    }

    private static ItemStack createQuestPreviewItem(RpgServerPlugin plugin, Player player, PlayerProfile profile,
                                                    Quest quest, QuestManager.QuestStatus status, boolean tracked) {
        MessageUtil messages = plugin.getMessageUtil();
        Material material = switch (status) {
            case IN_PROGRESS -> tracked ? Material.COMPASS : Material.WRITABLE_BOOK;
            case NOT_STARTED -> Material.BOOK;
            case LOCKED -> Material.BARRIER;
            case COMPLETED -> Material.ENCHANTED_BOOK;
        };
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        String prefix = tracked ? "<gold>★ </gold>" : "";
        meta.displayName(messages.parse(prefix + statusColor(status) + quest.getName() + statusClose(status)));
        List<Component> lore = new ArrayList<>();
        lore.add(messages.parse("<dark_gray>" + status.getDisplay() + "</dark_gray>"));
        if (status != QuestManager.QuestStatus.LOCKED && status != QuestManager.QuestStatus.COMPLETED) {
            QuestManager.QuestProgress progress = plugin.getQuestManager().getQuestProgress(profile, quest);
            if (progress.total() > 0) {
                lore.add(messages.parse(ProgressBarUtil.miniMessageBar(progress.completed(), progress.total())));
            }
        }
        lore.add(Component.empty());
        lore.add(messages.parse(status == QuestManager.QuestStatus.IN_PROGRESS && !tracked
                ? "<gold>▶ Clique para rastrear</gold>"
                : "<yellow>▶ Abrir diário</yellow>"));
        meta.lore(lore);
        if (tracked || status == QuestManager.QuestStatus.NOT_STARTED) {
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack createTabItem(MessageUtil messages, PluginConfig config,
                                           PlayerHubHolder.HubTab tab, boolean active) {
        Material material = switch (tab) {
            case INICIO -> Material.COMPASS;
            case CIVS -> Material.GRASS_BLOCK;
            case RPG -> Material.NETHER_STAR;
            case CONFIG -> Material.COMPARATOR;
            case QUESTS -> Material.BOOK;
        };
        String title = switch (tab) {
            case INICIO -> config.getHubTabInicio();
            case CIVS -> config.getHubTabCivs();
            case RPG -> config.getHubTabRpg();
            case CONFIG -> config.getHubTabConfig();
            case QUESTS -> config.getHubTabQuests();
        };
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        String prefix = active ? "<gold><bold>" : "<gray>";
        String suffix = active ? "</bold></gold>" : "</gray>";
        meta.displayName(messages.parse(prefix + title + suffix));
        if (active) {
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack toggleItem(MessageUtil messages, String title, String loreLine, boolean enabled) {
        Material material = enabled ? Material.LIME_WOOL : Material.RED_WOOL;
        String state = enabled ? "<green>Ligado</green>" : "<red>Desligado</red>";
        return actionItem(material, messages.parse(title),
                List.of(messages.parse(loreLine), messages.parse("<gray>Estado:</gray> " + state),
                        messages.parse("<yellow>▶ Clique para alternar</yellow>")), true);
    }

    private static void placeCommandAction(PlayerHubHolder holder, int slot, Material material,
                                           String title, String lore, String command, boolean glint) {
        holder.mapAction(slot, PlayerHubHolder.HubClick.command(command));
        inventorySet(holder, slot, actionItem(material,
                RpgServerPlugin.getInstance().getMessageUtil().parse(title),
                List.of(RpgServerPlugin.getInstance().getMessageUtil().parse(lore),
                        RpgServerPlugin.getInstance().getMessageUtil().parse("<yellow>▶ Clique para abrir</yellow>")),
                glint));
    }

    private static ItemStack actionItem(Material material, Component name, boolean glint) {
        return actionItem(material, name, List.of(), glint);
    }

    private static ItemStack actionItem(Material material, Component name, List<Component> lore, boolean glint) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(name);
        if (!lore.isEmpty()) {
            meta.lore(lore);
        }
        if (glint) {
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack infoItem(Material material, Component name, List<Component> lore) {
        return actionItem(material, name, lore, false);
    }

    private static void inventorySet(PlayerHubHolder holder, int slot, ItemStack item) {
        holder.getInventory().setItem(slot, item);
    }

    private static void fillBorder(org.bukkit.inventory.Inventory inventory, String playerArchetype) {
        ItemStack filler = createFiller(ArchetypeUtil.glassPane(playerArchetype));
        for (int slot = 0; slot < 9; slot++) {
            if (!isTabSlot(slot)) {
                inventory.setItem(slot, filler);
            }
        }
        for (int row = 1; row <= 4; row++) {
            inventory.setItem(row * 9, filler);
            inventory.setItem(row * 9 + 8, filler);
        }
        for (int slot = 45; slot < 54; slot++) {
            if (slot != PlayerHubHolder.FOOTER_REFRESH_SLOT
                    && slot != PlayerHubHolder.FOOTER_CLOSE_SLOT
                    && slot != PlayerHubHolder.FOOTER_JOURNAL_SLOT) {
                inventory.setItem(slot, filler);
            }
        }
    }

    private static boolean isTabSlot(int slot) {
        for (int tabSlot : PlayerHubHolder.TAB_SLOTS) {
            if (tabSlot == slot) {
                return true;
            }
        }
        return false;
    }

    private static ItemStack createFiller(Material material) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.empty());
        item.setItemMeta(meta);
        return item;
    }

    private static String statusColor(QuestManager.QuestStatus status) {
        return switch (status) {
            case IN_PROGRESS -> "<green>";
            case NOT_STARTED -> "<yellow>";
            case LOCKED -> "<red>";
            case COMPLETED -> "<aqua>";
        };
    }

    private static String statusClose(QuestManager.QuestStatus status) {
        return switch (status) {
            case IN_PROGRESS -> "</green>";
            case NOT_STARTED -> "</yellow>";
            case LOCKED -> "</red>";
            case COMPLETED -> "</aqua>";
        };
    }
}
