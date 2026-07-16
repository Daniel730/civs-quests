package dev.daniel730.rpgserver.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class TransientHudChannelTest {

    @Test
    public void parsesConfigValues() {
        assertEquals(TransientHudChannel.AUTO, TransientHudChannel.fromConfig(null));
        assertEquals(TransientHudChannel.CHAT, TransientHudChannel.fromConfig("chat"));
        assertEquals(TransientHudChannel.ACTIONBAR, TransientHudChannel.fromConfig("action-bar"));
        assertEquals(TransientHudChannel.NONE, TransientHudChannel.fromConfig("none"));
    }

    @Test
    public void autoDefersActionBarWhenAuraSkillsPresent() {
        assertTrue(TransientHudChannel.AUTO.usesChat(true));
        assertFalse(TransientHudChannel.AUTO.usesActionBar(true));
        assertTrue(TransientHudChannel.AUTO.usesActionBar(false));
        assertFalse(TransientHudChannel.AUTO.usesChat(false));
    }
}
