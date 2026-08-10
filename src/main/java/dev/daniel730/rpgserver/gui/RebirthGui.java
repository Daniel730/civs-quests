package dev.daniel730.rpgserver.gui;

import dev.daniel730.rpgserver.RpgServerPlugin;
import dev.daniel730.rpgserver.profile.PlayerProfile;
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
import java.util.List;

public final class RebirthGui {

    private static final int SIZE = 27;
    public static final int CONFIRM_SLOT = 11;
    public static final int CANCEL_SLOT = 15;

    private RebirthGui() {
    }

    public static void open(RpgServerPlugin plugin, Player player) {
        RebirthHolder holder = new RebirthHolder();
        Inventory inventory = Bukkit.createInventory(holder, SIZE,
                plugin.getMessageUtil().parse("<gold><bold>Confirmar Renascimento</bold></gold>"));
        holder.setInventory(inventory);
        boolean reduced = GuiMotion.reducedMotion(plugin);
        if (!reduced) {
            GuiMotion.skeleton(inventory, new int[]{CONFIRM_SLOT, CANCEL_SLOT, 4});
        }
        player.openInventory(inventory);
        render(plugin, player, holder, GuiMotion.MotionProfile.RARE);
    }

    public static void render(RpgServerPlugin plugin, Player player, RebirthHolder holder) {
        render(plugin, player, holder, GuiMotion.MotionProfile.INSTANT);
    }

    public static void render(RpgServerPlugin plugin, Player player, RebirthHolder holder,
                              GuiMotion.MotionProfile profile) {
        GuiMotion.cancel(holder);
        MessageUtil messages = plugin.getMessageUtil();
        PlayerProfile profileData = plugin.getProfileManager().getOrCreate(player);
        Inventory inventory = holder.getInventory();
        boolean reduced = GuiMotion.reducedMotion(plugin);
        GuiMotion.MotionProfile effective = GuiMotion.effectiveProfile(profile, reduced);

        inventory.clear();

        ItemStack pane = filler(Material.GRAY_STAINED_GLASS_PANE);
        for (int i = 0; i < SIZE; i++) {
            inventory.setItem(i, pane);
        }

        int refund = (int) Math.floor(profileData.getEssenceSpent()
                * plugin.getPluginConfig().getRebirthEssenceRefundPercent());

        List<GuiMotion.SlotItem> content = new ArrayList<>();
        content.add(new GuiMotion.SlotItem(4, infoItem(messages,
                messages.parse("<yellow>Renascimento " + (profileData.getRebirthCount() + 1) + "</yellow>"),
                List.of(
                        messages.parse("<gray>Perks e progresso de quests serão reiniciados.</gray>"),
                        messages.parse("<gray>Codex e descobertas são preservados.</gray>"),
                        messages.parse("<light_purple>Essência reembolsada:</light_purple> <white>" + refund + "</white>")
                ))));
        content.add(new GuiMotion.SlotItem(CONFIRM_SLOT, actionItem(Material.LIME_CONCRETE,
                messages.parse("<green><bold>Confirmar</bold></green>"),
                List.of(messages.parse("<gray>Renascer agora</gray>")), true)));
        content.add(new GuiMotion.SlotItem(CANCEL_SLOT, actionItem(Material.RED_CONCRETE,
                messages.parse("<red><bold>Cancelar</bold></red>"),
                List.of(messages.parse("<gray>Voltar sem renascer</gray>")), false)));

        if (effective != GuiMotion.MotionProfile.INSTANT) {
            GuiMotion.skeleton(inventory, new int[]{CONFIRM_SLOT, CANCEL_SLOT, 4});
        }
        GuiMotion.enter(plugin, holder, inventory, content, effective, false);
    }

    private static ItemStack filler(Material material) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.empty());
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack infoItem(MessageUtil messages, Component name, List<Component> lore) {
        ItemStack item = new ItemStack(Material.TOTEM_OF_UNDYING);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(name);
        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack actionItem(Material material, Component name, List<Component> lore, boolean glow) {
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
