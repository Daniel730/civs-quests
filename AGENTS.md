# AGENTS.md

## Cursor Cloud specific instructions

This repo (`civs-quests`) is the **RPG Server** Paper plugin. It is built with Maven
against **Java 25 / Paper 26.1.2**. The Windows/WSL paths in `.cursor/rules/*` do **not**
apply on the cloud VM — use the Linux locations below.

### Toolchain (persisted in the VM snapshot)
- JDK 25: `/opt/jdk-25` · Maven 3.9.9: `/opt/maven`.
- `JAVA_HOME` and the Maven `PATH` are exported in the agent's `~/.bashrc`. Interactive
  shells pick them up automatically; **non-interactive** scripts (like the startup update
  script) do not, so invoke Maven as `JAVA_HOME=/opt/jdk-25 /opt/maven/bin/mvn ...`.

### The Civs dependency JAR (most important gotcha)
`pom.xml` has a **`system`-scoped** dependency on `../Civs-1.11.6/target/civs-1.11.6.jar`
(resolved on the VM as `/Civs-1.11.6/target/civs-1.11.6.jar`). Nothing compiles without it.
- Always use the **custom `Daniel730/Civs` fork** (cloned at `/Civs-1.11.6`, remote `origin`
  = `https://github.com/Daniel730/Civs.git`) — **never** the upstream `Multitallented/Civs`
  (kept only as the `upstream` remote). This fork is actively maintained by another agent, so
  the startup update script re-fetches `origin/paper-26.1.2-migration` and rebuilds the jar on
  every run to stay in sync with their fixes.
- Build it from the **`paper-26.1.2-migration`** branch — **not** `master` and **not** the
  stale `v1.11.6` GitHub release. Only the migration branch has the events this plugin
  imports (`GuideNpcInteractEvent`, `TutorialChooseCompleteEvent`); using the release jar
  fails `mvn compile` with "cannot find symbol". If the fork's target branch ever changes,
  update the branch name in both the startup update script and the rebuild command below.
- Two Civs build deps are **not resolvable from public Maven repos** and are pre-installed
  into `~/.m2` (both satisfied by the `NoCheatPlus.jar` from the
  `Updated-NoCheatPlus/NoCheatPlus` `v1.5` GitHub release):
  `com.github.Updated-NoCheatPlus.NoCheatPlus:nocheatplus:1.5` and
  `fr.neatmonster:ncpplugin:1.1-SNAPSHOT`.
- To rebuild the Civs jar (only needed if the Civs source changes):
  `cd /Civs-1.11.6 && JAVA_HOME=/opt/jdk-25 /opt/maven/bin/mvn -q -B package -DskipTests`
  then it lands at `/Civs-1.11.6/target/civs-1.11.6.jar`.

### Build / test / package (run from repo root)
- Compile: `JAVA_HOME=/opt/jdk-25 /opt/maven/bin/mvn -q -B -DskipTests compile`
- Tests (JUnit4 + Mockito, 5 tests): `.../mvn -B test`
- Plugin jar: `.../mvn -q -B package` → `target/rpg-server-0.1.0-SNAPSHOT.jar`
- The `[WARNING] ... malformed project` notice comes from the `system`-scoped Civs dep and
  is expected; it does not fail the build.

### Running the plugin (manual, no client)
`RPGServer` is a server plugin — there is no standalone app. A ready test server lives at
`/home/ubuntu/mc-test` (Paper 26.1.2, offline-mode). Start it in tmux and drive it from the
server console; a real Minecraft client cannot connect (protocol 26.1.2 has no bot support),
so exercise the plugin with console commands such as `rpg reload` and `rpg help`.
- Required plugins in `plugins/` for a clean enable: `Vault` (hard `depend`), plus `Civs`,
  `AuraSkills`, `PlaceholderAPI`, `LuckPerms`, and **`FastAsyncWorldEdit`** — Civs throws
  `NoClassDefFoundError: com/sk89q/worldedit/.../Clipboard` on enable without WorldEdit/FAWE.
- **Civs data must be seeded** or Civs NPEs on a fresh start (its `towns/`, `regions/`,
  `menus/` folders must exist). `plugins/Civs` is seeded from the sibling
  `/Civs-1.11.6/Civs_servidor` data directory. Some seeded regions reference worlds that do
  not exist on the test server; the resulting "Null world / invalid region" lines are
  harmless.
- Start: `cd /home/ubuntu/mc-test && java -Xms1G -Xmx2G -jar paper.jar --nogui`. A healthy
  boot logs `Civs detectado — ... habilitados` and `RPGServer habilitado`.

### Notes
- Standard commands/placeholders/quest layout are documented in `README.md` and
  `docs/ARCHITECTURE.md`; the quest YAMLs live under `src/main/resources/quests/`.
