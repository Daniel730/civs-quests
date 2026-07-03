package dev.daniel730.rpgserver.listener;

import dev.daniel730.rpgserver.RpgServerPlugin;
import org.bukkit.event.Listener;

/**
 * Marker listener registered for Civs {@code CustomMobKillEvent} via reflection.
 */
public final class CustomMobQuestListener implements Listener {

    public CustomMobQuestListener(RpgServerPlugin plugin) {
        // Handler logic lives in CivsCustomMobHook EventExecutor.
    }
}
