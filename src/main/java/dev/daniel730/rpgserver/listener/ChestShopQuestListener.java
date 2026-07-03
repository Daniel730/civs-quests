package dev.daniel730.rpgserver.listener;

import dev.daniel730.rpgserver.RpgServerPlugin;
import org.bukkit.event.Listener;

/**
 * Marker listener registered for ChestShop {@code TransactionEvent} via reflection.
 */
public final class ChestShopQuestListener implements Listener {

    public ChestShopQuestListener(RpgServerPlugin plugin) {
        // Handler logic lives in ChestShopHook EventExecutor.
    }
}
