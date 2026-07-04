package dev.daniel730.rpgserver.discovery;

import dev.daniel730.rpgserver.RpgServerPlugin;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
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
                    ConfigurationSection poiSection = section.getConfigurationSection(key);
                    if (poiSection == null) {
                        continue;
                    }
                    String id = poiSection.getString("id", key);
                    String name = poiSection.getString("name", id);
                    String world = poiSection.getString("world", "world");
                    double x = poiSection.getDouble("x", 0);
                    double y = poiSection.getDouble("y", 64);
                    double z = poiSection.getDouble("z", 0);
                    double radius = poiSection.getDouble("radius", 8);
                    pois.put(id.toLowerCase(Locale.ROOT), new PoiDefinition(id, name, world, x, y, z, radius));
                }
            }
            plugin.getLogger().info("Carregados " + pois.size() + " POIs.");
        } catch (Exception ex) {
            plugin.getLogger().log(Level.WARNING, "Falha ao carregar discoveries/pois.yml", ex);
        }
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
}
