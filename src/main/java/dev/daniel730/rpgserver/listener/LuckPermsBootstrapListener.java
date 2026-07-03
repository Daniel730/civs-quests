package dev.daniel730.rpgserver.listener;

import dev.daniel730.rpgserver.RpgServerPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;

/**
 * LuckPerms registers {@link net.luckperms.api.LuckPermsProvider} after full plugin load;
 * connect once the server finishes startup instead of during RPG onEnable.
 */
public final class LuckPermsBootstrapListener implements Listener {

    private final RpgServerPlugin plugin;

    public LuckPermsBootstrapListener(RpgServerPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onServerLoad(ServerLoadEvent event) {
        if (event.getType() != ServerLoadEvent.LoadType.STARTUP) {
            return;
        }
        plugin.getLuckPermsHook().enable();
    }
}
