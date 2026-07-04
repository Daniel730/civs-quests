package dev.daniel730.rpgserver.quest;

import dev.daniel730.rpgserver.RpgServerPlugin;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;

public final class LootTableService {

    public record LootEntry(Material material, int minAmount, int maxAmount, double chance) {
    }

    private final RpgServerPlugin plugin;
    private final Map<String, List<LootEntry>> tables = new LinkedHashMap<>();
    private final Random random = new Random();

    public LootTableService(RpgServerPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        tables.clear();
        File folder = new File(plugin.getDataFolder(), "loot-tables");
        if (!folder.exists()) {
            folder.mkdirs();
            plugin.saveResource("loot-tables/hunt_common.yml", false);
            plugin.saveResource("loot-tables/hunt_rare.yml", false);
            plugin.saveResource("loot-tables/warrior_rare.yml", false);
        }
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) {
            return;
        }
        for (File file : files) {
            try {
                YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
                String tableId = yaml.getString("id", file.getName().replace(".yml", ""));
                List<LootEntry> entries = parseEntries(yaml.getMapList("entries"));
                if (!entries.isEmpty()) {
                    tables.put(tableId.toLowerCase(Locale.ROOT), entries);
                }
            } catch (Exception ex) {
                plugin.getLogger().log(Level.WARNING, "Falha ao carregar loot table " + file.getName(), ex);
            }
        }
        plugin.getLogger().info("Carregadas " + tables.size() + " loot tables.");
    }

    private List<LootEntry> parseEntries(List<Map<?, ?>> rawEntries) {
        List<LootEntry> entries = new ArrayList<>();
        for (Map<?, ?> raw : rawEntries) {
            Object materialObj = raw.get("material");
            if (materialObj == null) {
                continue;
            }
            Material material = Material.matchMaterial(String.valueOf(materialObj));
            if (material == null || !material.isItem()) {
                continue;
            }
            int min = intValue(raw, "min-amount", intValue(raw, "amount", 1));
            int max = intValue(raw, "max-amount", min);
            double chance = doubleValue(raw, "chance", 1.0);
            entries.add(new LootEntry(material, min, max, chance));
        }
        return entries;
    }

    public List<ItemStack> roll(String tableId) {
        if (tableId == null || tableId.isBlank()) {
            return List.of();
        }
        List<LootEntry> entries = tables.get(tableId.toLowerCase(Locale.ROOT));
        if (entries == null || entries.isEmpty()) {
            return List.of();
        }
        List<ItemStack> drops = new ArrayList<>();
        for (LootEntry entry : entries) {
            if (entry.chance() < 1.0 && random.nextDouble() > entry.chance()) {
                continue;
            }
            int amount = entry.minAmount();
            if (entry.maxAmount() > entry.minAmount()) {
                amount = entry.minAmount() + random.nextInt(entry.maxAmount() - entry.minAmount() + 1);
            }
            if (amount > 0) {
                drops.add(new ItemStack(entry.material(), amount));
            }
        }
        return drops;
    }

    /** Rolls and gives the table's drops to the player. Returns the granted items (empty if the table rolled nothing). */
    public List<ItemStack> grantTable(Player player, String tableId) {
        List<ItemStack> drops = roll(tableId);
        if (drops.isEmpty()) {
            return drops;
        }
        for (ItemStack stack : drops) {
            Map<Integer, ItemStack> leftover = player.getInventory().addItem(stack);
            for (ItemStack drop : leftover.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), drop);
            }
        }
        return drops;
    }

    private static int intValue(Map<?, ?> raw, String key, int defaultValue) {
        Object value = raw.get(key);
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value != null) {
            try {
                return Integer.parseInt(String.valueOf(value));
            } catch (NumberFormatException ignored) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    private static double doubleValue(Map<?, ?> raw, String key, double defaultValue) {
        Object value = raw.get(key);
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        if (value != null) {
            try {
                return Double.parseDouble(String.valueOf(value));
            } catch (NumberFormatException ignored) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    public Map<String, List<LootEntry>> getTablesSnapshot() {
        return Collections.unmodifiableMap(tables);
    }
}
