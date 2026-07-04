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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class SkillTreeGui {

    private static final int SIZE = 54;
    private static final int[] TREE_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    };

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
        render(plugin, player, holder);
        player.openInventory(inventory);
    }

    public static void render(RpgServerPlugin plugin, Player player, SkillTreeHolder holder) {
        PlayerProfile profile = plugin.getProfileManager().getOrCreate(player);
        SkillTreeManager skillTree = plugin.getSkillTreeManager();
        MessageUtil messages = plugin.getMessageUtil();
        Inventory inventory = holder.getInventory();
        inventory.clear();
        holder.clearMappings();

        fillBorder(inventory, profile.getArchetype());

        inventory.setItem(4, infoItem(messages,
                messages.parse("<gold>Essência:</gold> <white>" + profile.getPathEssence() + "</white>"),
                List.of(messages.parse("<gray>Renascimentos:</gray> <white>" + profile.getRebirthCount() + "</white>"),
                        messages.parse("<gray>Clique em um perk disponível para desbloquear.</gray>"))));

        List<PerkDefinition> perks = skillTree.getPerksForArchetype(profile.getArchetype());
        if (holder.getBranch() != null && !holder.getBranch().isBlank()) {
            perks = skillTree.getPerksForBranch(profile.getArchetype(), holder.getBranch());
        }
        perks = new ArrayList<>(perks);
        perks.sort((a, b) -> {
            int tier = Integer.compare(a.getTier(), b.getTier());
            if (tier != 0) {
                return tier;
            }
            return a.getName().compareToIgnoreCase(b.getName());
        });

        int index = 0;
        for (PerkDefinition perk : perks) {
            if (index >= TREE_SLOTS.length) {
                break;
            }
            int slot = TREE_SLOTS[index++];
            SkillTreeManager.PerkStatus status = skillTree.getPerkStatus(profile, perk);
            inventory.setItem(slot, createPerkItem(messages, perk, status, profile));
            if (status == SkillTreeManager.PerkStatus.AVAILABLE) {
                holder.mapSlot(slot, perk.getId());
            }
        }

        Set<String> branches = new LinkedHashSet<>();
        for (PerkDefinition perk : skillTree.getPerksForArchetype(profile.getArchetype())) {
            if (!perk.getBranch().isBlank()) {
                branches.add(perk.getBranch());
            }
        }
        int branchSlot = 48;
        for (String branch : branches) {
            if (branchSlot > 50) {
                break;
            }
            inventory.setItem(branchSlot++, actionItem(messages, Material.BOOK,
                    messages.parse("<yellow>" + branch + "</yellow>"),
                    List.of(messages.parse("<gray>Filtrar ramo</gray>")), false));
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
