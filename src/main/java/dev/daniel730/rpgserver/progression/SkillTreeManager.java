package dev.daniel730.rpgserver.progression;

import dev.daniel730.rpgserver.RpgServerPlugin;
import dev.daniel730.rpgserver.perk.PerkDefinition;
import dev.daniel730.rpgserver.perk.TerritorialPerk;
import dev.daniel730.rpgserver.profile.PlayerProfile;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public final class SkillTreeManager {

    public enum PerkStatus {
        UNLOCKED("Desbloqueado"),
        AVAILABLE("Disponível"),
        LOCKED("Bloqueado");

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
            plugin.saveResource("perks/warrior_berserk.yml", false);
            plugin.saveResource("perks/builder_discount.yml", false);
            plugin.saveResource("perks/builder_fortress.yml", false);
            plugin.saveResource("perks/warrior_veteran.yml", false);
            plugin.saveResource("perks/merchant_bazaar.yml", false);
            plugin.saveResource("perks/merchant_golden_touch.yml", false);
            plugin.saveResource("perks/builder_master.yml", false);
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

    private PerkDefinition parsePerk(YamlConfiguration config) {
        String id = config.getString("id");
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Campo 'id' obrigatório");
        }
        String name = config.getString("name", id);
        String archetype = config.getString("archetype", "");
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
        return new PerkDefinition(id, name, archetype, requires, type, statKey, value, operation);
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
        if (!matchesArchetype(profile, perk) || !meetsRequirements(profile, perk)) {
            return PerkStatus.LOCKED;
        }
        return PerkStatus.AVAILABLE;
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
        if (!matchesArchetype(profile, perk) || !meetsRequirements(profile, perk)) {
            return false;
        }
        profile.unlockPerk(perkId);
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
            if (matchesArchetype(profile, perk) && meetsRequirements(profile, perk)) {
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
