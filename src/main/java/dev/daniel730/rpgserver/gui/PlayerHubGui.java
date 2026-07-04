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
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public final class PlayerHubGui {

    private static final int INVENTORY_SIZE = 54;
    private static final int CONTENT_START = 9;
    private static final int CONTENT_END = 44;
    private static final int[] QUEST_TREE_SLOTS = {11, 20, 29, 38};
    private static final int QUEST_PREVIEW_LIMIT = 5;

    private PlayerHubGui() {
    }

    public static void open(RpgServerPlugin plugin, Player player, PlayerHubHolder holder) {
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
        Inventory inventory = holder.getInventory();

        inventory.clear();
        holder.clearActions();

        fillBorder(inventory, profile.getArchetype());
        renderTabs(holder, messages, config);
        renderFooter(plugin, holder, messages, config);

        switch (holder.getScreen()) {
            case PATH_PICKER -> renderPathPickerTab(plugin, player, profile, holder, messages, config);
            case QUEST_TREE -> renderQuestTreeView(plugin, player, profile, holder, messages, config);
            case TAB -> renderActiveTab(plugin, player, profile, holder, messages, config);
        }
    }

    private static void renderActiveTab(RpgServerPlugin plugin, Player player, PlayerProfile profile,
                                        PlayerHubHolder holder, MessageUtil messages, PluginConfig config) {
        switch (holder.getActiveTab()) {
            case INICIO -> renderInicioTab(plugin, player, profile, holder, messages, config);
            case CIVS -> renderCivsTab(plugin, holder, messages, config);
            case RPG -> renderRpgTab(plugin, player, profile, holder, messages, config);
            case CONFIG -> renderConfigTab(plugin, player, profile, holder, messages, config);
            case QUESTS -> renderQuestsTab(plugin, player, profile, holder, messages, config);
        }
    }

    private static void renderTabs(PlayerHubHolder holder, MessageUtil messages, PluginConfig config) {
        for (PlayerHubHolder.HubTab tab : PlayerHubHolder.HubTab.values()) {
            int slot = tab.getTabSlot();
            boolean active = tab == holder.getActiveTab() && holder.getScreen() == PlayerHubHolder.HubScreen.TAB;
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

        if (holder.canGoBack()) {
            holder.mapAction(PlayerHubHolder.FOOTER_BACK_SLOT, PlayerHubHolder.HubClick.of(PlayerHubHolder.HubAction.BACK));
            inventorySet(holder, PlayerHubHolder.FOOTER_BACK_SLOT,
                    actionItem(Material.ARROW, messages.parse(config.getHubFooterBack()), true));
        } else {
            inventorySet(holder, PlayerHubHolder.FOOTER_BACK_SLOT,
                    infoItem(Material.GRAY_STAINED_GLASS_PANE, Component.empty(), List.of()));
        }

        holder.mapAction(PlayerHubHolder.FOOTER_TRACK_SLOT,
                PlayerHubHolder.HubClick.of(PlayerHubHolder.HubAction.TRACK_NEXT));
        inventorySet(holder, PlayerHubHolder.FOOTER_TRACK_SLOT,
                actionItem(Material.COMPASS, messages.parse(config.getHubFooterTrack()), true));

        holder.mapAction(PlayerHubHolder.FOOTER_SYNC_SLOT, PlayerHubHolder.HubClick.of(PlayerHubHolder.HubAction.SYNC));
        inventorySet(holder, PlayerHubHolder.FOOTER_SYNC_SLOT,
                actionItem(Material.REPEATER, messages.parse(config.getHubFooterSync()), true));

        holder.mapAction(PlayerHubHolder.FOOTER_CLOSE_SLOT, PlayerHubHolder.HubClick.of(PlayerHubHolder.HubAction.CLOSE));
        inventorySet(holder, PlayerHubHolder.FOOTER_CLOSE_SLOT,
                actionItem(Material.BARRIER, messages.parse(config.getHubFooterClose()), false));
    }

    private static void renderInicioTab(RpgServerPlugin plugin, Player player, PlayerProfile profile,
                                        PlayerHubHolder holder, MessageUtil messages, PluginConfig config) {
        QuestManager questManager = plugin.getQuestManager();

        inventorySet(holder, 13, createProfileItem(plugin, player, profile, messages, config, questManager));

        holder.mapAction(21, PlayerHubHolder.HubClick.subview(PlayerHubHolder.HubScreen.PATH_PICKER));
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

        if (plugin.getCivsHook().isEnabled()) {
            holder.mapAction(29, PlayerHubHolder.HubClick.civsMenu("port"));
            inventorySet(holder, 29, actionItem(Material.ENDER_PEARL,
                    messages.parse(config.getHubCivsLocationsTitle()),
                    List.of(messages.parse(config.getHubCivsLocationsLore()),
                            messages.parse("<yellow>▶ Abrir locais Civs</yellow>")), true));
        }
    }

    private static void renderPathPickerTab(RpgServerPlugin plugin, Player player, PlayerProfile profile,
                                            PlayerHubHolder holder, MessageUtil messages, PluginConfig config) {
        QuestManager questManager = plugin.getQuestManager();
        inventorySet(holder, 4, infoItem(Material.MAP,
                messages.parse("<gold><bold>Escolha seu Caminho</bold></gold>"),
                List.of(messages.parse("<gray>Clique para aceitar e começar.</gray>"))));

        List<Quest> paths = questManager.getPathQuests(player, profile);
        int[] slots = {20, 22, 24};
        Material[] icons = {Material.IRON_SWORD, Material.EMERALD, Material.BRICKS};
        for (int i = 0; i < paths.size() && i < slots.length; i++) {
            Quest path = paths.get(i);
            int slot = slots[i];
            holder.mapAction(slot, PlayerHubHolder.HubClick.trackQuest(path.getId()));
            inventorySet(holder, slot, createPathHead(messages, path, icons[i]));
        }

        if (profile.getArchetype() != null && !profile.getArchetype().isBlank()) {
            inventorySet(holder, 31, infoItem(ArchetypeUtil.glassPane(profile.getArchetype()),
                    messages.parse("<gray>Caminho atual:</gray> "
                            + ArchetypeUtil.coloredDisplayName(profile.getArchetype())),
                    List.of(messages.parse("<dark_gray>Outros caminhos ficam bloqueados.</dark_gray>"))));
        }
    }

    private static void renderQuestTreeView(RpgServerPlugin plugin, Player player, PlayerProfile profile,
                                            PlayerHubHolder holder, MessageUtil messages, PluginConfig config) {
        QuestManager questManager = plugin.getQuestManager();
        String archetype = profile.getArchetype();
        inventorySet(holder, 4, infoItem(Material.OAK_SAPLING,
                messages.parse(config.getHubRpgQuestTreeTitle()),
                List.of(messages.parse(config.getHubRpgQuestTreeLore()))));

        if (archetype == null || archetype.isBlank()) {
            inventorySet(holder, 22, infoItem(Material.BARRIER,
                    messages.parse("<red>Escolha um caminho primeiro</red>"),
                    List.of(messages.parse("<gray>Use Início → Escolher Caminho.</gray>"))));
            return;
        }

        List<Quest> chain = questManager.getArchetypeStoryQuests(player, profile, archetype);
        int slotIndex = 0;
        for (Quest quest : chain) {
            if (slotIndex >= QUEST_TREE_SLOTS.length) {
                break;
            }
            int slot = QUEST_TREE_SLOTS[slotIndex++];
            QuestManager.QuestStatus status = questManager.getQuestStatus(player, profile, quest);
            boolean tracked = quest.getId().equals(profile.getTrackedQuestId());
            inventorySet(holder, slot, createQuestTreeNode(plugin, player, profile, quest, status, tracked));
            mapQuestNodeAction(holder, slot, quest, status, tracked);
        }
    }

    private static void renderCivsTab(RpgServerPlugin plugin, PlayerHubHolder holder,
                                      MessageUtil messages, PluginConfig config) {
        boolean civs = plugin.getCivsHook().isEnabled();
        if (civs) {
            placeCivsMenuAction(holder, 10, Material.GOLDEN_HELMET, config.getHubCivsTownTitle(),
                    config.getHubCivsTownLore(), "main");
            placeCivsMenuAction(holder, 12, Material.ENDER_PEARL, config.getHubCivsLocationsTitle(),
                    config.getHubCivsLocationsLore(), "port");
            placeCivsMenuAction(holder, 14, Material.BLUE_BED, config.getHubCivsTownInfoTitle(),
                    config.getHubCivsTownInfoLore(), "select-town");
            placeCivsMenuAction(holder, 16, Material.BRICKS, config.getHubCivsRegionsTitle(),
                    config.getHubCivsRegionsLore(), "region-list");
            placeCivsMenuAction(holder, 19, Material.GOLD_INGOT, config.getHubCivsAuctionTitle(),
                    config.getHubCivsAuctionLore(), "auction-browse");
            placeCivsMenuAction(holder, 21, Material.ENCHANTING_TABLE, config.getHubCivsSpellsTitle(),
                    config.getHubCivsSpellsLore(), "spell-list");
            placeCivsMenuAction(holder, 23, Material.MAP, config.getHubCivsFarmsTitle(),
                    config.getHubCivsFarmsLore(), "blueprints");
            placeCivsMenuAction(holder, 25, Material.IRON_SWORD, config.getHubCivsCombatTitle(),
                    config.getHubCivsCombatLore(), "class-list");
        } else {
            inventorySet(holder, 22, infoItem(Material.BARRIER,
                    messages.parse("<red>Civs indisponível</red>"),
                    List.of(messages.parse("<gray>Integração Civs desligada ou plugin ausente.</gray>"))));
        }
        inventorySet(holder, 31, infoItem(Material.OAK_SIGN, messages.parse(config.getHubCivsChestShopTitle()),
                List.of(messages.parse(config.getHubCivsChestShopLore()))));
    }

    private static void renderRpgTab(RpgServerPlugin plugin, Player player, PlayerProfile profile,
                                     PlayerHubHolder holder, MessageUtil messages, PluginConfig config) {
        SkillTreeManager skillTree = plugin.getSkillTreeManager();
        long unlocked = skillTree.getAllPerks().stream()
                .filter(p -> skillTree.getPerkStatus(profile, p) == SkillTreeManager.PerkStatus.UNLOCKED)
                .count();
        long total = skillTree.getAllPerks().size();

        holder.mapAction(11, PlayerHubHolder.HubClick.subview(PlayerHubHolder.HubScreen.QUEST_TREE));
        inventorySet(holder, 11, actionItem(Material.OAK_SAPLING,
                messages.parse(config.getHubRpgQuestTreeTitle()),
                List.of(messages.parse(config.getHubRpgQuestTreeLore())), true));

        holder.mapAction(13, PlayerHubHolder.HubClick.command("/rpg perks"));
        inventorySet(holder, 13, actionItem(Material.NETHER_STAR,
                messages.parse(config.getHubRpgPerksTitle()),
                List.of(messages.parse(config.getHubRpgPerksLore())), true));

        inventorySet(holder, 15, infoItem(Material.EXPERIENCE_BOTTLE,
                messages.parse(config.getHubRpgPerkSummaryTitle()
                        .replace("{unlocked}", String.valueOf(unlocked))
                        .replace("{total}", String.valueOf(total))),
                List.of(messages.parse(config.getHubRpgPerkSummaryLore()))));

        List<Quest> chainPreview = collectQuestChainPreview(plugin, player, profile);
        int slot = 28;
        for (Quest quest : chainPreview) {
            if (slot > 34) {
                break;
            }
            QuestManager.QuestStatus status = plugin.getQuestManager().getQuestStatus(player, profile, quest);
            boolean tracked = quest.getId().equals(profile.getTrackedQuestId());
            inventorySet(holder, slot, createQuestPreviewItem(plugin, player, profile, quest, status, tracked));
            mapQuestNodeAction(holder, slot, quest, status, tracked);
            slot++;
        }

        inventorySet(holder, 20, createScheduleStatusItem(plugin, player, profile, messages, config, QuestSchedule.DAILY));
        inventorySet(holder, 24, createScheduleStatusItem(plugin, player, profile, messages, config, QuestSchedule.WEEKLY));

        holder.mapAction(31, PlayerHubHolder.HubClick.of(PlayerHubHolder.HubAction.OPEN_JOURNAL));
        inventorySet(holder, 31, actionItem(Material.WRITABLE_BOOK,
                messages.parse(config.getHubRpgJournalTitle()),
                List.of(messages.parse(config.getHubRpgJournalLore())), true));
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
        List<Quest> preview = collectQuestChainPreview(plugin, player, profile);

        int slot = CONTENT_START;
        for (Quest quest : preview) {
            if (slot > CONTENT_END) {
                break;
            }
            QuestManager.QuestStatus status = questManager.getQuestStatus(player, profile, quest);
            boolean tracked = quest.getId().equals(profile.getTrackedQuestId());
            inventorySet(holder, slot, createQuestPreviewItem(plugin, player, profile, quest, status, tracked));
            mapQuestNodeAction(holder, slot, quest, status, tracked);
            slot++;
        }

        holder.mapAction(40, PlayerHubHolder.HubClick.of(PlayerHubHolder.HubAction.OPEN_JOURNAL));
        inventorySet(holder, 40, actionItem(Material.WRITABLE_BOOK,
                messages.parse(config.getHubQuestsOpenJournalTitle()),
                List.of(messages.parse(config.getHubQuestsOpenJournalLore())), true));
    }

    private static void mapQuestNodeAction(PlayerHubHolder holder, int slot, Quest quest,
                                           QuestManager.QuestStatus status, boolean tracked) {
        if (status == QuestManager.QuestStatus.NOT_STARTED) {
            holder.mapAction(slot, PlayerHubHolder.HubClick.trackQuest(quest.getId()));
        } else if (status == QuestManager.QuestStatus.IN_PROGRESS && !tracked) {
            holder.mapAction(slot, PlayerHubHolder.HubClick.trackQuest(quest.getId()));
        } else if (status == QuestManager.QuestStatus.IN_PROGRESS) {
            holder.mapAction(slot, PlayerHubHolder.HubClick.trackQuest(quest.getId()));
        } else if (status == QuestManager.QuestStatus.LOCKED) {
            holder.mapAction(slot, PlayerHubHolder.HubClick.of(PlayerHubHolder.HubAction.OPEN_JOURNAL));
        }
    }

    private static List<Quest> collectQuestChainPreview(RpgServerPlugin plugin, Player player,
                                                        PlayerProfile profile) {
        QuestManager questManager = plugin.getQuestManager();
        String archetype = profile.getArchetype();
        if (archetype == null || archetype.isBlank()) {
            return questManager.getPathQuests(player, profile).stream().limit(3).toList();
        }

        List<Quest> chain = questManager.getArchetypeStoryQuests(player, profile, archetype);
        List<Quest> preview = new ArrayList<>();
        boolean started = false;
        for (Quest quest : chain) {
            QuestManager.QuestStatus status = questManager.getQuestStatus(player, profile, quest);
            if (!started && status == QuestManager.QuestStatus.COMPLETED) {
                continue;
            }
            started = true;
            preview.add(quest);
            if (preview.size() >= QUEST_PREVIEW_LIMIT) {
                break;
            }
        }
        if (preview.isEmpty()) {
            return chain.stream().limit(QUEST_PREVIEW_LIMIT).toList();
        }
        return preview;
    }

    private static ItemStack createPathHead(MessageUtil messages, Quest path, Material fallback) {
        Material material = switch (path.getArchetype() != null ? path.getArchetype().toLowerCase(Locale.ROOT) : "") {
            case "warrior" -> Material.IRON_SWORD;
            case "merchant" -> Material.EMERALD;
            case "builder" -> Material.BRICKS;
            default -> fallback;
        };
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        String arch = path.getArchetype();
        meta.displayName(messages.parse(ArchetypeUtil.coloredDisplayName(arch) + " <white>— " + path.getName() + "</white>"));
        meta.lore(List.of(
                messages.parse("<gray>" + path.getDescription() + "</gray>"),
                Component.empty(),
                messages.parse("<yellow>▶ Clique para aceitar</yellow>")
        ));
        meta.addEnchant(Enchantment.UNBREAKING, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack createQuestTreeNode(RpgServerPlugin plugin, Player player, PlayerProfile profile,
                                                 Quest quest, QuestManager.QuestStatus status, boolean tracked) {
        Material material = switch (status) {
            case COMPLETED -> Material.LIME_STAINED_GLASS_PANE;
            case IN_PROGRESS -> tracked ? Material.COMPASS : Material.YELLOW_STAINED_GLASS_PANE;
            case NOT_STARTED -> Material.WHITE_STAINED_GLASS_PANE;
            case LOCKED -> Material.RED_STAINED_GLASS_PANE;
        };
        MessageUtil messages = plugin.getMessageUtil();
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
        lore.add(messages.parse(clickHint(status, tracked)));
        meta.lore(lore);
        if (tracked || status == QuestManager.QuestStatus.NOT_STARTED) {
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        item.setItemMeta(meta);
        return item;
    }

    private static String clickHint(QuestManager.QuestStatus status, boolean tracked) {
        return switch (status) {
            case NOT_STARTED -> "<yellow>▶ Clique para aceitar</yellow>";
            case IN_PROGRESS -> tracked ? "<aqua>Rastreando</aqua>" : "<gold>▶ Clique para rastrear</gold>";
            case COMPLETED -> "<green>✔ Concluída</green>";
            case LOCKED -> "<red>Bloqueada</red>";
        };
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
        lore.add(messages.parse(clickHint(status, tracked)));
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

    private static void placeCivsMenuAction(PlayerHubHolder holder, int slot, Material material,
                                            String title, String lore, String menuRef) {
        holder.mapAction(slot, PlayerHubHolder.HubClick.civsMenu(menuRef));
        inventorySet(holder, slot, actionItem(material,
                RpgServerPlugin.getInstance().getMessageUtil().parse(title),
                List.of(RpgServerPlugin.getInstance().getMessageUtil().parse(lore),
                        RpgServerPlugin.getInstance().getMessageUtil().parse("<yellow>▶ Clique para abrir</yellow>")),
                true));
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

    private static void fillBorder(Inventory inventory, String playerArchetype) {
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
            if (!isFooterSlot(slot)) {
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

    private static boolean isFooterSlot(int slot) {
        return slot == PlayerHubHolder.FOOTER_REFRESH_SLOT
                || slot == PlayerHubHolder.FOOTER_BACK_SLOT
                || slot == PlayerHubHolder.FOOTER_TRACK_SLOT
                || slot == PlayerHubHolder.FOOTER_SYNC_SLOT
                || slot == PlayerHubHolder.FOOTER_CLOSE_SLOT;
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
