package dev.daniel730.rpgserver.gui;

import dev.daniel730.rpgserver.RpgServerPlugin;
import dev.daniel730.rpgserver.perk.PerkDefinition;
import dev.daniel730.rpgserver.profile.PlayerProfile;
import dev.daniel730.rpgserver.progression.SkillTreeManager;
import dev.daniel730.rpgserver.util.ArchetypeUtil;
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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class SkillTreeGui {

    private static final int SIZE = 54;
    private static final int[] TIER3_SLOTS = {11, 13, 15};
    private static final int[] TIER2_SLOTS = {20, 22, 24};
    private static final int[] TIER1_SLOTS = {29, 31, 33};

    private static final Map<String, String[]> BRANCHES_BY_ARCHETYPE = Map.of(
            "warrior", new String[]{"fury", "siege", "hunter"},
            "merchant", new String[]{"bazar", "leilao", "fortuna"},
            "builder", new String[]{"fortaleza", "mineracao", "urbanista"}
    );

    private SkillTreeGui() {
    }

    public static void open(RpgServerPlugin plugin, Player player) {
        open(plugin, player, "");
    }

    public static void open(RpgServerPlugin plugin, Player player, String branch) {
        SkillTreeHolder holder = new SkillTreeHolder();
        holder.setBranch(branch);
        Inventory inventory = Bukkit.createInventory(holder, SIZE,
                plugin.getMessageUtil().parse("<light_purple><bold>Árvore de Perks</bold></light_purple>"));
        holder.setInventory(inventory);
        boolean reduced = GuiMotion.reducedMotion(plugin);
        if (!reduced) {
            GuiMotion.skeleton(inventory, GuiMotion.slotRange(10, 43));
        }
        player.openInventory(inventory);
        render(plugin, player, holder, GuiMotion.MotionProfile.RARE);
    }

    public static void render(RpgServerPlugin plugin, Player player, SkillTreeHolder holder) {
        render(plugin, player, holder, GuiMotion.MotionProfile.INSTANT);
    }

    public static void render(RpgServerPlugin plugin, Player player, SkillTreeHolder holder,
                              GuiMotion.MotionProfile profile) {
        GuiMotion.cancel(holder);
        PlayerProfile profileData = plugin.getProfileManager().getOrCreate(player);
        SkillTreeManager skillTree = plugin.getSkillTreeManager();
        MessageUtil messages = plugin.getMessageUtil();
        Inventory inventory = holder.getInventory();
        boolean reduced = GuiMotion.reducedMotion(plugin);
        GuiMotion.MotionProfile effective = GuiMotion.effectiveProfile(profile, reduced);

        inventory.clear();
        holder.clearMappings();

        fillBorder(inventory, profileData.getArchetype());

        inventory.setItem(4, infoItem(messages,
                messages.parse("<gold>Essência:</gold> <white>" + profileData.getPathEssence() + "</white>"),
                List.of(messages.parse("<gray>Renascimentos:</gray> <white>" + profileData.getRebirthCount() + "</white>"),
                        messages.parse("<gray>Clique em um perk disponível para desbloquear.</gray>"))));

        List<GuiMotion.SlotItem> content = new ArrayList<>();
        String archetype = profileData.getArchetype();
        if (archetype == null || archetype.isBlank()) {
            content.add(new GuiMotion.SlotItem(22, infoItem(messages,
                    messages.parse("<red>Escolha um caminho primeiro</red>"),
                    List.of(messages.parse("<gray>Use a Central → Escolher Caminho.</gray>")))));
            finishEnter(plugin, holder, inventory, content, effective);
            return;
        }

        String[] branches = BRANCHES_BY_ARCHETYPE.get(archetype.toLowerCase(Locale.ROOT));
        if (branches == null) {
            renderLegacyGrid(plugin, player, holder, profileData, skillTree, messages, content);
            finishEnter(plugin, holder, inventory, content, effective);
            return;
        }

        Map<String, Map<Integer, PerkDefinition>> layout = buildBranchLayout(skillTree, archetype, branches);
        for (int column = 0; column < branches.length; column++) {
            String branchKey = branches[column];
            if (holder.getBranch() != null && !holder.getBranch().isBlank()
                    && !holder.getBranch().equalsIgnoreCase(branchKey)) {
                continue;
            }
            placeTierPerk(content, holder, messages, skillTree, profileData, layout, branchKey, 3, TIER3_SLOTS[column]);
            placeTierPerk(content, holder, messages, skillTree, profileData, layout, branchKey, 2, TIER2_SLOTS[column]);
            placeTierPerk(content, holder, messages, skillTree, profileData, layout, branchKey, 1, TIER1_SLOTS[column]);
            content.add(new GuiMotion.SlotItem(48 + column, branchLabel(messages, branchKey, column)));
        }
        finishEnter(plugin, holder, inventory, content, effective);
    }

    private static void finishEnter(RpgServerPlugin plugin, SkillTreeHolder holder, Inventory inventory,
                                    List<GuiMotion.SlotItem> content, GuiMotion.MotionProfile effective) {
        if (effective != GuiMotion.MotionProfile.INSTANT && !content.isEmpty()) {
            GuiMotion.skeleton(inventory, GuiMotion.slotRange(10, 43));
        }
        GuiMotion.enter(plugin, holder, inventory, content, effective, false);
    }

    private static void placeTierPerk(List<GuiMotion.SlotItem> content, SkillTreeHolder holder, MessageUtil messages,
                                      SkillTreeManager skillTree, PlayerProfile profile,
                                      Map<String, Map<Integer, PerkDefinition>> layout,
                                      String branch, int tier, int slot) {
        PerkDefinition perk = layout.getOrDefault(branch, Map.of()).get(tier);
        if (perk == null) {
            content.add(new GuiMotion.SlotItem(slot, createFiller(ArchetypeUtil.glassPane(profile.getArchetype()))));
            return;
        }
        SkillTreeManager.PerkStatus status = skillTree.getPerkStatus(profile, perk);
        content.add(new GuiMotion.SlotItem(slot, createPerkItem(messages, perk, status, profile)));
        if (status == SkillTreeManager.PerkStatus.AVAILABLE) {
            holder.mapSlot(slot, perk.getId());
        }
    }

    private static Map<String, Map<Integer, PerkDefinition>> buildBranchLayout(
            SkillTreeManager skillTree, String archetype, String[] branches) {
        Map<String, Map<Integer, PerkDefinition>> layout = new HashMap<>();
        for (String branch : branches) {
            layout.put(branch, new HashMap<>());
        }
        for (PerkDefinition perk : skillTree.getPerksForArchetype(archetype)) {
            if (perk.getBranch().isBlank() || perk.getTier() < 1 || perk.getTier() > 3) {
                continue;
            }
            String branchKey = perk.getBranch().toLowerCase(Locale.ROOT);
            Map<Integer, PerkDefinition> column = layout.get(branchKey);
            if (column == null) {
                continue;
            }
            PerkDefinition existing = column.get(perk.getTier());
            if (existing == null || perk.getName().compareToIgnoreCase(existing.getName()) < 0) {
                column.put(perk.getTier(), perk);
            }
        }
        return layout;
    }

    private static ItemStack branchLabel(MessageUtil messages, String branch, int column) {
        Material icon = switch (column) {
            case 0 -> Material.RED_BANNER;
            case 1 -> Material.YELLOW_BANNER;
            default -> Material.LIME_BANNER;
        };
        return actionItem(messages, icon,
                messages.parse("<yellow>" + capitalize(branch) + "</yellow>"),
                List.of(messages.parse("<gray>Ramo da árvore</gray>")), false);
    }

    private static String capitalize(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        return value.substring(0, 1).toUpperCase(Locale.ROOT) + value.substring(1);
    }

    private static void renderLegacyGrid(RpgServerPlugin plugin, Player player, SkillTreeHolder holder,
                                         PlayerProfile profile, SkillTreeManager skillTree,
                                         MessageUtil messages, List<GuiMotion.SlotItem> content) {
        List<PerkDefinition> perks = new ArrayList<>(skillTree.getPerksForArchetype(profile.getArchetype()));
        perks.sort((a, b) -> {
            int tier = Integer.compare(a.getTier(), b.getTier());
            if (tier != 0) {
                return tier;
            }
            return a.getName().compareToIgnoreCase(b.getName());
        });
        int[] slots = {29, 31, 33, 20, 22, 24, 11, 13, 15};
        int index = 0;
        for (PerkDefinition perk : perks) {
            if (index >= slots.length) {
                break;
            }
            int slot = slots[index++];
            SkillTreeManager.PerkStatus status = skillTree.getPerkStatus(profile, perk);
            content.add(new GuiMotion.SlotItem(slot, createPerkItem(messages, perk, status, profile)));
            if (status == SkillTreeManager.PerkStatus.AVAILABLE) {
                holder.mapSlot(slot, perk.getId());
            }
        }
    }

    private static ItemStack createPerkItem(MessageUtil messages, PerkDefinition perk,
                                            SkillTreeManager.PerkStatus status, PlayerProfile profile) {
        Material material = switch (status) {
            case UNLOCKED -> Material.ENCHANTED_BOOK;
            case AVAILABLE -> Material.BOOK;
            case CHOICE_LOCKED -> Material.BARRIER;
            case LOCKED -> Material.GRAY_DYE;
        };
        List<Component> lore = new ArrayList<>();
        lore.add(messages.parse("<gray>Ramo:</gray> <white>" + empty(perk.getBranch()) + "</white>"));
        lore.add(messages.parse("<gray>Tier:</gray> <white>" + perk.getTier() + "</white>"));
        if (perk.getEssenceCost() > 0) {
            lore.add(messages.parse("<gray>Custo:</gray> <light_purple>" + perk.getEssenceCost() + " essência</light_purple>"));
        }
        if (!perk.getExclusiveGroup().isBlank()) {
            lore.add(messages.parse("<dark_gray>Grupo exclusivo: " + perk.getExclusiveGroup() + "</dark_gray>"));
        }
        lore.add(messages.parse("<dark_gray>[" + status.getDisplay() + "]</dark_gray>"));
        if (status == SkillTreeManager.PerkStatus.AVAILABLE) {
            lore.add(messages.parse("<yellow>▶ Clique para desbloquear</yellow>"));
        }
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(messages.parse(color(status) + perk.getName() + "</color>"));
        meta.lore(lore);
        if (status == SkillTreeManager.PerkStatus.UNLOCKED) {
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        item.setItemMeta(meta);
        return item;
    }

    private static String color(SkillTreeManager.PerkStatus status) {
        return switch (status) {
            case UNLOCKED -> "<green>";
            case AVAILABLE -> "<yellow>";
            case CHOICE_LOCKED -> "<red>";
            case LOCKED -> "<gray>";
        };
    }

    private static String empty(String value) {
        return value == null || value.isBlank() ? "—" : value;
    }

    private static void fillBorder(Inventory inventory, String archetype) {
        ItemStack pane = createFiller(ArchetypeUtil.glassPane(archetype));
        for (int i = 0; i < 9; i++) {
            inventory.setItem(i, pane);
        }
        for (int row = 1; row < 6; row++) {
            inventory.setItem(row * 9, pane);
            inventory.setItem(row * 9 + 8, pane);
        }
        for (int i = 45; i < 54; i++) {
            inventory.setItem(i, pane);
        }
    }

    private static ItemStack createFiller(Material material) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.empty());
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack infoItem(MessageUtil messages, Component name, List<Component> lore) {
        ItemStack item = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(name);
        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack actionItem(MessageUtil messages, Material material, Component name,
                                        List<Component> lore, boolean glow) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(name);
        meta.lore(lore);
        if (glow) {
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        item.setItemMeta(meta);
        return item;
    }
}
