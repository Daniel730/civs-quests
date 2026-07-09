package dev.daniel730.rpgserver.progression;

import dev.daniel730.rpgserver.RpgServerPlugin;
import dev.daniel730.rpgserver.perk.PerkDefinition;
import dev.daniel730.rpgserver.perk.TerritorialPerk;
import dev.daniel730.rpgserver.profile.PlayerProfile;
import dev.daniel730.rpgserver.quest.Quest;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;

public final class SkillTreeManager {

    public enum PerkStatus {
        UNLOCKED("Desbloqueado"),
        AVAILABLE("Disponível"),
        LOCKED("Bloqueado"),
        CHOICE_LOCKED("Escolha bloqueada");

        private final String display;

        PerkStatus(String display) {
            this.display = display;
        }

        public String getDisplay() {
            return display;
        }
    }

    private final RpgServerPlugin plugin;
    private final Map<String, PerkDefinition> perks = new LinkedHashMap<>();

    public SkillTreeManager(RpgServerPlugin plugin) {
        this.plugin = plugin;
    }

    public void loadPerks() {
        perks.clear();
        File perksFolder = new File(plugin.getDataFolder(), "perks");
        if (!perksFolder.exists()) {
            perksFolder.mkdirs();
            saveDefaultPerks();
        }

        File[] files = perksFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) {
            return;
        }

