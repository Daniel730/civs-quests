package dev.daniel730.rpgserver.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tick-based inventory enter/skeleton helpers (Bukkit adaptation of design-motion-principles).
 * High-frequency UIs should use {@link MotionProfile#HUB} or {@link MotionProfile#INSTANT}.
 */
public final class GuiMotion {

    public enum MotionProfile {
        /** No stagger — reduced motion / keyboard refresh. */
        INSTANT(0, Integer.MAX_VALUE),
        /** Hub and tabs — short stagger. */
        HUB(1, 2),
        /** Rare screens (path picker, rebirth). */
        RARE(1, 1);

        final int ticksPerStep;
        final int slotsPerStep;

        MotionProfile(int ticksPerStep, int slotsPerStep) {
            this.ticksPerStep = ticksPerStep;
            this.slotsPerStep = slotsPerStep;
        }
    }

    public record SlotItem(int slot, ItemStack item) {
    }

    private static final Map<Object, BukkitTask> TASKS = new ConcurrentHashMap<>();

    private GuiMotion() {
    }

    public static ItemStack skeletonPane() {
        ItemStack stack = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            // Avoid Adventure Component APIs here — keep skeleton cheap for unit/CI.
            meta.setDisplayName(" ");
            stack.setItemMeta(meta);
        }
        return stack;
    }

    public static void skeleton(Inventory inventory, int[] contentSlots) {
        ItemStack pane = skeletonPane();
        for (int slot : contentSlots) {
            if (slot >= 0 && slot < inventory.getSize()) {
                inventory.setItem(slot, pane.clone());
            }
        }
    }

    public static void cancel(Object holderKey) {
        BukkitTask task = TASKS.remove(holderKey);
        if (task != null) {
            task.cancel();
        }
    }

    /**
     * Fill slots immediately or with a short stagger. Cancels any prior motion for {@code holderKey}.
     */
    public static void enter(JavaPlugin plugin, Object holderKey, Inventory inventory,
                             List<SlotItem> items, MotionProfile profile, boolean reducedMotion) {
        Objects.requireNonNull(plugin, "plugin");
        Objects.requireNonNull(inventory, "inventory");
        Objects.requireNonNull(items, "items");
        cancel(holderKey);

        List<SlotItem> copy = new ArrayList<>(items);
        MotionProfile effective = reducedMotion ? MotionProfile.INSTANT : profile;

        if (effective == MotionProfile.INSTANT || copy.isEmpty()) {
            for (SlotItem si : copy) {
                if (si.slot() >= 0 && si.slot() < inventory.getSize()) {
                    inventory.setItem(si.slot(), si.item());
                }
            }
            return;
        }

        final int[] index = {0};
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            int placed = 0;
            while (index[0] < copy.size() && placed < effective.slotsPerStep) {
                SlotItem si = copy.get(index[0]++);
                if (si.slot() >= 0 && si.slot() < inventory.getSize()) {
                    inventory.setItem(si.slot(), si.item());
                }
                placed++;
            }
            if (index[0] >= copy.size()) {
                cancel(holderKey);
            }
        }, 0L, Math.max(1, effective.ticksPerStep));
        TASKS.put(holderKey, task);
    }
}
