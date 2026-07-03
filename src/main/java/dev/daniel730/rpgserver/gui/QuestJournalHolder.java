package dev.daniel730.rpgserver.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public final class QuestJournalHolder implements InventoryHolder {

    public static final int NAV_PREV_SLOT = 45;
    public static final int NAV_NEXT_SLOT = 53;

    private Inventory inventory;
    private final Map<Integer, String> slotToQuestId = new HashMap<>();
    private int page;
    private int totalPages = 1;

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    public void mapSlot(int slot, String questId) {
        slotToQuestId.put(slot, questId);
    }

    public void clearSlotMappings() {
        slotToQuestId.clear();
    }

    public String getQuestId(int slot) {
        return slotToQuestId.get(slot);
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