        for (File file : files) {
            try {
                PerkDefinition perk = parsePerk(YamlConfiguration.loadConfiguration(file));
                perks.put(perk.getId(), perk);
            } catch (Exception ex) {
                plugin.getLogger().log(Level.WARNING, "Falha ao carregar perk " + file.getName(), ex);
            }
        }
        plugin.getLogger().info("Carregados " + perks.size() + " perks.");
    }

    private void saveDefaultPerks() {
        String[] defaults = {
                "warrior_berserk.yml", "builder_discount.yml", "builder_fortress.yml",
                "warrior_veteran.yml", "merchant_bazaar.yml", "merchant_golden_touch.yml",
                "builder_master.yml", "warrior_duelist.yml", "merchant_trader.yml",
                "warrior_siege_heart.yml", "warrior_frost_walker.yml", "warrior_duel_master.yml",
                "warrior_battle_cry.yml", "builder_stoneheart.yml", "builder_skybridge.yml",
                "builder_quarry_sense.yml", "builder_architect.yml", "merchant_guild_seal.yml",
                "merchant_silk_route.yml", "merchant_ledger_master.yml", "merchant_smuggler.yml",
                "warrior_bulwark.yml", "warrior_tracker.yml", "merchant_broker.yml",
                "merchant_haggler.yml", "merchant_auctioneer.yml", "builder_planner.yml",
                "builder_deep_delver.yml", "warrior_berserker_lord.yml", "warrior_siege_breaker.yml",
                "warrior_beast_slayer.yml", "merchant_monopoly.yml", "merchant_guildmaster.yml",
                "merchant_tycoon.yml", "builder_citadel.yml", "builder_mine_baron.yml",
                "builder_metropolis.yml"
        };
        for (String resource : defaults) {
            try {
                plugin.saveResource("perks/" + resource, false);
            } catch (IllegalArgumentException ignored) {
                // optional bundled perk
            }
        }
    }

    private PerkDefinition parsePerk(YamlConfiguration config) {
        String id = config.getString("id");
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Campo 'id' obrigatório");
        }
        String name = config.getString("name", id);
        String archetype = config.getString("archetype", "");
        String branch = config.getString("branch", "");
        int tier = config.getInt("tier", 1);
        String exclusiveGroup = config.getString("exclusive-group", "");
        int essenceCost = config.getInt("essence-cost", 0);
        List<String> requires = config.getStringList("requires");
        PerkDefinition.PerkType type = PerkDefinition.PerkType.fromYaml(config.getString("type"));
        if (type == null) {
            throw new IllegalArgumentException("Perk '" + id + "' requer type: auraskills-stat ou civs-territorial");
        }
        String statKey = config.getString("stat");
        if (statKey == null || statKey.isBlank()) {
            throw new IllegalArgumentException("Perk '" + id + "' requer campo 'stat'");
        }
        double value = config.getDouble("value", 0);
        String operation = config.getString("operation", "add");
        return new PerkDefinition(id, name, archetype, branch, tier, exclusiveGroup, essenceCost,
                requires, type, statKey, value, operation);
    }

    public PerkDefinition getPerk(String id) {
        return perks.get(id);
    }

    public Collection<PerkDefinition> getAllPerks() {
        return Collections.unmodifiableCollection(perks.values());
    }

    public List<PerkDefinition> getPerksForArchetype(String archetype) {
        if (archetype == null || archetype.isBlank()) {
            return List.copyOf(perks.values());
        }
        List<PerkDefinition> filtered = new ArrayList<>();
        for (PerkDefinition perk : perks.values()) {
            if (perk.getArchetype().isBlank() || perk.getArchetype().equalsIgnoreCase(archetype)) {
                filtered.add(perk);
            }
        }
        return filtered;
    }

    public List<PerkDefinition> getPerksForBranch(String archetype, String branch) {
        List<PerkDefinition> filtered = new ArrayList<>();
        for (PerkDefinition perk : getPerksForArchetype(archetype)) {
            if (branch == null || branch.isBlank() || branch.equalsIgnoreCase(perk.getBranch())) {
                filtered.add(perk);
            }
        }
        filtered.sort((a, b) -> Integer.compare(a.getTier(), b.getTier()));
        return filtered;
    }

    public boolean meetsRequirements(PlayerProfile profile, PerkDefinition perk) {
        for (String requiredId : perk.getRequiredIds()) {
            if (profile.isPerkUnlocked(requiredId)) {
                continue;
            }
            if (!profile.isQuestComplete(requiredId)) {
                return false;
            }
        }
        return true;
    }

    public boolean isCapstoneChoiceAvailable(PlayerProfile profile, PerkDefinition perk) {
        for (Quest quest : plugin.getQuestManager().getAllQuests()) {
            if (!profile.isQuestComplete(quest.getId())) {
                continue;
            }
            if (quest.getUnlocksPerkChoice().contains(perk.getId())) {
                return true;
            }
        }
        return false;
    }

    public boolean isBlockedByExclusiveGroup(PlayerProfile profile, PerkDefinition perk) {
        if (perk.getExclusiveGroup().isBlank()) {
            return false;
        }
        if (profile.isExclusiveGroupLocked(perk.getExclusiveGroup())) {
            for (String unlockedId : profile.getUnlockedPerkIds()) {
                PerkDefinition unlocked = perks.get(unlockedId);
                if (unlocked != null
                        && perk.getExclusiveGroup().equalsIgnoreCase(unlocked.getExclusiveGroup())) {
                    return !unlocked.getId().equals(perk.getId());
                }
            }
            return true;
        }
        return false;
    }

    public boolean matchesArchetype(PlayerProfile profile, PerkDefinition perk) {
        if (perk.getArchetype().isBlank()) {
            return true;
        }
        String playerArchetype = profile.getArchetype();
        return playerArchetype != null && playerArchetype.equalsIgnoreCase(perk.getArchetype());
    }

    public PerkStatus getPerkStatus(PlayerProfile profile, PerkDefinition perk) {
        if (profile.isPerkUnlocked(perk.getId())) {
            return PerkStatus.UNLOCKED;
        }
        if (!matchesArchetype(profile, perk)) {
            return PerkStatus.LOCKED;
        }
        if (isBlockedByExclusiveGroup(profile, perk)) {
            return PerkStatus.CHOICE_LOCKED;
        }
        if (!meetsRequirements(profile, perk)) {
            return PerkStatus.LOCKED;
        }
        if (perk.getEssenceCost() > 0 && profile.getPathEssence() < perk.getEssenceCost()) {
            return PerkStatus.LOCKED;
        }
        if (isCapstoneChoicePerk(perk) && !isCapstoneChoiceAvailable(profile, perk)) {
            return PerkStatus.LOCKED;
        }
        return PerkStatus.AVAILABLE;
    }

    private boolean isCapstoneChoicePerk(PerkDefinition perk) {
        for (Quest quest : plugin.getQuestManager().getAllQuests()) {
            if (quest.getUnlocksPerkChoice().contains(perk.getId())) {
                return true;
            }
        }
        return false;
    }

    public boolean tryUnlock(Player player, String perkId) {
        return tryUnlock(player, perkId, true);
    }

    public boolean tryUnlock(Player player, String perkId, boolean notify) {
        PerkDefinition perk = perks.get(perkId);
        if (perk == null) {
            plugin.getLogger().warning("Perk desconhecido: " + perkId);
            return false;
        }
        PlayerProfile profile = plugin.getProfileManager().getOrCreate(player);
        if (profile.isPerkUnlocked(perkId)) {
            return false;
        }
        if (getPerkStatus(profile, perk) != PerkStatus.AVAILABLE) {
            return false;
        }
        if (perk.getEssenceCost() > 0 && !profile.spendPathEssence(perk.getEssenceCost())) {
            if (notify) {
                plugin.getMessageUtil().send(player,
                        "<red>Essência insuficiente.</red> <gray>Necessário:</gray> <white>"
                                + perk.getEssenceCost() + "</white>");
            }
            return false;
        }
        profile.unlockPerk(perkId);
        if (!perk.getExclusiveGroup().isBlank()) {
            profile.lockExclusiveGroup(perk.getExclusiveGroup());
        }
        if (isCapstoneChoicePerk(perk)) {
            profile.setLegacyPerkId(perkId);
        }
        applyPerk(player, perk);
        plugin.getProfileManager().markDirty(player.getUniqueId());
        if (notify) {
            plugin.getMessageUtil().send(player, "<green>Perk desbloqueado:</green> " + perk.getName());
        }
        return true;
    }

    public void applyUnlockedPerks(Player player) {
        PlayerProfile profile = plugin.getProfileManager().getOrCreate(player);
        for (String perkId : profile.getUnlockedPerkIds()) {
            PerkDefinition perk = perks.get(perkId);
            if (perk != null) {
                applyPerk(player, perk);
            }
        }
    }

    public void applyPerk(Player player, PerkDefinition perk) {
        switch (perk.getType()) {
            case AURASKILLS_STAT -> plugin.getAuraSkillsHook().addStatModifier(
                    player, perk.getId(), perk.getStatKey(), perk.getValue(), perk.getOperation());
            case CIVS_TERRITORIAL -> {
                TerritorialPerk.Operation operation = "multiply".equalsIgnoreCase(perk.getOperation())
                        ? TerritorialPerk.Operation.MULTIPLY
                        : TerritorialPerk.Operation.ADD;
                plugin.getCivsHook().addTerritorialModifier(player,
                        new TerritorialPerk(perk.getId(), perk.getStatKey(), perk.getValue(), operation));
            }
        }
    }

    public void revokePerk(Player player, String perkId) {
        PerkDefinition perk = perks.get(perkId);
        if (perk == null) {
            return;
        }
        switch (perk.getType()) {
            case AURASKILLS_STAT -> plugin.getAuraSkillsHook().removeStatModifier(player, perkId);
            case CIVS_TERRITORIAL -> plugin.getCivsHook().removeTerritorialModifier(player, perkId);
        }
    }

    public void checkAutoUnlocks(Player player) {
        PlayerProfile profile = plugin.getProfileManager().getOrCreate(player);
        boolean changed = false;
        for (PerkDefinition perk : perks.values()) {
            if (profile.isPerkUnlocked(perk.getId())) {
                continue;
            }
            if (perk.getEssenceCost() > 0 || isCapstoneChoicePerk(perk)) {
                continue;
            }
            if (getPerkStatus(profile, perk) == PerkStatus.AVAILABLE) {
                profile.unlockPerk(perk.getId());
                applyPerk(player, perk);
                plugin.getMessageUtil().send(player, "<green>Perk desbloqueado:</green> " + perk.getName());
                changed = true;
            }
        }
        if (changed) {
            plugin.getProfileManager().markDirty(player.getUniqueId());
        }
    }
}
