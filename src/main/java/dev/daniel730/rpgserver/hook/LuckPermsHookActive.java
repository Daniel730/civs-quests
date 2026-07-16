package dev.daniel730.rpgserver.hook;

import dev.daniel730.rpgserver.RpgServerPlugin;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.types.InheritanceNode;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.concurrent.CompletableFuture;

/**
 * Active LuckPerms implementation — loaded only when LuckPerms is present
 * ({@link SoftHookFactory}).
 */
public final class LuckPermsHookActive extends LuckPermsHook {

    private LuckPerms luckPerms;

    public LuckPermsHookActive(RpgServerPlugin plugin) {
        super(plugin);
    }

    @Override
    public void enable() {
        if (!plugin.getPluginConfig().isLuckPermsEnabled()) {
            enabled = false;
            luckPerms = null;
            return;
        }
        if (enabled) {
            return;
        }
        if (Bukkit.getPluginManager().getPlugin("LuckPerms") == null) {
            plugin.getLogger().warning("LuckPerms não encontrado.");
            return;
        }
        RegisteredServiceProvider<LuckPerms> services =
                Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        if (services != null) {
            luckPerms = services.getProvider();
        }
        if (luckPerms == null) {
            try {
                luckPerms = LuckPermsProvider.get();
            } catch (IllegalStateException ex) {
                plugin.getLogger().warning("LuckPerms API indisponível: " + ex.getMessage());
                return;
            }
        }
        enabled = true;
        plugin.getLogger().info("LuckPerms API conectada.");
    }

    @Override
    public void refresh() {
        enabled = false;
        luckPerms = null;
        enable();
    }

    public LuckPerms getLuckPerms() {
        return luckPerms;
    }

    @Override
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

    @Override
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
