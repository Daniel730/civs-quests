package dev.daniel730.rpgserver.quest;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Validates the shipped quest content in {@code src/main/resources}: every quest parses and every
 * cross-reference (prerequisite quest, discover_poi POI, reward loot-table, unlocked perk) points at
 * something that actually exists. Guards against dangling references — the same class of latent bug
 * that hid 19 quests behind the {@code loadQuests()} hardcoded list.
 */
public class QuestContentIntegrityTest {

    private static final File RESOURCES = new File("src/main/resources");

    private static Set<String> poiIds;
    private static Set<String> perkIds;
    private static Set<String> lootTableIds;
    private static Set<String> questIds;
    private static List<File> questFiles;

    @BeforeClass
    public static void loadContent() {
        assertTrue("resources dir must exist (run from module root): " + RESOURCES.getAbsolutePath(),
                RESOURCES.isDirectory());

        poiIds = new HashSet<>();
        YamlConfiguration pois = YamlConfiguration.loadConfiguration(new File(RESOURCES, "discoveries/pois.yml"));
        ConfigurationSection poiSection = pois.getConfigurationSection("pois");
        if (poiSection != null) {
            poiIds.addAll(poiSection.getKeys(false));
        }

        perkIds = idsFromDir("perks");
        lootTableIds = idsFromDir("loot-tables");

        questFiles = new ArrayList<>();
        File[] files = new File(RESOURCES, "quests").listFiles((dir, name) -> name.endsWith(".yml"));
        if (files != null) {
            questFiles.addAll(List.of(files));
        }
        questIds = new HashSet<>();
        for (File file : questFiles) {
            String id = YamlConfiguration.loadConfiguration(file).getString("id");
            if (id != null && !id.isBlank()) {
                questIds.add(id);
            }
        }
    }

    private static Set<String> idsFromDir(String dir) {
        Set<String> ids = new HashSet<>();
        File[] files = new File(RESOURCES, dir).listFiles((d, name) -> name.endsWith(".yml"));
        if (files != null) {
            for (File file : files) {
                String id = YamlConfiguration.loadConfiguration(file).getString("id");
                if (id != null && !id.isBlank()) {
                    ids.add(id);
                }
            }
        }
        return ids;
    }

    @Test
    public void shippedQuestsExist() {
        assertFalse("expected quest YAMLs under src/main/resources/quests", questFiles.isEmpty());
    }

    @Test
    public void everyQuestParsesAndHasIdAndObjectives() {
        Set<String> seenIds = new HashSet<>();
        List<String> problems = new ArrayList<>();
        for (File file : questFiles) {
            YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
            String id = cfg.getString("id");
            if (id == null || id.isBlank()) {
                problems.add(file.getName() + ": missing id");
                continue;
            }
            if (!seenIds.add(id)) {
                problems.add(file.getName() + ": duplicate quest id '" + id + "'");
            }
            if (cfg.getMapList("objectives").isEmpty()) {
                problems.add(file.getName() + ": no objectives");
            }
        }
        assertNoProblems(problems);
    }

    @Test
    public void everyReferenceResolves() {
        List<String> problems = new ArrayList<>();
        for (File file : questFiles) {
            YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
            String qid = cfg.getString("id", file.getName());

            for (String req : cfg.getStringList("requires")) {
                if (!questIds.contains(req)) {
                    problems.add(qid + ": requires unknown quest '" + req + "'");
                }
            }
            for (Map<?, ?> raw : cfg.getMapList("objectives")) {
                Object type = raw.get("type");
                Object poi = raw.get("poi");
                if ("discover_poi".equals(type) && poi != null && !poiIds.contains(poi.toString())) {
                    problems.add(qid + ": discover_poi references unknown POI '" + poi + "'");
                }
            }
            ConfigurationSection rewards = cfg.getConfigurationSection("rewards");
            if (rewards != null) {
                String lootTable = rewards.getString("loot-table");
                if (lootTable != null && !lootTable.isBlank() && !lootTableIds.contains(lootTable)) {
                    problems.add(qid + ": reward references unknown loot-table '" + lootTable + "'");
                }
            }
            String unlocksPerk = cfg.getString("unlocks-perk");
            if (unlocksPerk != null && !unlocksPerk.isBlank() && !perkIds.contains(unlocksPerk)) {
                problems.add(qid + ": unlocks-perk references unknown perk '" + unlocksPerk + "'");
            }
            for (String perk : cfg.getStringList("unlocks-perk-choice")) {
                if (!perkIds.contains(perk)) {
                    problems.add(qid + ": unlocks-perk-choice references unknown perk '" + perk + "'");
                }
            }
        }
        assertNoProblems(problems);
    }

    private static void assertNoProblems(List<String> problems) {
        if (!problems.isEmpty()) {
            fail("Quest content integrity problems (" + problems.size() + "):\n  "
                    + String.join("\n  ", new TreeSet<>(problems)));
        }
    }
}
