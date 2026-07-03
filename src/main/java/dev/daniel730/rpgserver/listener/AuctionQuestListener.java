package dev.daniel730.rpgserver.listener;

import dev.daniel730.rpgserver.RpgServerPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.redcastlemedia.multitallented.civs.events.AuctionListEvent;
import org.redcastlemedia.multitallented.civs.events.AuctionPurchaseEvent;

/**
 * Civs auction house objectives via {@link AuctionListEvent} and {@link AuctionPurchaseEvent}.
 */
public final class AuctionQuestListener implements Listener {

    private final RpgServerPlugin plugin;

    public AuctionQuestListener(RpgServerPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onAuctionList(AuctionListEvent event) {
        Player seller = Bukkit.getPlayer(event.getSellerId());
        if (seller == null) {
            return;
        }
        plugin.getQuestManager().handleAuctionList(seller, 1);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onAuctionPurchase(AuctionPurchaseEvent event) {
        Player buyer = Bukkit.getPlayer(event.getBuyerId());
        if (buyer == null) {
            return;
        }
        plugin.getQuestManager().handleAuctionBuy(buyer, 1);
    }
}
