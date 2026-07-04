package dev.daniel730.rpgserver.profile;

import dev.daniel730.rpgserver.RpgServerPlugin;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public final class ProfileManager {

    private final RpgServerPlugin plugin;
    private final File playersFolder;
    private final Map<UUID, PlayerProfile> profiles = new ConcurrentHashMap<>();
    private final Set<UUID> dirtyProfiles = ConcurrentHashMap.newKeySet();

    public ProfileManager(RpgServerPlugin plugin) {
        this.plugin = plugin;
        this.playersFolder = new File(plugin.getDataFolder(), "players");
        if (!playersFolder.exists()) {
            playersFolder.mkdirs();
        }
    }

    public void loadOnlinePlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            loadProfile(player.getUniqueId());
        }
    }

    public PlayerProfile getOrCreate(Player player) {
        return profiles.computeIfAbsent(player.getUniqueId(), this::loadProfile);
    }

    public PlayerProfile getProfile(UUID uuid) {
        return profiles.get(uuid);
    }

    public PlayerProfile loadProfile(UUID uuid) {
        return profiles.computeIfAbsent(uuid, id -> {
            File file = profileFile(id);
            PlayerProfile profile = new PlayerProfile(id);
            if (file.exists()) {
                YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
                profile.setArchetype(yaml.getString("archetype"));
                profile.setTrackedQuestId(yaml.getString("tracked-quest"));
                profile.setActiveQuestIds(new HashSet<>(yaml.getStringList("active-quests")));
                profile.setCompletedQuestIds(new HashSet<>(yaml.getStringList("completed-quests")));
                profile.setCompletedObjectiveKeys(new HashSet<>(yaml.getStringList("completed-objectives")));
                profile.setStartedQuestIds(new HashSet<>(yaml.getStringList("started-quests")));
                if (yaml.isConfigurationSection("objective-progress")) {
                    var progressSection = yaml.getConfigurationSection("objective-progress");
                    java.util.Map<String, Integer> progress = new java.util.HashMap<>();
                    for (String key : progressSection.getKeys(false)) {
                        progress.put(key, progressSection.getInt(key));
                    }
                    profile.setObjectiveProgressMap(progress);
                }
                if (yaml.isConfigurationSection("quest-start-balances")) {
                    var balanceSection = yaml.getConfigurationSection("quest-start-balances");
                    java.util.Map<String, Double> balances = new java.util.HashMap<>();
                    for (String key : balanceSection.getKeys(false)) {
                        balances.put(key, balanceSection.getDouble(key));
                    }
                    profile.setQuestStartBalances(balances);
                }
                profile.setUnlockedPerkIds(new HashSet<>(yaml.getStringList("unlocked-perks")));
                profile.setWelcomeShown(yaml.getBoolean("welcome-shown", false));
                if (yaml.isConfigurationSection("quest-completion-times")) {
                    var timesSection = yaml.getConfigurationSection("quest-completion-times");
                    Map<String, Long> times = new java.util.HashMap<>();
                    for (String key : timesSection.getKeys(false)) {
                        times.put(key, timesSection.getLong(key));
                    }
                    profile.setQuestCompletionTimes(times);
                }
                profile.setDailyCtaShownDay(yaml.getString("daily-cta-shown-day"));
                if (yaml.contains("settings.notifications")) {
                    profile.setNotificationsEnabled(yaml.getBoolean("settings.notifications"));
                }
                if (yaml.contains("settings.bossbar")) {
                    profile.setBossBarEnabled(yaml.getBoolean("settings.bossbar"));
                }
            }
            return profile;
        });
    }

    public void unloadProfile(UUID uuid) {
        saveProfileSync(uuid);
        profiles.remove(uuid);
        dirtyProfiles.remove(uuid);
    }

    public void markDirty(UUID uuid) {
        dirtyProfiles.add(uuid);
    }

    public void saveDirtyProfiles() {
        for (UUID uuid : Set.copyOf(dirtyProfiles)) {
            saveProfileAsync(uuid);
        }
    }

    public void saveAllSync() {
        for (UUID uuid : profiles.keySet()) {
            saveProfileSync(uuid);
        }
        dirtyProfiles.clear();
    }

    public void saveProfileAsync(UUID uuid) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            if (saveProfileSync(uuid)) {
                dirtyProfiles.remove(uuid);
            }
        });
    }

    private boolean saveProfileSync(UUID uuid) {
        PlayerProfile profile = profiles.get(uuid);
        if (profile == null) {
            return false;
        }
        File file = profileFile(uuid);
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("uuid", uuid.toString());
        yaml.set("archetype", profile.getArchetype());
        yaml.set("tracked-quest", profile.getTrackedQuestId());
        yaml.set("active-quests", profile.getActiveQuestIds().stream().toList());
        yaml.set("completed-quests", profile.getCompletedQuestIds().stream().toList());
        yaml.set("completed-objectives", profile.getCompletedObjectiveKeys().stream().toList());
        yaml.set("started-quests", profile.getStartedQuestIds().stream().toList());
        yaml.set("objective-progress", profile.getObjectiveProgressSnapshot());
        yaml.set("quest-start-balances", profile.getQuestStartBalancesSnapshot());
        yaml.set("quest-completion-times", profile.getQuestCompletionTimesSnapshot());
        yaml.set("unlocked-perks", profile.getUnlockedPerkIds().stream().toList());
        yaml.set("welcome-shown", profile.isWelcomeShown());
        yaml.set("daily-cta-shown-day", profile.getDailyCtaShownDay());
        if (profile.getNotificationsEnabled() != null) {
            yaml.set("settings.notifications", profile.getNotificationsEnabled());
        }
        if (profile.getBossBarEnabled() != null) {
            yaml.set("settings.bossbar", profile.getBossBarEnabled());
        }
        try {
            yaml.save(file);
            return true;
        } catch (IOException ex) {
            plugin.getLogger().log(Level.SEVERE, "Falha ao salvar perfil " + uuid, ex);
            return false;
        }
    }

    private File profileFile(UUID uuid) {
        return new File(playersFolder, uuid + ".yml");
    }
}
