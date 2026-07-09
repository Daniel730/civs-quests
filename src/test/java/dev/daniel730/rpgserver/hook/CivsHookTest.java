package dev.daniel730.rpgserver.hook;

import dev.daniel730.rpgserver.RpgServerPlugin;
import dev.daniel730.rpgserver.config.PluginConfig;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CivsHookTest {

    private RpgServerPlugin plugin;
    private PluginConfig pluginConfig;
    private CivsHook hook;
    private final UUID playerId = UUID.randomUUID();

    @Before
    public void setUp() {
        plugin = mock(RpgServerPlugin.class);
        pluginConfig = mock(PluginConfig.class);
        when(plugin.getPluginConfig()).thenReturn(pluginConfig);
        when(plugin.getLogger()).thenReturn(Logger.getLogger("test"));
        hook = new CivsHook(plugin);
    }

    @Test
    public void withCivsReturnsFallbackWhenCivsIntegrationDisabled() {
        when(pluginConfig.isCivsEnabled()).thenReturn(false);
        hook.enable();

        assertFalse(hook.isEnabled());
        assertFalse(hook.isCivsMenuOpen(playerId));
        assertEquals(Integer.MAX_VALUE, hook.getCivsMenuHistorySize(playerId));
        assertFalse(hook.openMenu(null, "main"));
    }

    @Test
    public void onCivsDisabledClearsEnabledState() {
        hook.onCivsDisabled();

        assertFalse(hook.isEnabled());
        assertEquals(Integer.MAX_VALUE, hook.getCivsMenuHistorySize(playerId));
    }
}
