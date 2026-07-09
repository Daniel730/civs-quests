package dev.daniel730.rpgserver.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

public final class PlayerHubHolder implements InventoryHolder {

    public static final int FOOTER_REFRESH_SLOT = 45;
    public static final int FOOTER_BACK_SLOT = 47;
    public static final int FOOTER_JOURNAL_SLOT = 48;
    public static final int FOOTER_TRACK_SLOT = 49;
    public static final int FOOTER_SYNC_SLOT = 51;
    public static final int FOOTER_CLOSE_SLOT = 53;

    public static final int[] TAB_SLOTS = {0, 2, 4, 6, 8};

    private Inventory inventory;
    private HubTab activeTab = HubTab.INICIO;
    private HubScreen screen = HubScreen.TAB;
    private String selectedPathQuestId;
    private final Deque<HubScreen> screenStack = new ArrayDeque<>();
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

    public HubScreen getScreen() {
        return screen;
    }

    public void resetNavigation(HubTab tab) {
        screenStack.clear();
        screen = HubScreen.TAB;
        activeTab = tab;
        selectedPathQuestId = null;
    }

    public String getSelectedPathQuestId() {
        return selectedPathQuestId;
    }

    public void setSelectedPathQuestId(String selectedPathQuestId) {
        this.selectedPathQuestId = selectedPathQuestId;
    }

    public void pushScreen(HubScreen next) {
        screenStack.push(screen);
        screen = next;
    }

    public boolean popScreen() {
        if (screenStack.isEmpty()) {
            return false;
        }
        screen = screenStack.pop();
        return true;
    }

    public boolean canGoBack() {
        return !screenStack.isEmpty();
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

    public enum HubScreen {
        TAB,
        PATH_PICKER,
        PATH_DETAIL,
        QUEST_TREE,
        SKILL_TREE,
        CODEX
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
        OPEN_SKILL_TREE,
        OPEN_CODEX,
        OPEN_SUBVIEW,
        OPEN_CIVS_MENU,
        TRACK_NEXT,
        TRACK_QUEST,
        SYNC,
        BACK,
        CLOSE,
        REFRESH,
        ACCEPT_PATH,
        OPEN_REBIRTH
    }

    public record HubClick(HubAction action, String payload) {
        public static HubClick tab(HubTab tab) {
            return new HubClick(HubAction.TAB, tab.name());
        }

        public static HubClick command(String command) {
            return new HubClick(HubAction.COMMAND, command);
        }

        public static HubClick subview(HubScreen screen) {
            return new HubClick(HubAction.OPEN_SUBVIEW, screen.name());
        }

        public static HubClick openSkillTree() {
            return new HubClick(HubAction.OPEN_SKILL_TREE, null);
        }

        public static HubClick openCodex() {
            return new HubClick(HubAction.OPEN_CODEX, null);
        }

        public static HubClick civsMenu(String menuName) {
            return new HubClick(HubAction.OPEN_CIVS_MENU, menuName);
        }

        public static HubClick of(HubAction action) {
            return new HubClick(action, null);
        }

        public static HubClick trackQuest(String questId) {
            return new HubClick(HubAction.TRACK_QUEST, questId);
        }

        public static HubClick acceptPath(String questId) {
            return new HubClick(HubAction.ACCEPT_PATH, questId);
        }

        public static HubClick openPathDetail(String questId) {
            return new HubClick(HubAction.OPEN_SUBVIEW, "PATH_DETAIL:" + questId);
        }
    }
}
