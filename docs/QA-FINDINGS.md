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

### UX / HUD coexistence (AuraSkills)
- ✅ **Quest ActionBar no longer fights AuraSkills/Civs** — `feedback.transient-channel`
  (`auto|chat|actionbar|none`, default `auto`): when AuraSkills is enabled, short
  quest pulses go to chat; titles + tracked boss bar unchanged. Tests:
  `TransientHudChannelTest`.
- ✅ Journal copy: clearer track/accept/next-step hints (pt_BR).
- ✅ **Composed ActionBar HUD** — `hud.composed.enabled` merges AuraSkills HP +
  Civs mana (`%civs_mana_pair%`) + tracked quest into one ActionBar via PAPI.
  Pair with Civs `mana-hud: composed` and AuraSkills `action_bar.idle: false`.
  Tests: `ComposedHudComposerTest`.
- ✅ **Hide vanilla hearts** — optional mini resource pack
  (`hud.hide-vanilla-hearts`) blanks heart sprites; hunger stays; HP/mana stay
  on composed ActionBar. Clients must accept the pack (force + prompt).

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
- ✅ **`enter_combat` duplicate handlers** — removed from `CivsQuestListener`; only
  `CombatQuestListener` (Civs soft-enabled) handles `EnterCombatEvent` now.
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
  Civs 1.11.7 build (hook logs "ativo").
- ✅ Cross-repo integrity: `CivsIntegrationIntegrityTest` asserts hub menu keys
  (`spell-list`, `class-list`, …) and `custom_mob_kill` mob ids exist in sibling
  `../Civs-1.11.6` pack (`Civs_servidor` + hybrid). Skips if Civs checkout absent.
- ✅ Soft-depend classlinkage: `AuraSkills` / `LuckPerms` hard imports aborted
  `onEnable` when those plugins were absent (`NoClassDefFoundError`). Fixed with
  no-op base + `SoftHookFactory` reflective active load + reflective
  `AuraSkillsQuestListener` registration. Tests: `SoftHookFactoryTest`.
- ✅ Live QA (Cloud VM, offline Mojang client **Smokeshow**, Paper 26.1.2):
  `/rpg hub` / journal / sync PASS; Magias/Combate menus resolve; 56 quests;
  Civs custom mob hook ativo; guide NPCs spawn (4) after QA coords + Civs dedupe fix.

### Test coverage
- ✅ Core unit tests + content integrity + Civs pack alignment tests + soft-hook factory.
  ⬜ still lots of surface left (sync, rewards execution, GUIs).

### HUD / resource pack
- ✅ **Composed ActionBar** owns HP + Civs mana (`hud.composed`).
- ✅ **Hide vanilla hearts** — server cannot blank hearts without a client resource pack.
- ✅ **Hearts-slot layout (fix)** — prior miss: hearts were blanked but HP/mana stayed as
  center ActionBar text. Now `layout: hearts-slot` sends bitmap HP/mana bars via font
  `rpg:hud` with negative-space shift onto the vacated hearts row. Hunger stays vanilla;
  quest stays on BossBar. Tune `hud.composed.hearts-slot.shift-left` if GUI scale drifts.
  Pack: `resource-packs/hide-vanilla-hearts.zip` + `HideHeartsPackService` HTTP.

## Execution order this pass (test-first)
1. ✅ Remove duplicate `enter_combat` handler.
2. ✅ Civs hub menu / custom-mob integrity tests against sibling Civs checkout.
3. ✅ Soft-depend AuraSkills/LuckPerms enable without hard linkage (SoftHookFactory).
4. ✅ Live Minecraft QA (Smokeshow): hub Civs menus, journal, sync, guide spawn.
5. Wire `settings.debug` debug logging (deferred).
