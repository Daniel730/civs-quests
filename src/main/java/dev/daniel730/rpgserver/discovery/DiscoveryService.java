package dev.daniel730.rpgserver.discovery;

import dev.daniel730.rpgserver.RpgServerPlugin;
import dev.daniel730.rpgserver.profile.PlayerProfile;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Locale;

public final class DiscoveryService {

    private final RpgServerPlugin plugin;
    private final DiscoveryRegistry registry;

    public DiscoveryService(RpgServerPlugin plugin, DiscoveryRegistry registry) {
        this.plugin = plugin;
        this.registry = registry;
    }

    public DiscoveryRegistry getRegistry() {
        return registry;
    }

    public void checkPlayerLocation(Player player, Location location) {
        if (player == null || location == null) {
            return;
        }
        PlayerProfile profile = plugin.getProfileManager().getOrCreate(player);
        boolean changed = false;

        for (PoiDefinition poi : registry.getAllPois()) {
            if (!poi.contains(location)) {
                continue;
            }
            if (profile.hasDiscoveredPoi(poi.getId())) {
                continue;
            }
            profile.discoverPoi(poi.getId());
            plugin.getQuestManager().handleDiscoverPoi(player, poi.getId());
            plugin.getMessageUtil().send(player,
                    "<aqua>★</aqua> <white>Descoberta:</white> <yellow>" + poi.getName() + "</yellow>");
            changed = true;
        }

        String biomeKey = location.getBlock().getBiome().getKey().getKey();
        if (biomeKey != null && !biomeKey.isBlank()) {
            String normalized = biomeKey.toLowerCase(Locale.ROOT);
            if (!profile.hasDiscoveredBiome(normalized)) {
                profile.discoverBiome(normalized);
                plugin.getQuestManager().handleDiscoverBiome(player, normalized);
                changed = true;
            }
        }

        if (changed) {
            plugin.getProfileManager().markDirty(player.getUniqueId());
        }
    }

    public boolean markPoiDiscovered(Player player, String poiId) {
        PoiDefinition poi = registry.getPoi(poiId);
        if (poi == null) {
            return false;
        }
        PlayerProfile profile = plugin.getProfileManager().getOrCreate(player);
        if (profile.hasDiscoveredPoi(poi.getId())) {
            return true;
        }
        profile.discoverPoi(poi.getId());
        plugin.getQuestManager().handleDiscoverPoi(player, poi.getId());
        plugin.getProfileManager().markDirty(player.getUniqueId());
        return true;
    }
}
