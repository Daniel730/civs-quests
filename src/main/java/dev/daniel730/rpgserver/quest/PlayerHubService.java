package dev.daniel730.rpgserver.quest;

import dev.daniel730.rpgserver.RpgServerPlugin;
import dev.daniel730.rpgserver.gui.PlayerHubGui;
import dev.daniel730.rpgserver.gui.PlayerHubHolder;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.Map;

/**
 * Player quick-access hub — inventory GUI (never a written book).
 * Opens via {@code /rpg hub}, {@code /rpg menu}, {@code /rpg guide}, {@code /rpg book},
 * or right-clicking the hub compass item (tagged with PDC {@code rpg-hub-item}).
 */
public final class PlayerHubService {

    public static final String HUB_ITEM_KEY = "rpg-hub-item";

    private final RpgServerPlugin plugin;
    private final NamespacedKey hubItemKey;

    public PlayerHubService(RpgServerPlugin plugin) {
        this.plugin = plugin;
        this.hubItemKey = new NamespacedKey(plugin, HUB_ITEM_KEY);
    }

    public void openHub(Player player) {
        openHub(player, PlayerHubHolder.HubTab.INICIO);
    }

    public void openHub(Player player, PlayerHubHolder.HubTab tab) {
        PlayerHubHolder holder = new PlayerHubHolder();
        holder.resetNavigation(tab);
        PlayerHubGui.open(plugin, player, holder);
        plugin.getQuestFeedbackService().playJournalOpen(player);
    }

    public void refreshHub(Player player) {
        if (refreshIfOpen(player)) {
            plugin.getMessageUtil().send(player, plugin.getPluginConfig().getHubRefreshed());
        } else {
            openHub(player);
            plugin.getMessageUtil().send(player, plugin.getPluginConfig().getHubRefreshed());
        }
    }

    /** Re-renders the hub when already open; no-op if the player is in another inventory. */
    public boolean refreshIfOpen(Player player) {
        if (!(player.getOpenInventory().getTopInventory().getHolder() instanceof PlayerHubHolder holder)) {
            return false;
        }
        PlayerHubGui.render(plugin, player, holder);
        return true;
    }

    public boolean isHubOpen(Player player) {
        return player.getOpenInventory().getTopInventory().getHolder() instanceof PlayerHubHolder;
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
        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        return pdc.has(hubItemKey, PersistentDataType.BYTE);
    }

    public ItemStack createHubItem() {
        ItemStack item = new ItemStack(Material.RECOVERY_COMPASS);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(plugin.getMessageUtil().parse(plugin.getPluginConfig().getHubItemName()));
        meta.lore(List.of(plugin.getMessageUtil().parse(plugin.getPluginConfig().getHubItemLore())));
        meta.getPersistentDataContainer().set(hubItemKey, PersistentDataType.BYTE, (byte) 1);
        meta.addEnchant(Enchantment.UNBREAKING, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);
        return item;
    }
}
