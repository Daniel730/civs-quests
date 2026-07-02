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
                profile.setActiveQuestIds(new HashSet<>(yaml.getStringList("active-quests")));
                profile.setCompletedQuestIds(new HashSet<>(yaml.getStringList("completed-quests")));
                profile.setCompletedObjectiveKeys(new HashSet<>(yaml.getStringList("completed-objectives")));
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
        yaml.set("active-quests", profile.getActiveQuestIds().stream().toList());
        yaml.set("completed-quests", profile.getCompletedQuestIds().stream().toList());
        yaml.set("completed-objectives", profile.getCompletedObjectiveKeys().stream().toList());
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
