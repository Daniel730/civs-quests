package dev.daniel730.rpgserver.hook;

import dev.daniel730.rpgserver.RpgServerPlugin;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.types.InheritanceNode;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;

public final class LuckPermsHook {

    private final RpgServerPlugin plugin;
    private LuckPerms luckPerms;
    private boolean enabled;

    public LuckPermsHook(RpgServerPlugin plugin) {
        this.plugin = plugin;
    }

    public void enable() {
        if (!plugin.getPluginConfig().isLuckPermsEnabled()) {
            return;
        }
        if (Bukkit.getPluginManager().getPlugin("LuckPerms") == null) {
            plugin.getLogger().warning("LuckPerms não encontrado.");
            return;
        }
        try {
            luckPerms = LuckPermsProvider.get();
            enabled = luckPerms != null;
            if (enabled) {
                plugin.getLogger().info("LuckPerms API conectada.");
            }
        } catch (IllegalStateException ex) {
            plugin.getLogger().warning("LuckPerms API indisponível: " + ex.getMessage());
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public LuckPerms getLuckPerms() {
        return luckPerms;
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
        if (!enabled || permission == null || permission.isBlank()) {
            return false;
        }
        User user = luckPerms.getUserManager().getUser(player.getUniqueId());
        if (user == null) {
            return false;
        }
        Node node = Node.builder(permission).build();
        user.data().add(node);
        CompletableFuture<Void> save = luckPerms.getUserManager().saveUser(user);
        save.exceptionally(ex -> {
            plugin.getLogger().warning("Falha ao salvar permissão LuckPerms: " + ex.getMessage());
            return null;
        });
        return true;
    }

    public boolean grantGroup(Player player, String group) {
        if (!enabled || group == null || group.isBlank()) {
            return false;
        }
        User user = luckPerms.getUserManager().getUser(player.getUniqueId());
        if (user == null) {
            return false;
        }
        InheritanceNode node = InheritanceNode.builder(group).build();
        user.data().add(node);
        CompletableFuture<Void> save = luckPerms.getUserManager().saveUser(user);
        save.exceptionally(ex -> {
            plugin.getLogger().warning("Falha ao salvar grupo LuckPerms: " + ex.getMessage());
            return null;
        });
        return true;
    }
}
