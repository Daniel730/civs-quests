package dev.daniel730.rpgserver.quest;

import dev.daniel730.rpgserver.RpgServerPlugin;
import dev.daniel730.rpgserver.gui.PlayerHubGui;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Map;

/**
 * Player quick-access hub — inventory GUI (not a written book).
 * Opens via {@code /rpg hub}, {@code /rpg menu}, {@code /rpg guide}, or hub compass item.
 */
public final class PlayerHubService {

    public static final String HUB_ITEM_MARKER = "rpg-hub-compass";
    /** @deprecated legacy written-book marker — still opens hub */
    public static final String LEGACY_GUIDE_BOOK_MARKER = "rpg-guide-book";

    private final RpgServerPlugin plugin;

    public PlayerHubService(RpgServerPlugin plugin) {
        this.plugin = plugin;
    }

    public void openHub(Player player) {
        PlayerHubGui.open(plugin, player);
        plugin.getQuestFeedbackService().playJournalOpen(player);
    }

    public void openHub(Player player, dev.daniel730.rpgserver.gui.PlayerHubHolder.HubTab tab) {
        PlayerHubGui.open(plugin, player, tab);
        plugin.getQuestFeedbackService().playJournalOpen(player);
    }

    public void refreshHub(Player player) {
        openHub(player);
        plugin.getMessageUtil().send(player, plugin.getPluginConfig().getHubRefreshed());
    }

    public void giveHubItem(Player player) {
        if (hasHubItemInInventory(player)) {
            plugin.getMessageUtil().send(player, plugin.getPluginConfig().getHubAlreadyHave());
            return;
        }
        ItemStack item = createHubItem();
        Map<Integer, ItemStack> leftover = player.getInventory().addItem(item);
        if (!leftover.isEmpty()) {
            player.getWorld().dropItemNaturally(player.getLocation(), item);
            plugin.getMessageUtil().send(player, plugin.getPluginConfig().getHubInventoryFull());
        } else {
            plugin.getMessageUtil().send(player, plugin.getPluginConfig().getHubGranted());
        }
    }

    public void openOrGiveHub(Player player) {
        if (!hasHubItemInInventory(player)) {
            ItemStack item = createHubItem();
            Map<Integer, ItemStack> leftover = player.getInventory().addItem(item);
            if (!leftover.isEmpty()) {
                player.getWorld().dropItemNaturally(player.getLocation(), item);
            }
        }
        openHub(player);
    }

    public boolean hasHubItemInInventory(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (isHubItem(item)) {
                return true;
            }
        }
        return false;
    }

    public boolean isHubItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        List<Component> lore = item.getItemMeta().lore();
        if (lore == null || lore.isEmpty()) {
            return false;
        }
        String firstLine = PlainTextComponentSerializer.plainText().serialize(lore.getFirst());
        return HUB_ITEM_MARKER.equals(firstLine) || LEGACY_GUIDE_BOOK_MARKER.equals(firstLine);
    }

    public ItemStack createHubItem() {
        ItemStack item = new ItemStack(Material.RECOVERY_COMPASS);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(plugin.getMessageUtil().parse(plugin.getPluginConfig().getHubItemName()));
        meta.lore(List.of(
                Component.text(HUB_ITEM_MARKER, NamedTextColor.DARK_GRAY),
                plugin.getMessageUtil().parse(plugin.getPluginConfig().getHubItemLore())
        ));
        meta.addEnchant(Enchantment.UNBREAKING, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);
        return item;
    }
}
