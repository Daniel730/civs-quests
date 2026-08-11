package dev.daniel730.rpgserver.quest;

import dev.daniel730.rpgserver.RpgServerPlugin;
import dev.daniel730.rpgserver.config.PluginConfig;
import dev.daniel730.rpgserver.hook.CivsHook;
import dev.daniel730.rpgserver.profile.PlayerProfile;
import dev.daniel730.rpgserver.profile.ProfileManager;
import dev.daniel730.rpgserver.util.MessageUtil;
import org.bukkit.entity.Player;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class HuntSpawnServiceTest {

    private RpgServerPlugin plugin;
    private CivsHook civsHook;
    private MessageUtil messageUtil;
    private Player player;
    private HuntSpawnService service;

    @Before
    public void setUp() {
        plugin = mock(RpgServerPlugin.class);
        civsHook = mock(CivsHook.class);
        PluginConfig pluginConfig = mock(PluginConfig.class);
        ProfileManager profileManager = mock(ProfileManager.class);
        messageUtil = mock(MessageUtil.class);
        player = mock(Player.class);
        PlayerProfile profile = mock(PlayerProfile.class);

        when(plugin.getCivsHook()).thenReturn(civsHook);
        when(plugin.getPluginConfig()).thenReturn(pluginConfig);
        when(plugin.getProfileManager()).thenReturn(profileManager);
        when(plugin.getMessageUtil()).thenReturn(messageUtil);
        when(civsHook.isEnabled()).thenReturn(true);
        when(pluginConfig.getHuntSpawnPartyRadius()).thenReturn(32.0);
        when(pluginConfig.getHuntSpawnCooldownSeconds()).thenReturn(300L);
        when(player.getUniqueId()).thenReturn(UUID.randomUUID());
        when(profileManager.getOrCreate(player)).thenReturn(profile);
        when(profile.getHuntCooldown(anyString())).thenReturn(null);

        service = new HuntSpawnService(plugin);
    }

    @Test
    public void spawnOnAccept_spawnsAmountTimes() {
        when(civsHook.spawnQuestMob(eq(player), eq("bandit_scout"), eq(32.0))).thenReturn(true);

        Quest.Objective objective = new Quest.Objective(
                "slay", "custom_mob_kill", "kill scouts", null, null, 0,
                "bandit_scout", null, 2, true, true);
        Quest quest = new Quest(
                "rescue_quartermaster", "Resgate", "neutral", "desc",
                List.of(), 2, "story", null, List.of(), QuestSchedule.NONE,
                List.of(), List.of(objective), RewardDefinition.empty());

        service.spawnOnQuestAccept(player, quest);

        verify(civsHook, times(2)).spawnQuestMob(player, "bandit_scout", 32.0);
        verify(messageUtil).send(eq(player), contains("bandit_scout ×2"));
    }
}
