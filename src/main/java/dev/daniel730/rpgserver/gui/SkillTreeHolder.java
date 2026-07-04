package dev.daniel730.rpgserver.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public final class SkillTreeHolder implements InventoryHolder {

    private Inventory inventory;
    private String branch = "";
    private final Map<Integer, String> slotPerks = new HashMap<>();

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch == null ? "" : branch;
    }

    public void mapSlot(int slot, String perkId) {
        slotPerks.put(slot, perkId);
    }

    public void clearMappings() {
        slotPerks.clear();
    }

    public String getPerkId(int slot) {
        return slotPerks.get(slot);
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
