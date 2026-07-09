package dev.daniel730.rpgserver.discovery;

import dev.daniel730.rpgserver.RpgServerPlugin;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;

public final class DiscoveryRegistry {

    private final RpgServerPlugin plugin;
    private final Map<String, PoiDefinition> pois = new LinkedHashMap<>();

    public DiscoveryRegistry(RpgServerPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        pois.clear();
        File folder = new File(plugin.getDataFolder(), "discoveries");
        if (!folder.exists()) {
            folder.mkdirs();
            plugin.saveResource("discoveries/pois.yml", false);
        }
        File poisFile = new File(folder, "pois.yml");
        if (!poisFile.exists()) {
            plugin.saveResource("discoveries/pois.yml", false);
        }
        try {
            YamlConfiguration yaml = YamlConfiguration.loadConfiguration(poisFile);
            ConfigurationSection section = yaml.getConfigurationSection("pois");
            if (section != null) {
                for (String key : section.getKeys(false)) {
                    PoiDefinition poi = parsePoiSection(key, section.getConfigurationSection(key));
                    if (poi != null) {
                        pois.put(poi.getId().toLowerCase(Locale.ROOT), poi);
                    }
                }
            }
            plugin.getLogger().info("Carregados " + pois.size() + " POIs.");
        } catch (Exception ex) {
            plugin.getLogger().log(Level.WARNING, "Falha ao carregar discoveries/pois.yml", ex);
        }
    }

    private PoiDefinition parsePoiSection(String key, ConfigurationSection poiSection) {
        if (poiSection == null) {
            return null;
        }
        String id = poiSection.getString("id", key);
        String name = poiSection.getString("name", id);
        String world = poiSection.getString("world", "world");
        double x = poiSection.getDouble("x", 0);
        double y = poiSection.getDouble("y", 64);
        double z = poiSection.getDouble("z", 0);
        double radius = poiSection.getDouble("radius", 8);
        return new PoiDefinition(id, name, world, x, y, z, radius);
    }

    public PoiDefinition getPoi(String poiId) {
        if (poiId == null) {
            return null;
        }
        return pois.get(poiId.toLowerCase(Locale.ROOT));
    }

    public Collection<PoiDefinition> getAllPois() {
        return Collections.unmodifiableCollection(pois.values());
    }

    public PoiDefinition upsertPoi(String poiId, String displayName, Location location, double radius) {
        if (poiId == null || poiId.isBlank() || location == null || location.getWorld() == null) {
            return null;
        }
        String normalizedId = poiId.toLowerCase(Locale.ROOT);
        PoiDefinition existing = pois.get(normalizedId);
        String name = displayName == null || displayName.isBlank()
                ? (existing == null ? poiId : existing.getName())
                : displayName;
        double effectiveRadius = radius > 0
                ? radius
                : (existing == null ? 40 : existing.getRadius());
        PoiDefinition poi = new PoiDefinition(
                poiId,
                name,
                location.getWorld().getName(),
                location.getX(),
                location.getY(),
                location.getZ(),
                effectiveRadius);
        pois.put(normalizedId, poi);
        return poi;
    }

    public boolean savePoi(PoiDefinition poi) {
        if (poi == null) {
            return false;
        }
        pois.put(poi.getId().toLowerCase(Locale.ROOT), poi);
        return savePois();
    }

    public boolean savePois() {
        File folder = new File(plugin.getDataFolder(), "discoveries");
        if (!folder.exists()) {
            folder.mkdirs();
        }
        File poisFile = new File(folder, "pois.yml");
        YamlConfiguration yaml = poisFile.exists()
                ? YamlConfiguration.loadConfiguration(poisFile)
                : new YamlConfiguration();
        yaml.set("pois", null);
        for (PoiDefinition poi : pois.values()) {
            String key = poi.getId();
            yaml.set("pois." + key + ".id", poi.getId());
            yaml.set("pois." + key + ".name", poi.getName());
            yaml.set("pois." + key + ".world", poi.getWorldName());
            yaml.set("pois." + key + ".x", Math.round(poi.getX()));
            yaml.set("pois." + key + ".y", Math.round(poi.getY()));
            yaml.set("pois." + key + ".z", Math.round(poi.getZ()));
            yaml.set("pois." + key + ".radius", (int) poi.getRadius());
        }
        try {
            yaml.save(poisFile);
            return true;
        } catch (IOException ex) {
            plugin.getLogger().log(Level.WARNING, "Falha ao salvar discoveries/pois.yml", ex);
            return false;
        }
    }

    public String exportYamlSnippet() {
        StringBuilder builder = new StringBuilder("pois:\n");
        for (PoiDefinition poi : pois.values()) {
            builder.append("  ").append(poi.getId()).append(":\n");
            builder.append("    id: ").append(poi.getId()).append('\n');
            builder.append("    name: \"").append(poi.getName()).append("\"\n");
            builder.append("    world: ").append(poi.getWorldName()).append('\n');
            builder.append("    x: ").append(Math.round(poi.getX())).append('\n');
            builder.append("    y: ").append(Math.round(poi.getY())).append('\n');
            builder.append("    z: ").append(Math.round(poi.getZ())).append('\n');
            builder.append("    radius: ").append((int) poi.getRadius()).append('\n');
        }
        return builder.toString();
    }
}
