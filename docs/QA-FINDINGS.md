# RPG Server — QA findings & backlog

Living document produced from a full audit of `dev.daniel730.rpgserver` (Paper 26.1.2).
Tracks bugs, gaps, and planned work. Updated as items land.

## Architecture map (summary)

- **Events → Listeners/Hooks → `QuestManager.handle*` → `PlayerProfile` (YAML) → `RewardExecutor`.**
- 21 objective types (`ObjectiveTypes`), all wired to at least one listener/handler.
- Progression: quests, archetypes (warrior/builder/merchant), perks (`SkillTreeManager`),
  rebirth (`RebirthService`), discovery (POIs/biomes), daily/weekly rotations.
- Integrations (graceful no-op when absent): Civs, AuraSkills, Vault, ChestShop, Essentials,
  LuckPerms, PlaceholderAPI, VeinMiner.

## Findings

Legend: ✅ done · 🔨 in progress · ⬜ planned · 📝 noted (won't change now)

### Bugs / correctness
- ✅ **Only 37 of 56 bundled quests were extracted on first run.** `loadQuests()` copied a
  hardcoded list of 37 quest resources to a fresh data folder, silently dropping 19 shipped
  quests (`rescue_treasurer`, `merchant_ledger`, `warrior_oath`, `builder_fundador`, …) so they
  never loaded. Now enumerates `quests/*.yml` from the plugin jar (excluding `quests/dev/`), with
  the old list as fallback. **Live-verified: "Carregadas 56 quests"** (was 37). Unit test on the
  `isBundledQuestResource` predicate.
- ✅ **Archetype not locked when a path quest auto-starts.** `processInstantObjectives`/
  `backfillCivsState` call `ensureQuestStarted` for every workable quest, so a path quest can
  start (and path-lock via `isConflictingPath`) without any objective completing, leaving
  `archetype == null`: `/rpg profile` showed "Nenhum" and `getMiscQuests()`/
  `findNextAvailableQuest()` hid the player's own path quests. Fixed in `onQuestStarted`;
  ✅ regression tests added (`QuestManagerLogicTest`).
- 📝 **`enter_combat` has two registered handlers** (`CivsQuestListener` + `CombatQuestListener`),
  both calling `handleEnterCombat`. Benign today (instant/idempotent completion) but redundant.
  ⬜ remove the duplicate to avoid a future double-count if it ever becomes count-based.
- ✅ **`earn_money` / `balance_min` only re-checked on join + ChestShop transactions.** Money
  earned during a session didn't progress these objectives until relog. Added a per-minute
  re-check for online players (gated on Vault economy). **Live-verified**: money given to an
  online player completed a `balance_min` objective on the next tick (no relog/sync).

### Config drift (defined in config.yml, not wired)
- ✅ `quests.starter-quest-id` — was hardcoded `"welcome"`; now read from config (default
  `welcome`, behavior unchanged) in both onboarding listeners, with a null-quest guard.
- ⬜ `settings.debug` — `isDebug()` exists but has no callers. Wire to gate debug logging.
- 📝 `integrations.civs.require-town-for-quests` — not implemented. Behavior-changing; needs a
  product decision before wiring.
- 📝 `integrations.auraskills.sync-on-join` — join sync currently keys off
  `progression.sync-on-join-from-civs`. Overlapping/undocumented; leave for a design pass.
- 📝 `progression.default-track`, `quests.enabled`, `settings.locale`,
  `integrations.placeholderapi.register-expansion` — unused; leave/clean later.

### Integration (Civs-side — coordinate with the Civs fork owner)
- 📝 `cast_spell` depends on Civs firing `SpellPreCastEvent` (may not fire yet).
- 📝 `custom_mob_kill` uses `CustomMobKillEvent` via reflection — present in the current
  `paper-26.1.2-migration` Civs build (hook logs "ativo").

### Test coverage
- ✅ Added first core unit tests: `QuestManagerLogicTest` (archetype/conflict/status + quest
  resource predicate), `RewardDefinitionTest` (reward parsing), `PluginConfigTest` (typed config).
  Suite grew from 5 → 24 tests. ⬜ still lots of surface left (sync, rewards execution, GUIs).

## Execution order this pass (test-first)
1. Regression + logic tests: archetype/conflict (`QuestManager`), `RewardDefinition` parsing.
2. Wire `quests.starter-quest-id` (config, default preserves behavior) + test.
3. Periodic `earn_money`/`balance_min` re-check for online players + test.
4. Wire `settings.debug` debug logging.
5. Remove duplicate `enter_combat` handler.
6. Verify the batch live on Minecraft.
