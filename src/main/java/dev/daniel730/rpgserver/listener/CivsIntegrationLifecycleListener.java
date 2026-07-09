package dev.daniel730.rpgserver.listener;

import dev.daniel730.rpgserver.RpgServerPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;

/**
 * Keeps {@link dev.daniel730.rpgserver.hook.CivsHook} in sync when Civs is reloaded on a live server.
 */
public final class CivsIntegrationLifecycleListener implements Listener {

    private final RpgServerPlugin plugin;

    public CivsIntegrationLifecycleListener(RpgServerPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPluginDisable(PluginDisableEvent event) {
        if ("Civs".equals(event.getPlugin().getName())) {
            plugin.getCivsHook().onCivsDisabled();
            if (plugin.getCivsCustomMobHook() != null) {
                plugin.getCivsCustomMobHook().disable();
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPluginEnable(PluginEnableEvent event) {
        if ("Civs".equals(event.getPlugin().getName())) {
            plugin.getCivsHook().refresh();
            plugin.reregisterCivsIntegrationListeners();
        }
    }
}
