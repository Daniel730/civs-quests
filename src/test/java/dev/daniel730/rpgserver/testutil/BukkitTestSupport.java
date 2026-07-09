package dev.daniel730.rpgserver.testutil;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.plugin.PluginManager;

import java.lang.reflect.Field;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class BukkitTestSupport {

    private static boolean installed;

    private BukkitTestSupport() {
    }

    public static void install() {
        if (installed) {
            return;
        }
        try {
            Server server = mock(Server.class);
            when(server.isPrimaryThread()).thenReturn(true);
            when(server.getPluginManager()).thenReturn(mock(PluginManager.class));
            setStaticField(Bukkit.class, "server", server);
            installed = true;
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException("Failed to install Bukkit test support", ex);
        }
    }

    private static void setStaticField(Class<?> type, String fieldName, Object value)
            throws ReflectiveOperationException {
        Field field = type.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(null, value);
    }
}
