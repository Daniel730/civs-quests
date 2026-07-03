# Sprint 1 — Status & Handoff

**Updated:** 2026-07-03  
**Stack:** Civs, AuraSkills, ChestShop, Essentials, RPGServer, Vault, LuckPerms, PAPI  
**Product:** Option 2 — Civs tutorials (onboarding) + RPG quests (mid-game parallel).

**Build:** both `mvn compile` OK. **Deploy:** Civs JAR then RPG JAR → Linux `plugins/`.

**Next:** Sprint 2 — Civs StatManager + auction BIN; RPG SkillTreeManager + journal GUI.

---

## RPG (Sprint 1 — DONE)

| Ticket | Status |
|--------|--------|
| RPG-001 ObjectiveTypeRegistry | ✅ |
| RPG-003 kill_mob, mine_block, earn_money + build_region, skill_level | ✅ |
| RPG-004 requires: quest chain | ✅ |
| RPG-005 RewardExecutor (Vault, AuraSkills, LP) | ✅ |
| RPG-006 ChestShopHook (TransactionEvent) | ✅ |
| RPG-007 EssentialsHook | ✅ |
| RPG-008 InteractiveBooksHook | ✅ |
| RPG-002 CivsSkillHook | ❌ cancelled — keep AuraSkillsHook |

**Pending:** RPG-009 VeinMiner; `/rpg reload` ChestShop re-register; SkillTreeManager (P1).

**Test:** `/rpg quest list|status`, permissions `rpg.quest.*`, `sprint1_examples.yml` chain.

**Key files:** `quest/objective/*`, `RewardExecutor.java`, `hook/ChestShopHook.java`, `listener/BukkitQuestListener.java`, `quests/sprint1_examples.yml`.

---

## Civs (Sprint 1 — DONE)

Sprint 1 P0 tickets for Civs-internal skills (territorial/building bonuses and shop
discounts). This is the **Civs internal** skill ledger only — AuraSkills remains the
owner of player progression on the server. RPG consumes `GainExpEvent` for
Civs-internal objectives.

### Done

- **CIVS-001 — Wire `GainExpEvent`.** All Civs-internal skill XP now flows through a
  single choke point in `Civilian`. `awardSkill(...)` (both overloads) and the new
  `addSkillXp(...)` fire `GainExpEvent` before applying XP, and honour cancellation.
  Previously the event class existed but was never fired.
  - `src/main/java/org/redcastlemedia/multitallented/civs/civilians/Civilian.java`
    - New private `resolveSkillXpGrant(Skill, double)` builds and dispatches the event,
      returns the (possibly listener-adjusted, possibly vetoed) grant amount.
    - `awardSkill` overloads refactored to preview XP, fire the event, then apply.

- **CIVS-002 — Bukkit listeners for Civs-internal XP.** New `SkillListener`
  (`@CivsSingleton`, auto-registered via reflection like other listeners) covers:
  - Mining: ore blocks on `BlockBreakEvent` (silk-touch excluded) → `mining` skill.
  - Farming/food: mature crops / melons / pumpkins / cane / etc. on `BlockBreakEvent`
    → `food` skill.
  - Fishing: `PlayerFishEvent` `CAUGHT_FISH` → new `fishing` skill.
  - Combat: `EntityDeathEvent` by weapon type → `sword` / `axe` / `trident` / `bow` /
    `crossbow`.
  - `src/main/java/org/redcastlemedia/multitallented/civs/skills/SkillListener.java` (new)
  - To avoid double-counting, the pre-existing scattered awards were removed:
    - Mining `checkMiningSkill`/`blockIsOre` deleted from `ProtectionHandler`.
    - Weapon-kill awards deleted from `DeathListener.onEntityDeath`.
  - Enabled the previously TODO `FISHING` value in `CivSkills` and added
    `resources/hybrid/skills/fishing.yml` plus `fishing-skill` translations (en, pt_br).

- **CIVS-003 — PlaceholderAPI skill placeholders.** `PlaceHook` now resolves
  `%civs_skill_<name>_level%` and `%civs_skill_<name>_xp%` (via `identifier.startsWith("skill_")`).
  Unknown or untrained skills return `0`.
  - `src/main/java/org/redcastlemedia/multitallented/civs/placeholderexpansion/PlaceHook.java`

