package dev.daniel730.rpgserver.hook;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SoftHookFactoryTest {

    @Test
    public void shouldLoadActiveOnlyWhenSoftDependPluginPresent() {
        assertTrue(SoftHookFactory.shouldLoadActive(true));
        assertFalse(SoftHookFactory.shouldLoadActive(false));
    }

    @Test
    public void noopHooksDoNotEnableWithoutActiveImplementation() {
        // No-op bases must construct without optional APIs on the classpath.
        AuraSkillsHook aura = new AuraSkillsHook(null);
        LuckPermsHook luck = new LuckPermsHook(null);
        aura.enable();
        luck.enable();
        assertFalse(aura.isEnabled());
        assertFalse(luck.isEnabled());
        assertFalse(aura.addSkillXp(null, "mining", 1));
        assertFalse(luck.grantPermission(null, "rpg.test"));
    }
}
