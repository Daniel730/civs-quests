package dev.daniel730.rpgserver.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public final class PlayerHubHolder implements InventoryHolder {

    public static final int FOOTER_REFRESH_SLOT = 45;
    public static final int FOOTER_CLOSE_SLOT = 49;
    public static final int FOOTER_JOURNAL_SLOT = 53;

    public static final int[] TAB_SLOTS = {0, 2, 4, 6, 8};

    private Inventory inventory;
    private HubTab activeTab = HubTab.INICIO;
    private final Map<Integer, HubClick> slotActions = new HashMap<>();

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    public HubTab getActiveTab() {
        return activeTab;
    }

    public void setActiveTab(HubTab activeTab) {
        this.activeTab = activeTab;
    }

    public void mapAction(int slot, HubClick action) {
        slotActions.put(slot, action);
    }

    public void clearActions() {
        slotActions.clear();
    }

    public HubClick getAction(int slot) {
        return slotActions.get(slot);
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public enum HubTab {
        INICIO("Início", 0),
        CIVS("Civs", 1),
        RPG("RPG", 2),
        CONFIG("Config", 3),
        QUESTS("Quests", 4);

        private final String label;
        private final int index;

        HubTab(String label, int index) {
            this.label = label;
            this.index = index;
        }

        public String getLabel() {
            return label;
        }

        public int getTabSlot() {
            return TAB_SLOTS[index];
        }

        public static HubTab fromTabSlot(int slot) {
            for (HubTab tab : values()) {
                if (tab.getTabSlot() == slot) {
                    return tab;
                }
            }
            return null;
        }
    }

    public enum HubAction {
        TAB,
        COMMAND,
        TOGGLE_NOTIFICATIONS,
        TOGGLE_BOSSBAR,
        OPEN_JOURNAL,
        TRACK_NEXT,
        TRACK_QUEST,
        CLOSE,
        REFRESH
    }

    public record HubClick(HubAction action, String payload) {
        public static HubClick tab(HubTab tab) {
            return new HubClick(HubAction.TAB, tab.name());
        }

        public static HubClick command(String command) {
            return new HubClick(HubAction.COMMAND, command);
        }

        public static HubClick of(HubAction action) {
            return new HubClick(action, null);
        }

        public static HubClick trackQuest(String questId) {
            return new HubClick(HubAction.TRACK_QUEST, questId);
        }
    }
}