- **CIVS-004 — Public `Civilian.addSkillXp(...)`.** New public API
  `double addSkillXp(String skillKey, double amount)` (and a
  `Player`-aware overload) grants raw Civs-internal XP and fires `GainExpEvent`.
  Raw XP is stored in a new `Skill.bonusExp` field (separate from accomplishment
  counters) and persisted under the `_bonus-exp` key in the civilian YAML.
  - `src/main/java/org/redcastlemedia/multitallented/civs/skills/Skill.java`
    (`bonusExp`, `previewRawExp`, `addRawExp`, `getBonusExpKey`).
  - Persistence in `CivilianManager` load/save.

### Files changed

| File | Change |
|------|--------|
| `civilians/Civilian.java` | Central `GainExpEvent` firing; public `addSkillXp` |
| `skills/Skill.java` | `bonusExp` raw-XP support; preview helpers |
| `skills/SkillListener.java` | **New** listener: mining, farming, fishing, combat |
| `skills/CivSkills.java` | Enabled `FISHING` |
| `placeholderexpansion/PlaceHook.java` | `%civs_skill_<name>_level%` / `_xp%` |
| `protections/ProtectionHandler.java` | Removed inline mining award (moved to listener) |
| `protections/DeathListener.java` | Removed inline weapon-kill award (moved to listener) |
| `civilians/CivilianManager.java` | Load/save `bonusExp` |
| `resources/hybrid/skills/fishing.yml` | **New** fishing skill config |
| `resources/hybrid/translations/{en,pt_br}.yml` | `fishing-skill` strings |

### How to test in-game

1. Build: `C:\Users\Danie\tools\apache-maven-3.9.10\bin\mvn.cmd compile` (green),
   then package and deploy `target/civs-1.11.6.jar` to the server `plugins/`.
   Ensure `use-skills: true` in Civs config.
2. **Listeners:** mine ores (no silk touch), harvest mature crops, catch fish, and
   kill mobs with each weapon type. Each should show the Civs exp-gained message /
   action bar and increase the relevant skill in the Civs skills menu.
3. **Placeholders (PAPI):** `/papi parse me %civs_skill_mining_level%` and
   `%civs_skill_mining_xp%`; repeat for `fishing`, `food`, `sword`, etc. Unknown
   skill → `-`, untrained valid skill → `0`.
4. **`GainExpEvent` firing:** from a test plugin or the RPG plugin, register a
   listener on `org.redcastlemedia.multitallented.civs.events.GainExpEvent` and
   confirm it fires (with `getType()` = skill key, `getExp()` = amount) on any of the
   actions above, and that cancelling it blocks the XP.
5. **`addSkillXp` API:** call `civilian.addSkillXp("mining", 100)`; verify XP is
   granted, the event fires, and the value survives a `/reload` or restart (persisted
   under `skills.mining._bonus-exp`).

### Notes / left to do

- Food XP is awarded both on eating (existing `CivilianListener`) and on harvesting
  mature crops (new listener) — these are intentionally distinct sources. Revisit if
  the design should pick one.
- `addSkillXp` for accomplishment-based skills adds XP as `bonusExp` (raw), which does
  not populate per-item accomplishment counters; that is expected for the raw-XP API.
- RPG-side `CivsInternalSkillListener` now exists as a **placeholder** (registered when
  Civs is enabled) — ready to consume `GainExpEvent` once `civs_skill_*` objective types
  are added to the RPG objective registry (P2 backlog).

**Civs Sprint 2:** StatManager, auction BIN, SpellPreCastEvent wire.

---

## Skills for agents

| Skill | Use |
|-------|-----|
| `project-stack` | Scope, backlog, this file |
| `civs-custom` | Civs implementation |
| `rpg-server-plugin` | RPG hooks, build |
| `rpg-quests` | Objectives, YAML, rewards |
| `reference-plugins` | Pattern borrowing |

Full extraction matrix: `FEATURE-EXTRACTION.md`.
