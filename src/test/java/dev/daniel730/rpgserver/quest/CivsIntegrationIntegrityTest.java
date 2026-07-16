package dev.daniel730.rpgserver.quest;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Cross-repo guards for Civs ↔ RPG hub/NPC/mob alignment. Skips gracefully if the sibling
 * Civs checkout is not present next to this module ({@code ../Civs-1.11.6}).
 */
public class CivsIntegrationIntegrityTest {

    private static final File RESOURCES = new File("src/main/resources");
    private static final File CIVS_ROOT = new File("../Civs-1.11.6");
    private static final List<String> HUB_CIVS_MENUS = Arrays.asList(
            "main", "port", "select-town", "region-list", "auction-browse",
            "spell-list", "blueprints", "class-list");

    private static Set<String> civsMobIds;
    private static Set<String> civsMenuNames;
    private static boolean civsPresent;

    @BeforeClass
    public static void loadCivsPack() {
        civsPresent = CIVS_ROOT.isDirectory();
        civsMobIds = new HashSet<>();
        civsMenuNames = new HashSet<>();
        if (!civsPresent) {
            return;
        }
        File servidorMobs = new File(CIVS_ROOT, "Civs_servidor/mobs");
        File hybridMobs = new File(CIVS_ROOT, "src/main/java/resources/hybrid/mobs");
        loadYamlIds(servidorMobs, civsMobIds);
        loadYamlIds(hybridMobs, civsMobIds);

        File servidorMenus = new File(CIVS_ROOT, "Civs_servidor/menus");
        File hybridMenus = new File(CIVS_ROOT, "src/main/java/resources/hybrid/menus");
        loadMenuNames(servidorMenus, civsMenuNames);
        loadMenuNames(hybridMenus, civsMenuNames);
    }

    private static void loadYamlIds(File dir, Set<String> into) {
        if (!dir.isDirectory()) {
            return;
        }
        File[] files = dir.listFiles((d, name) -> name.endsWith(".yml"));
        if (files == null) {
            return;
        }
        for (File file : files) {
            String id = YamlConfiguration.loadConfiguration(file).getString("id");
            if (id == null || id.isBlank()) {
                id = file.getName().replace(".yml", "");
            }
            into.add(id.toLowerCase());
        }
    }

    private static void loadMenuNames(File dir, Set<String> into) {
        if (!dir.isDirectory()) {
            return;
        }
        File[] files = dir.listFiles((d, name) -> name.endsWith(".yml"));
        if (files == null) {
            return;
        }
        for (File file : files) {
            into.add(file.getName().replace(".yml", "").toLowerCase());
        }
    }

    @Test
    public void customMobKillObjectivesReferenceKnownCivsMobs() {
        Assume.assumeTrue("Civs sibling checkout missing at " + CIVS_ROOT.getAbsolutePath(), civsPresent);
        assertTrue("expected Civs custom mob definitions", !civsMobIds.isEmpty());

        List<String> problems = new ArrayList<>();
        File[] questFiles = new File(RESOURCES, "quests").listFiles((d, n) -> n.endsWith(".yml"));
        if (questFiles == null) {
            fail("no quest files");
            return;
        }
        for (File file : questFiles) {
            YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
            String qid = cfg.getString("id", file.getName());
            for (Map<?, ?> raw : cfg.getMapList("objectives")) {
                if (!"custom_mob_kill".equals(raw.get("type"))) {
                    continue;
                }
                Object mob = raw.get("mob");
                if (mob == null || !civsMobIds.contains(mob.toString().toLowerCase())) {
                    problems.add(qid + ": custom_mob_kill unknown mob '" + mob + "'");
                }
            }
        }
        assertNoProblems(problems);
    }

    @Test
    public void hubCivsMenusExistInCivsPack() {
        Assume.assumeTrue("Civs sibling checkout missing at " + CIVS_ROOT.getAbsolutePath(), civsPresent);
        List<String> problems = new ArrayList<>();
        for (String menu : HUB_CIVS_MENUS) {
            if (!civsMenuNames.contains(menu.toLowerCase())) {
                problems.add("hub opens missing Civs menu '" + menu + "'");
            }
        }
        assertNoProblems(problems);
    }

    private static void assertNoProblems(List<String> problems) {
        if (!problems.isEmpty()) {
            fail("Civs integration integrity problems (" + problems.size() + "):\n  "
                    + String.join("\n  ", new TreeSet<>(problems)));
        }
    }
}
