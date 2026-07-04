package dev.daniel730.rpgserver.hook;

import dev.daniel730.rpgserver.RpgServerPlugin;
import dev.daniel730.rpgserver.listener.ChestShopQuestListener;
import dev.daniel730.rpgserver.util.AgentDebugLog;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public final class ChestShopHook {

    private final RpgServerPlugin plugin;
    private boolean enabled;
    private Listener transactionListener;

    public ChestShopHook(RpgServerPlugin plugin) {
        this.plugin = plugin;
    }

    public void enable() {
        if (!plugin.getPluginConfig().isChestShopEnabled()) {
            return;
        }
        Plugin chestShop = Bukkit.getPluginManager().getPlugin("ChestShop");
        if (chestShop == null) {
            plugin.getLogger().info("ChestShop ausente — objetivos shop_* inativos.");
            return;
        }
        try {
            Class<?> eventClass = Class.forName("com.Acrobot.ChestShop.Events.TransactionEvent");
            Class<?> transactionTypeClass = Class.forName(
                    "com.Acrobot.ChestShop.Events.TransactionEvent$TransactionType");
            Method getClient = eventClass.getMethod("getClient");
            Method getOwnerAccount = eventClass.getMethod("getOwnerAccount");
            Method getExactPrice = eventClass.getMethod("getExactPrice");
            Method getTransactionType = eventClass.getMethod("getTransactionType");
            Method getStock = eventClass.getMethod("getStock");

            Class<?> accountClass = Class.forName("com.Acrobot.ChestShop.Database.Account");
            Method getOwnerUuid = accountClass.getMethod("getUuid");

            Object buyType = Enum.valueOf((Class<? extends Enum>) transactionTypeClass, "BUY");
            Object sellType = Enum.valueOf((Class<? extends Enum>) transactionTypeClass, "SELL");

            transactionListener = new ChestShopQuestListener(plugin);
            EventExecutor executor = (ignored, event) -> {
                if (!eventClass.isInstance(event)) {
                    return;
                }
                try {
                    if (event instanceof org.bukkit.event.Cancellable cancellable && cancellable.isCancelled()) {
                        return;
                    }
                    Object transactionType = getTransactionType.invoke(event);
                    BigDecimal price = (BigDecimal) getExactPrice.invoke(event);
                    int itemCount = resolveItemCount(getStock.invoke(event));
                    int revenueAmount = price == null ? 0 : (int) Math.floor(price.doubleValue());
                    // #region agent log
                    AgentDebugLog.log(plugin, "H4", "ChestShopHook.TransactionEvent",
                            "chestshop transaction processed",
                            Map.of("transactionType", String.valueOf(transactionType),
                                    "itemCount", itemCount,
                                    "price", String.valueOf(price),
                                    "revenueAmount", revenueAmount));
                    // #endregion

                    if (buyType.equals(transactionType)) {
                        Object client = getClient.invoke(event);
                        if (client instanceof org.bukkit.entity.Player player) {
                            plugin.getQuestManager().handleShopBuy(player, itemCount);
                        }
                    } else if (sellType.equals(transactionType)) {
                        Object client = getClient.invoke(event);
                        if (client instanceof org.bukkit.entity.Player player) {
                            plugin.getQuestManager().handleShopSell(player, itemCount);
                        }
                    }

                    if (buyType.equals(transactionType) && revenueAmount > 0) {
                        Object ownerAccount = getOwnerAccount.invoke(event);
                        if (ownerAccount == null) {
                            return;
                        }
                        UUID ownerUuid = (UUID) getOwnerUuid.invoke(ownerAccount);
                        org.bukkit.entity.Player owner = Bukkit.getPlayer(ownerUuid);
                        if (owner != null) {
                            plugin.getQuestManager().handleShopRevenue(owner, revenueAmount);
                        }
                    }
                } catch (ReflectiveOperationException ex) {
                    plugin.getLogger().log(Level.FINE, "Falha ao processar TransactionEvent", ex);
                }
            };

            Bukkit.getPluginManager().registerEvent(
                    (Class) eventClass,
                    transactionListener,
                    EventPriority.MONITOR,
                    executor,
                    plugin,
                    true
            );
            enabled = true;
            plugin.getLogger().info("ChestShop hook ativo (TransactionEvent via reflexão).");
        } catch (ReflectiveOperationException ex) {
            plugin.getLogger().warning("ChestShop API não encontrada — objetivos shop_* inativos: " + ex.getMessage());
        }
    }

    public void disable() {
        if (transactionListener != null) {
            HandlerList.unregisterAll(transactionListener);
            transactionListener = null;
        }
        enabled = false;
    }

    public boolean isEnabled() {
        return enabled;
    }

    private static int resolveItemCount(Object stock) {
        if (!(stock instanceof org.bukkit.inventory.ItemStack[] stacks)) {
            return 1;
        }
        int total = 0;
        for (org.bukkit.inventory.ItemStack stack : stacks) {
            if (stack != null) {
                total += stack.getAmount();
            }
        }
        return Math.max(1, total);
    }
}
