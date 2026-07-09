package dev.daniel730.rpgserver.listener;

import dev.daniel730.rpgserver.RpgServerPlugin;
import dev.daniel730.rpgserver.hook.CivsCustomMobHook;
import dev.daniel730.rpgserver.hook.CivsHook;
import dev.daniel730.rpgserver.testutil.BukkitTestSupport;
import org.bukkit.plugin.Plugin;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.junit.BeforeClass;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CivsIntegrationLifecycleListenerTest {

    private RpgServerPlugin plugin;
    private CivsHook civsHook;
    private CivsCustomMobHook civsCustomMobHook;
    private CivsIntegrationLifecycleListener listener;

    @BeforeClass
    public static void installBukkit() {
        BukkitTestSupport.install();
    }

    @Before
    public void setUp() {
        plugin = mock(RpgServerPlugin.class);
        civsHook = mock(CivsHook.class);
        civsCustomMobHook = mock(CivsCustomMobHook.class);
        when(plugin.getCivsHook()).thenReturn(civsHook);
        when(plugin.getCivsCustomMobHook()).thenReturn(civsCustomMobHook);
        listener = new CivsIntegrationLifecycleListener(plugin);
    }

    @Test
    public void onPluginDisablePausesCivsHook() {
        Plugin civs = mock(Plugin.class);
        when(civs.getName()).thenReturn("Civs");

        listener.onPluginDisable(new PluginDisableEvent(civs));

        verify(civsHook).onCivsDisabled();
        verify(civsCustomMobHook).disable();
    }

    @Test
    public void onPluginEnableRefreshesCivsIntegration() {
        Plugin civs = mock(Plugin.class);
        when(civs.getName()).thenReturn("Civs");

        listener.onPluginEnable(new PluginEnableEvent(civs));

        verify(civsHook).refresh();
        verify(plugin).reregisterCivsIntegrationListeners();
    }

    @Test
    public void ignoresOtherPlugins() {
        Plugin other = mock(Plugin.class);
        when(other.getName()).thenReturn("Vault");

        listener.onPluginDisable(new PluginDisableEvent(other));
        listener.onPluginEnable(new PluginEnableEvent(other));

        verify(civsHook, org.mockito.Mockito.never()).onCivsDisabled();
        verify(civsHook, org.mockito.Mockito.never()).refresh();
    }
}
