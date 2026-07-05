package dev.daniel730.rpgserver.hook;

import dev.daniel730.rpgserver.RpgServerPlugin;
import dev.daniel730.rpgserver.listener.CustomMobQuestListener;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Registers for Civs {@code CustomMobKillEvent} via reflection (CIVS-010).
 * No-ops until Civs ships the event class and fires it on custom mob death.
 */
public final class CivsCustomMobHook {

    private static final String EVENT_CLASS =
            "org.redcastlemedia.multitallented.civs.events.CustomMobKillEvent";

    private final RpgServerPlugin plugin;
    private boolean enabled;
    private Listener customMobListener;

    public CivsCustomMobHook(RpgServerPlugin plugin) {
        this.plugin = plugin;
    }

    public void enable() {
        if (!plugin.getPluginConfig().isCivsEnabled()) {
            return;
        }
        Plugin civs = Bukkit.getPluginManager().getPlugin("Civs");
        if (civs == null) {
            return;
        }
        try {
            Class<?> eventClass = Class.forName(EVENT_CLASS);
            Method getMobId = eventClass.getMethod("getMobId");
            KillerAccessor killerAccessor = resolveKillerAccessor(eventClass);

            customMobListener = new CustomMobQuestListener(plugin);
            EventExecutor executor = (ignored, event) -> {
                if (!eventClass.isInstance(event)) {
                    return;
                }
                if (event instanceof Cancellable cancellable && cancellable.isCancelled()) {
                    return;
                }
                try {
                    Player killer = killerAccessor.resolve(event);
                    String mobId = (String) getMobId.invoke(event);
                    if (killer == null || mobId == null || mobId.isBlank()) {
                        return;
                    }
                    plugin.getQuestManager().handleCustomMobKill(killer, mobId);
                } catch (ReflectiveOperationException ex) {
                    plugin.getLogger().log(Level.FINE, "Falha ao processar CustomMobKillEvent", ex);
                }
            };

            Bukkit.getPluginManager().registerEvent(
                    (Class) eventClass,
                    customMobListener,
                    EventPriority.MONITOR,
                    executor,
                    plugin,
                    false
            );
            enabled = true;
            plugin.getLogger().info("Civs custom mob hook ativo (CustomMobKillEvent via reflexão).");
        } catch (ClassNotFoundException ex) {
            plugin.getLogger().info(
                    "CustomMobKillEvent ainda não existe no Civs — objetivos custom_mob_kill aguardam CIVS-010.");
        } catch (ReflectiveOperationException ex) {
            plugin.getLogger().warning(
                    "CustomMobKillEvent API incompatível — objetivos custom_mob_kill inativos: " + ex.getMessage());
        }
    }

    private static KillerAccessor resolveKillerAccessor(Class<?> eventClass) throws ReflectiveOperationException {
        try {
            // Prefer the credited player: for quest-owned mobs (spawnForQuest), Civs credits the
            // quest owner when a party member within partyRadius lands the kill, not the killer.
            Method getCreditedPlayer = eventClass.getMethod("getCreditedPlayer");
            Method getKillerForFallback = eventClass.getMethod("getKiller");
            return event -> {
                Player credited = (Player) getCreditedPlayer.invoke(event);
                return credited != null ? credited : (Player) getKillerForFallback.invoke(event);
            };
        } catch (NoSuchMethodException ignored) {
            // legacy Civs API without party kill credit
        }
        try {
            Method getKiller = eventClass.getMethod("getKiller");
            return event -> (Player) getKiller.invoke(event);
        } catch (NoSuchMethodException ignored) {
            // legacy/alternate Civs API
        }
        try {
            Method getKillerId = eventClass.getMethod("getKillerId");
            return event -> {
                UUID killerId = (UUID) getKillerId.invoke(event);
                return killerId == null ? null : Bukkit.getPlayer(killerId);
            };
        } catch (NoSuchMethodException ignored) {
            Method getUuid = eventClass.getMethod("getUuid");
            return event -> {
                UUID killerId = (UUID) getUuid.invoke(event);
                return killerId == null ? null : Bukkit.getPlayer(killerId);
            };
        }
    }

    @FunctionalInterface
    private interface KillerAccessor {
        Player resolve(Object event) throws ReflectiveOperationException;
    }

    public void disable() {
        if (customMobListener != null) {
            HandlerList.unregisterAll(customMobListener);
            customMobListener = null;
        }
        enabled = false;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
