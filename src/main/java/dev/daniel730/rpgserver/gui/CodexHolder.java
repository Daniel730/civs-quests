package dev.daniel730.rpgserver.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public final class CodexHolder implements InventoryHolder {

    private Inventory inventory;
    private int page;

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
