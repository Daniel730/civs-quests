package dev.daniel730.rpgserver.hook;

import dev.daniel730.rpgserver.RpgServerPlugin;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Constructor;
import java.util.function.Function;
import java.util.logging.Level;

/**
 * Soft-depend hooks that import optional plugin APIs must not be loaded by the JVM
 * when that plugin is absent — {@code new Hook()} links the hard API types and can
 * abort {@code onEnable} with {@link NoClassDefFoundError}.
 * <p>
 * Load the active implementation only when the target plugin is present; otherwise
 * return a no-op base that does not reference the optional API.
 */
public final class SoftHookFactory {

    private SoftHookFactory() {
    }

    public static AuraSkillsHook auraSkills(RpgServerPlugin plugin) {
        return load(
                plugin,
                "AuraSkills",
                "dev.daniel730.rpgserver.hook.AuraSkillsHookActive",
                AuraSkillsHook.class,
                AuraSkillsHook::new);
    }

    public static LuckPermsHook luckPerms(RpgServerPlugin plugin) {
        return load(
                plugin,
                "LuckPerms",
                "dev.daniel730.rpgserver.hook.LuckPermsHookActive",
                LuckPermsHook.class,
                LuckPermsHook::new);
    }

    @SuppressWarnings("unchecked")
    static <T> T load(
            RpgServerPlugin plugin,
            String softdependName,
            String activeClassName,
            Class<T> baseType,
            Function<RpgServerPlugin, T> noopFactory) {
        Plugin soft = Bukkit.getPluginManager().getPlugin(softdependName);
        if (soft == null) {
            return noopFactory.apply(plugin);
        }
        try {
            Class<?> active = Class.forName(activeClassName, true, SoftHookFactory.class.getClassLoader());
            Constructor<?> ctor = active.getConstructor(RpgServerPlugin.class);
            Object instance = ctor.newInstance(plugin);
            if (!baseType.isInstance(instance)) {
                plugin.getLogger().warning(
                        softdependName + " hook class does not extend " + baseType.getSimpleName());
                return noopFactory.apply(plugin);
            }
            return (T) instance;
        } catch (Throwable ex) {
            plugin.getLogger().log(
                    Level.WARNING,
                    softdependName + " presente mas hook ativo falhou ao carregar — usando no-op: "
                            + ex.getClass().getSimpleName() + ": " + ex.getMessage(),
                    ex);
            return noopFactory.apply(plugin);
        }
    }

    /** Package-visible for unit tests without a live Bukkit server. */
    static boolean shouldLoadActive(boolean pluginPresent) {
        return pluginPresent;
    }
}
