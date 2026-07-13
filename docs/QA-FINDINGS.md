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
- ✅ **Archetype not locked when a path quest auto-starts.** `processInstantObjectives`/
  `backfillCivsState` call `ensureQuestStarted` for every workable quest, so a path quest can
  start (and path-lock via `isConflictingPath`) without any objective completing, leaving
  `archetype == null`: `/rpg profile` showed "Nenhum" and `getMiscQuests()`/
  `findNextAvailableQuest()` hid the player's own path quests. Fixed in `onQuestStarted`
  (commit "Fix: lock archetype when a path quest auto-starts"). ⬜ add regression tests.
- 📝 **`enter_combat` has two registered handlers** (`CivsQuestListener` + `CombatQuestListener`),
  both calling `handleEnterCombat`. Benign today (instant/idempotent completion) but redundant.
  ⬜ remove the duplicate to avoid a future double-count if it ever becomes count-based.
- ⬜ **`earn_money` / `balance_min` only re-checked on join + ChestShop transactions.** Money
  earned during a session doesn't progress these objectives until relog. Add a lightweight
  periodic re-check for online players.

### Config drift (defined in config.yml, not wired)
- ⬜ `quests.starter-quest-id` — hardcoded `"welcome"` in `GuideNpcQuestListener` /
  `RpgTutorialBridgeListener`. Wire to config (default `welcome`, so behavior is unchanged).
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
- ⬜ Core is ~2.4% covered. Add unit tests around `QuestManager` archetype/conflict logic and
  `RewardDefinition` parsing first (highest value, lowest Bukkit coupling).

## Execution order this pass (test-first)
1. Regression + logic tests: archetype/conflict (`QuestManager`), `RewardDefinition` parsing.
2. Wire `quests.starter-quest-id` (config, default preserves behavior) + test.
3. Periodic `earn_money`/`balance_min` re-check for online players + test.
4. Wire `settings.debug` debug logging.
5. Remove duplicate `enter_combat` handler.
6. Verify the batch live on Minecraft.
