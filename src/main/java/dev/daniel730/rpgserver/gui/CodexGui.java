package dev.daniel730.rpgserver.gui;

import dev.daniel730.rpgserver.RpgServerPlugin;
import dev.daniel730.rpgserver.discovery.PoiDefinition;
import dev.daniel730.rpgserver.profile.PlayerProfile;
import dev.daniel730.rpgserver.util.ArchetypeUtil;
import dev.daniel730.rpgserver.util.MessageUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class CodexGui {

    private static final int SIZE = 54;
    private static final int CONTENT_START = 9;
    private static final int CONTENT_END = 44;
    private static final int ENTRIES_PER_PAGE = CONTENT_END - CONTENT_START + 1;

    private CodexGui() {
    }

    public static void open(RpgServerPlugin plugin, Player player) {
        CodexHolder holder = new CodexHolder();
        Inventory inventory = Bukkit.createInventory(holder, SIZE,
                plugin.getMessageUtil().parse("<aqua><bold>Codex de Exploração</bold></aqua>"));
        holder.setInventory(inventory);
        render(plugin, player, holder, 0);
        player.openInventory(inventory);
    }

    public static void render(RpgServerPlugin plugin, Player player, CodexHolder holder, int page) {
        PlayerProfile profile = plugin.getProfileManager().getOrCreate(player);
        MessageUtil messages = plugin.getMessageUtil();
        Inventory inventory = holder.getInventory();
        inventory.clear();

        fillBorder(inventory, profile.getArchetype());
        List<CodexEntry> entries = buildEntries(plugin, profile);
        int totalPages = Math.max(1, (entries.size() + ENTRIES_PER_PAGE - 1) / ENTRIES_PER_PAGE);
        int clampedPage = Math.max(0, Math.min(page, totalPages - 1));
        holder.setPage(clampedPage);

        inventory.setItem(4, headerItem(messages, profile, clampedPage, totalPages));

        int start = clampedPage * ENTRIES_PER_PAGE;
        int slot = CONTENT_START;
        for (int i = start; i < entries.size() && slot <= CONTENT_END; i++, slot++) {
            CodexEntry entry = entries.get(i);
            inventory.setItem(slot, entryItem(messages, entry));
        }
    }

    private static List<CodexEntry> buildEntries(RpgServerPlugin plugin, PlayerProfile profile) {
        List<CodexEntry> entries = new ArrayList<>();
        for (PoiDefinition poi : plugin.getDiscoveryService().getRegistry().getAllPois()) {
            boolean discovered = profile.hasDiscoveredPoi(poi.getId());
            entries.add(new CodexEntry("POI", poi.getName(), discovered));
        }
        for (String biomeId : profile.getDiscoveredBiomes()) {
            entries.add(new CodexEntry("Bioma", formatBiome(biomeId), true));
        }
        return entries;
    }

    private static String formatBiome(String biomeId) {
        if (biomeId == null) {
            return "Desconhecido";
        }
        return biomeId.replace('_', ' ');
    }

    private record CodexEntry(String type, String name, boolean discovered) {
    }

    private static ItemStack headerItem(MessageUtil messages, PlayerProfile profile, int page, int totalPages) {
        ItemStack item = new ItemStack(Material.WRITABLE_BOOK);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(messages.parse("<aqua><bold>Codex</bold></aqua>"));
        meta.lore(List.of(
                messages.parse("<gray>POIs descobertos:</gray> <white>" + profile.getDiscoveredPois().size() + "</white>"),
                messages.parse("<gray>Biomas:</gray> <white>" + profile.getDiscoveredBiomes().size() + "</white>"),
                messages.parse("<dark_gray>Página " + (page + 1) + "/" + totalPages + "</dark_gray>")
        ));
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack entryItem(MessageUtil messages, CodexEntry entry) {
        Material material = entry.discovered()
                ? ("POI".equals(entry.type()) ? Material.MAP : Material.GRASS_BLOCK)
                : Material.GRAY_DYE;
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        String color = entry.discovered() ? "<green>" : "<dark_gray>";
        meta.displayName(messages.parse(color + entry.name() + "</color>"));
        meta.lore(List.of(messages.parse("<gray>" + entry.type() + "</gray> "
                + (entry.discovered() ? "<green>✔</green>" : "<red>?</red>"))));
        item.setItemMeta(meta);
        return item;
    }

    private static void fillBorder(Inventory inventory, String archetype) {
        ItemStack pane = createFiller(ArchetypeUtil.glassPane(archetype));
        for (int i = 0; i < SIZE; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8) {
                inventory.setItem(i, pane);
            }
        }
    }

    private static ItemStack createFiller(Material material) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.empty());
        item.setItemMeta(meta);
        return item;
    }
}
