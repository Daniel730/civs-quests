package dev.daniel730.rpgserver.hook;

import dev.daniel730.rpgserver.RpgServerPlugin;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Locale;
import java.util.logging.Level;

public final class VeinMinerHook {

    private final RpgServerPlugin plugin;
    private boolean enabled;
    private Listener veinMineListener;

    public VeinMinerHook(RpgServerPlugin plugin) {
        this.plugin = plugin;
    }

    public void enable() {
        if (!plugin.getPluginConfig().isVeinMinerEnabled()) {
            return;
        }
        if (Bukkit.getPluginManager().getPlugin("VeinMiner") == null) {
            plugin.getLogger().info("VeinMiner ausente — objetivo vein_mine inativo.");
            return;
        }
        try {
            Class<?> eventClass = Class.forName("wtf.choco.veinminer.api.event.player.PlayerVeinMineEvent");
            Method getPlayer = eventClass.getMethod("getPlayer");
            Method getBlock = eventClass.getMethod("getBlock");
            Method getBlocks = eventClass.getMethod("getBlocks");

            veinMineListener = new Listener() {
            };
            EventExecutor executor = (ignored, event) -> {
                if (!eventClass.isInstance(event)) {
                    return;
                }
                try {
                    Player player = (Player) getPlayer.invoke(event);
                    Block origin = (Block) getBlock.invoke(event);
                    if (player == null || origin == null) {
                        return;
                    }
                    String blockKey = origin.getType().name().toLowerCase(Locale.ROOT);
                    int increment = 1;
                    Object blocksObj = getBlocks.invoke(event);
                    if (blocksObj instanceof Collection<?> blocks && !blocks.isEmpty()) {
                        increment = blocks.size();
                    }
                    plugin.getQuestManager().handleVeinMine(player, blockKey, increment);
                } catch (ReflectiveOperationException ex) {
                    plugin.getLogger().log(Level.FINE, "Falha ao processar PlayerVeinMineEvent", ex);
                }
            };

            Bukkit.getPluginManager().registerEvent(
                    (Class) eventClass,
                    veinMineListener,
                    EventPriority.MONITOR,
                    executor,
                    plugin,
                    false
            );
            enabled = true;
            plugin.getLogger().info("VeinMiner hook ativo (PlayerVeinMineEvent via reflexão).");
        } catch (ReflectiveOperationException ex) {
            plugin.getLogger().warning("VeinMiner API não encontrada — objetivo vein_mine inativo: " + ex.getMessage());
        }
    }

    public void disable() {
        if (veinMineListener != null) {
            HandlerList.unregisterAll(veinMineListener);
            veinMineListener = null;
        }
        enabled = false;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
