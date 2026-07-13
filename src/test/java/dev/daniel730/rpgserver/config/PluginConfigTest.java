package dev.daniel730.rpgserver.config;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/** Tests for typed config reads, including the newly-wired {@code quests.starter-quest-id}. */
public class PluginConfigTest {

    private static PluginConfig config(String body) throws Exception {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.loadFromString(body);
        return new PluginConfig(yaml);
    }

    @Test
    public void starterQuestIdDefaultsToWelcome() throws Exception {
        PluginConfig config = config("");
        assertEquals("welcome", config.getStarterQuestId());
    }

    @Test
    public void starterQuestIdIsConfigurable() throws Exception {
        PluginConfig config = config(String.join("\n",
                "quests:",
                "  starter-quest-id: first_steps"));
        assertEquals("first_steps", config.getStarterQuestId());
    }

    @Test
    public void maxActiveQuestsDefaultsToThree() throws Exception {
        assertEquals(3, config("").getMaxActiveQuests());
        assertEquals(5, config("quests:\n  max-active: 5").getMaxActiveQuests());
    }

    @Test
    public void defaultsAreSane() throws Exception {
        PluginConfig config = config("");
        assertTrue(config.isCivsEnabled());
        assertTrue(config.isAllowAbandon());
    }
}
