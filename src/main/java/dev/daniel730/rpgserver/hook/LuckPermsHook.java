package dev.daniel730.rpgserver.hook;

import dev.daniel730.rpgserver.RpgServerPlugin;
import org.bukkit.entity.Player;

/**
 * No-op LuckPerms bridge that does not link the LuckPerms API. When LuckPerms is
 * installed, {@link SoftHookFactory} loads {@link LuckPermsHookActive} instead.
 */
public class LuckPermsHook {

    protected final RpgServerPlugin plugin;
    protected boolean enabled;

    public LuckPermsHook(RpgServerPlugin plugin) {
        this.plugin = plugin;
    }

    public void enable() {
        enabled = false;
    }

    public void refresh() {
        enabled = false;
        enable();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String questPermission(String questId) {
        return plugin.getPluginConfig().getQuestPermissionPrefix() + questId;
    }

    public boolean hasQuestPermission(Player player, String questId) {
        if (!enabled) {
            return true;
        }
        return player.hasPermission(questPermission(questId));
    }

    public boolean grantPermission(Player player, String permission) {
        return false;
    }

    public boolean grantGroup(Player player, String group) {
        return false;
    }
}
