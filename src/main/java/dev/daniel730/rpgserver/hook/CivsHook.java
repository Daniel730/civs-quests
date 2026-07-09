package dev.daniel730.rpgserver.hook;

import dev.daniel730.rpgserver.RpgServerPlugin;
import dev.daniel730.rpgserver.perk.TerritorialPerk;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.redcastlemedia.multitallented.civs.mobs.CustomMobManager;
import org.bukkit.inventory.Inventory;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.localization.LocaleConstants;
import org.redcastlemedia.multitallented.civs.localization.LocaleManager;
import org.redcastlemedia.multitallented.civs.menus.MenuManager;
import org.redcastlemedia.multitallented.civs.util.Constants;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.skills.Skill;
import org.redcastlemedia.multitallented.civs.stats.StatManager;
import org.redcastlemedia.multitallented.civs.stats.StatModifier;
import org.redcastlemedia.multitallented.civs.stats.StatOperation;
import org.redcastlemedia.multitallented.civs.stats.TerritorialStat;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class CivsHook {

    private static final String TERRITORIAL_MODIFIER_PREFIX = "rpg_";

    private final RpgServerPlugin plugin;
    private boolean enabled;

    public CivsHook(RpgServerPlugin plugin) {
        this.plugin = plugin;
    }

    public void enable() {
        if (!plugin.getPluginConfig().isCivsEnabled()) {
            enabled = false;
            return;
        }
        Plugin civs = Bukkit.getPluginManager().getPlugin("Civs");
        if (civs == null) {
            enabled = false;
            plugin.getLogger().warning("Civs não encontrado — objetivos Civs e perks territoriais ficarão inativos.");
            return;
        }
        if (!civs.isEnabled()) {
            enabled = false;
            plugin.getLogger().warning(
                    "Civs presente mas falhou ao iniciar — objetivos Civs e perks territoriais ficarão inativos.");
            return;
        }
        enabled = probeCivsRuntime();
        if (enabled) {
            plugin.getLogger().info("Civs detectado — região, skills internas, leilão e StatManager habilitados.");
        } else {
            plugin.getLogger().warning("Civs presente mas API indisponível — integração pausada até o plugin terminar de carregar.");
        }
    }

    public void refresh() {
        enable();
    }

    /** Called when Civs is disabled/reloaded; must not touch Civs classes. */
    public void onCivsDisabled() {
        if (enabled) {
            enabled = false;
            plugin.getLogger().info("Civs desabilitado — integração RPG pausada até novo carregamento.");
        }
    }

    public boolean isEnabled() {
        return enabled && isCivsPluginActive();
    }

    private boolean isCivsPluginActive() {
        Plugin civs = Bukkit.getPluginManager().getPlugin("Civs");
        return civs != null && civs.isEnabled();
    }

    private boolean probeCivsRuntime() {
        if (!isCivsPluginActive()) {
            return false;
        }
        try {
            CivilianManager.getInstance();
            return true;
        } catch (Throwable error) {
            markCivsUnavailable(error);
            return false;
        }
    }

    private boolean ensureCivsReady() {
        if (!enabled) {
            return false;
        }
        if (!isCivsPluginActive()) {
            enabled = false;
            return false;
        }
        return true;
    }

    private void markCivsUnavailable(Throwable error) {
        if (enabled) {
            enabled = false;
            plugin.getLogger().warning("Integração Civs pausada (Civs indisponível ou recarregando): "
                    + error.getClass().getSimpleName() + " — " + error.getMessage());
        }
    }

    private <T> T withCivs(CivsSupplier<T> action, T fallback) {
        if (!ensureCivsReady()) {
            return fallback;
        }
        try {
            return action.get();
        } catch (Throwable error) {
            markCivsUnavailable(error);
            return fallback;
        }
    }

    public boolean isCivsMenuOpen(UUID uuid) {
        return withCivs(() -> MenuManager.getInstance().hasMenuOpen(uuid), false);
    }

    public int getCivsMenuHistorySize(UUID uuid) {
        return withCivs(() -> MenuManager.getHistorySize(uuid), Integer.MAX_VALUE);
    }

    public boolean isCivsBackButtonClick(Player player, ItemStack item) {
        if (player == null || item == null) {
            return false;
        }
        return withCivs(() -> {
            Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
            if (civilian == null) {
                return false;
            }
            return MenuManager.getInstance().getBackButton()
                    .createCVItem(civilian.getLocale(), 0)
                    .equivalentItem(item, true, true);
        }, false);
    }

    @FunctionalInterface
    private interface CivsSupplier<T> {
        T get() throws Exception;
    }

    /**
     * Opens a Civs {@link MenuManager} screen (same as {@code /cv menu <name>}).
     * Closes any RPG inventory the player has open first.
     */
    public boolean openMenu(Player player, String menuName) {
        return openMenu(player, menuName, Map.of());
    }

    public boolean openMenu(Player player, String menuName, Map<String, String> params) {
        if (!ensureCivsReady() || player == null || menuName == null || menuName.isBlank()) {
            return false;
        }
        if (!hasMenuPermission(player)) {
            return false;
        }
        return withCivs(() -> {
            player.closeInventory();
            MenuManager.clearHistory(player.getUniqueId());
            Inventory inventory = MenuManager.getInstance().openMenu(player, menuName, new HashMap<>(params));
            return inventory != null;
        }, false);
    }

    /** Opens the Civs port / locations list ({@code menu:port} from the starter book). */
    public boolean openLocationsMenu(Player player) {
        return openMenu(player, "port");
    }

    /** Opens the Civs main menu ({@code /cv menu}). */
    public boolean openMainMenu(Player player) {
        return openMenu(player, "main");
    }

    /**
     * Opens a Civs menu from a menu string (e.g. {@code select-town?prevMenu=town&uuid=...}).
     */
    public boolean openMenuFromString(Player player, String menuString) {
        if (!ensureCivsReady() || player == null || menuString == null || menuString.isBlank()) {
            return false;
        }
        if (!hasMenuPermission(player)) {
            return false;
        }
        return withCivs(() -> {
            Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
            if (civilian == null) {
                return false;
            }
            player.closeInventory();
            MenuManager.clearHistory(player.getUniqueId());
            Inventory inventory = MenuManager.openMenuFromString(civilian, menuString);
            return inventory != null;
        }, false);
    }

    private boolean hasMenuPermission(Player player) {
        return withCivs(() -> {
            if (Civs.perm != null && !Civs.perm.has(player, Constants.MENU_PERMISSION)) {
                player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(player,
                        LocaleConstants.PERMISSION_DENIED));
                return false;
            }
            return true;
        }, false);
    }

    public boolean hasBuiltRegion(Player player, String regionKey) {
        if (!ensureCivsReady() || player == null || regionKey == null || regionKey.isBlank()) {
            return false;
        }
        return withCivs(() -> hasBuiltRegionUnsafe(player, regionKey), false);
    }

    private boolean hasBuiltRegionUnsafe(Player player, String regionKey) {
        String normalized = regionKey.toLowerCase(Locale.ROOT);
        Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
        if (civilian == null) {
            return false;
        }
        Skill building = civilian.getSkills().get("building");
        if (building != null && building.getAccomplishments().getOrDefault(normalized, 0) > 0) {
            return true;
        }
        Map<String, Integer> stash = civilian.getStashItems();
        if (stash != null && stash.getOrDefault(normalized, 0) > 0) {
            return true;
        }
        for (Region region : RegionManager.getInstance().getAllRegions()) {
            if (region.getType().equalsIgnoreCase(normalized)
                    && region.getOwners().contains(player.getUniqueId())) {
                return true;
            }
        }
        return false;
    }

    public int getMobKillCount(Player player, String mobKey) {
        if (!ensureCivsReady() || player == null || mobKey == null || mobKey.isBlank()) {
            return 0;
        }
        return withCivs(() -> getMobKillCountUnsafe(player, mobKey), 0);
    }

    private int getMobKillCountUnsafe(Player player, String mobKey) {
        Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
        if (civilian == null) {
            return 0;
        }
        String normalizedMob = mobKey.toLowerCase(Locale.ROOT);
        int total = 0;
        for (String combatSkill : List.of("sword", "axe")) {
            Skill skill = civilian.getSkills().get(combatSkill);
            if (skill == null) {
                continue;
            }
            for (Map.Entry<String, Integer> entry : skill.getAccomplishments().entrySet()) {
                String key = entry.getKey().toLowerCase(Locale.ROOT);
                if (key.equals(normalizedMob)) {
                    total += entry.getValue();
                }
            }
        }
        return total;
    }

    /**
     * Returns true when the player belongs to a town (owner, member, or guest role in raw roster).
     * When {@code townName} is set, only that town matches.
     */
    public boolean isTownMember(Player player, String townName) {
        if (!ensureCivsReady() || player == null) {
            return false;
        }
        return withCivs(() -> isTownMemberUnsafe(player, townName), false);
    }

    private boolean isTownMemberUnsafe(Player player, String townName) {
        Set<Town> towns = TownManager.getInstance().getTownsForPlayer(player.getUniqueId());
        if (towns.isEmpty()) {
            return false;
        }
        if (townName == null || townName.isBlank()) {
            return true;
        }
        String filter = townName.toLowerCase(Locale.ROOT);
        for (Town town : towns) {
            if (town.getName().equalsIgnoreCase(filter)) {
                return true;
            }
        }
        return false;
    }

    /** Towns where the player is listed as owner in Civs raw roster. */
    public boolean isTownOwner(Player player, String townName) {
        if (!ensureCivsReady() || player == null) {
            return false;
        }
        return withCivs(() -> isTownOwnerUnsafe(player, townName), false);
    }

    private boolean isTownOwnerUnsafe(Player player, String townName) {
        for (Town town : TownManager.getInstance().getTownsForPlayer(player.getUniqueId())) {
            if (townName != null && !townName.isBlank()
                    && !town.getName().equalsIgnoreCase(townName)) {
                continue;
            }
            String role = town.getRawPeople().get(player.getUniqueId());
            if (role != null && role.toLowerCase(Locale.ROOT).contains(Constants.OWNER)) {
                return true;
            }
        }
        return false;
    }

    /** Number of other players (non-owner) in any town the player belongs to. */
    public int countTownCoMembers(Player player) {
        if (!ensureCivsReady() || player == null) {
            return 0;
        }
        return withCivs(() -> countTownCoMembersUnsafe(player), 0);
    }

    private int countTownCoMembersUnsafe(Player player) {
        int count = 0;
        UUID self = player.getUniqueId();
        for (Town town : TownManager.getInstance().getTownsForPlayer(self)) {
            for (UUID member : town.getRawPeople().keySet()) {
                if (!member.equals(self)) {
                    count++;
                }
            }
        }
        return count;
    }

    public double getSkillTotalExp(Player player, String skillKey) {
        if (!ensureCivsReady() || player == null) {
            return 0;
        }
        return withCivs(() -> getSkillTotalExpUnsafe(player, skillKey), 0D);
    }

    private double getSkillTotalExpUnsafe(Player player, String skillKey) {
        Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
        if (civilian == null) {
            return 0;
        }
        if (skillKey == null || skillKey.isBlank()) {
            double total = 0;
            for (Skill skill : civilian.getSkills().values()) {
                total += skill.getTotalExp();
            }
            return total;
        }
        Skill skill = findSkill(civilian, skillKey);
        return skill == null ? 0 : skill.getTotalExp();
    }

    public int getSkillLevel(Player player, String skillKey) {
        if (!ensureCivsReady() || player == null || skillKey == null || skillKey.isBlank()) {
            return 0;
        }
        return withCivs(() -> getSkillLevelUnsafe(player, skillKey), 0);
    }

    private int getSkillLevelUnsafe(Player player, String skillKey) {
        Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
        if (civilian == null) {
            return 0;
        }
        Skill skill = findSkill(civilian, skillKey);
        return skill == null ? 0 : skill.getLevel();
    }

    public boolean addTerritorialModifier(Player player, TerritorialPerk perk) {
        if (!ensureCivsReady() || player == null || perk == null) {
            return false;
        }
        return withCivs(() -> addTerritorialModifierUnsafe(player, perk), false);
    }

    public boolean addPathTraitModifier(Player player, String modifierId, String statKey,
                                        double value, String operation) {
        if (!ensureCivsReady() || player == null || modifierId == null || modifierId.isBlank()
                || statKey == null || statKey.isBlank()) {
            return false;
        }
        return withCivs(() -> {
            TerritorialStat stat = TerritorialStat.fromKey(statKey);
            if (stat == null) {
                plugin.getLogger().warning("Stat territorial Civs desconhecido: " + statKey);
                return false;
            }
            StatOperation statOperation = "multiply".equalsIgnoreCase(operation)
                    ? StatOperation.MULTIPLY
                    : StatOperation.ADD;
            StatModifier modifier = new StatModifier(modifierId, stat, value, statOperation);
            StatManager.getInstance().removeModifier(player.getUniqueId(), modifierId);
            StatManager.getInstance().addModifier(player.getUniqueId(), modifier);
            return true;
        }, false);
    }

    private boolean addTerritorialModifierUnsafe(Player player, TerritorialPerk perk) {
        TerritorialStat stat = TerritorialStat.fromKey(perk.getStatKey());
        if (stat == null) {
            plugin.getLogger().warning("Stat territorial Civs desconhecido: " + perk.getStatKey());
            return false;
        }
        StatOperation operation = perk.getOperation() == TerritorialPerk.Operation.MULTIPLY
                ? StatOperation.MULTIPLY
                : StatOperation.ADD;
        StatModifier modifier = new StatModifier(
                territorialModifierId(perk.getId()),
                stat,
                perk.getValue(),
                operation);
        StatManager.getInstance().removeModifier(player.getUniqueId(), territorialModifierId(perk.getId()));
        StatManager.getInstance().addModifier(player.getUniqueId(), modifier);
        return true;
    }

    public boolean removeTerritorialModifier(Player player, String perkId) {
        if (!ensureCivsReady() || player == null || perkId == null || perkId.isBlank()) {
            return false;
        }
        return withCivs(() -> StatManager.getInstance().removeModifier(player.getUniqueId(), territorialModifierId(perkId)),
                false);
    }

    public boolean hasTerritorialModifier(Player player, String perkId) {
        if (!ensureCivsReady() || player == null || perkId == null || perkId.isBlank()) {
            return false;
        }
        return withCivs(() -> StatManager.getInstance().hasModifier(player.getUniqueId(), territorialModifierId(perkId)),
                false);
    }

    public boolean addSkillXp(Player player, String skillKey, double amount) {
        if (!ensureCivsReady() || player == null || skillKey == null || skillKey.isBlank() || amount <= 0) {
            return false;
        }
        return withCivs(() -> {
            Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
            if (civilian == null) {
                return false;
            }
            return civilian.addSkillXp(player, skillKey, amount) > 0;
        }, false);
    }

    public boolean spawnQuestMob(Player player, String mobId, double partyRadius) {
        if (!ensureCivsReady() || player == null || mobId == null || mobId.isBlank()) {
            return false;
        }
        return withCivs(() -> spawnQuestMobUnsafe(player, mobId, partyRadius), false);
    }

    private boolean spawnQuestMobUnsafe(Player player, String mobId, double partyRadius) {
            CustomMobManager manager = CustomMobManager.getInstance();
            if (!manager.isEnabled() || manager.getMob(mobId) == null) {
                plugin.getLogger().warning("Mob customizado desconhecido ou desabilitado: " + mobId);
                return false;
            }
            return manager.spawnForQuest(mobId, player.getLocation(), player.getUniqueId(), partyRadius) != null;
    }

    public static String territorialModifierId(String perkId) {
        if (perkId == null || perkId.isBlank()) {
            throw new IllegalArgumentException("perk id is required");
        }
        return perkId.startsWith(TERRITORIAL_MODIFIER_PREFIX) ? perkId : TERRITORIAL_MODIFIER_PREFIX + perkId;
    }

    private Skill findSkill(Civilian civilian, String name) {
        Skill skill = civilian.getSkills().get(name.toLowerCase(Locale.ROOT));
        if (skill != null) {
            return skill;
        }
        for (Skill current : civilian.getSkills().values()) {
            if (current.getType().equalsIgnoreCase(name)) {
                return current;
            }
        }
        return null;
    }
}
