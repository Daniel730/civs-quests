package dev.daniel730.rpgserver.quest;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/** Parsing tests for {@link RewardDefinition#fromConfig}. */
public class RewardDefinitionTest {

    private static YamlConfiguration yaml(String body) throws Exception {
        YamlConfiguration config = new YamlConfiguration();
        config.loadFromString(body);
        return config;
    }

    @Test
    public void emptySectionYieldsEmptyReward() {
        assertTrue(RewardDefinition.empty().isEmpty());
        assertTrue(RewardDefinition.fromConfig(null).isEmpty());
    }

    @Test
    public void parsesAllRewardChannels() throws Exception {
        YamlConfiguration config = yaml(String.join("\n",
                "rewards:",
                "  money: 50",
                "  skill-xp:",
                "    farming: 100",
                "    fighting: 25",
                "  civs-skill-xp:",
                "    building: 10",
                "  permission: rpg.quest.merchant_path",
                "  lp-group: rpg-merchant",
                "  essentials-kit: starter",
                "  warp: market",
                "  loot-table: frost_cache"));

        RewardDefinition rewards = RewardDefinition.fromConfig(config.getConfigurationSection("rewards"));

        assertFalse(rewards.isEmpty());
        assertEquals(50.0, rewards.getMoney(), 0.0001);
        assertEquals(100.0, rewards.getSkillXp().get("farming"), 0.0001);
        assertEquals(25.0, rewards.getSkillXp().get("fighting"), 0.0001);
        assertEquals(10.0, rewards.getCivsSkillXp().get("building"), 0.0001);
        assertEquals("rpg.quest.merchant_path", rewards.getPermission());
        assertEquals("rpg-merchant", rewards.getLpGroup());
        assertEquals("starter", rewards.getEssentialsKit());
        assertEquals("market", rewards.getWarp());
        assertEquals("frost_cache", rewards.getLootTable());
    }

    @Test
    public void blankStringsBecomeNull() throws Exception {
        YamlConfiguration config = yaml(String.join("\n",
                "rewards:",
                "  money: 0",
                "  permission: ''",
                "  loot-table: '   '"));

        RewardDefinition rewards = RewardDefinition.fromConfig(config.getConfigurationSection("rewards"));

        assertNull(rewards.getPermission());
        assertNull(rewards.getLootTable());
        assertTrue(rewards.isEmpty());
    }

    @Test
    public void skillKeysAreLowercased() throws Exception {
        YamlConfiguration config = yaml(String.join("\n",
                "rewards:",
                "  skill-xp:",
                "    FARMING: 42"));

        RewardDefinition rewards = RewardDefinition.fromConfig(config.getConfigurationSection("rewards"));
        assertEquals(42.0, rewards.getSkillXp().get("farming"), 0.0001);
    }
}
