# Sprint 2 — Status & Handoff

**Updated:** 2026-07-03 (smokeshow playtest fixes added)  
**Stack:** Civs, AuraSkills, ChestShop, Essentials, RPGServer, Vault, LuckPerms, PAPI  
**Product:** Option 2 — Civs tutorials (onboarding) + RPG quests (mid-game parallel).

**Build:** both `mvn compile` OK (local, uncommitted).  
**Deploy:** Civs JAR first → RPG JAR → Linux `plugins/`.  
**Manual test:** Civs `docs/SPRINT-2-TEST-PLAN.md` (StatManager, auction BIN, SpellPreCastEvent).

**Git state (not committed):**
| Repo | Remote | Branch | State |
|------|--------|--------|-------|
| Civs | `Daniel730/Civs` | `sprint-2/civs-polish` | Uncommitted local changes |
| RPG | `Daniel730/civs-quests` | `master` | Uncommitted local changes |

**Next:** Deploy both JARs → in-game validation → commit + PR per repo → merge after tests pass.

---

## Civs (Sprint 2 — DONE locally)

Branch `sprint-2/civs-polish` on `C:\Users\Danie\Downloads\Civs-1.11.6\Civs-1.11.6\`.

| Ticket | GitHub | Status |
|--------|--------|--------|
| CIVS-006 StatManager territorial stat modifiers | [#6](https://github.com/Daniel730/Civs/issues/6) | ✅ local |
| CIVS-007 Auction house BIN native | [#7](https://github.com/Daniel730/Civs/issues/7) | ✅ local |
| CIVS-008 Wire SpellPreCastEvent | [#8](https://github.com/Daniel730/Civs/issues/8) | ✅ local |

### CIVS-006 — StatManager

- New `stats/` package: `StatManager`, `StatModifier`, `StatOperation`, `TerritorialStat`, `StatTotals`, `StatListener`.
- Per-player modifier registry (`addModifier` / `removeModifier` / `getTotals`).
- `StatListener` applies territorial stats (shop discount, PvP attack/defense) scoped to town/region; same-town PvP excluded.
- `SkillManager` consumes `SHOP_DISCOUNT` totals for Civs shop pricing.
- PAPI: `%civs_stat_<stat>%` via `PlaceHook`.

### CIVS-007 — Auction BIN

- `AuctionManager` + `AuctionListing` + `AuctionResult`; YAML persistence; expiry + pending returns.
- `/civs auction` command; browse / sell / my-listings CustomMenu GUIs (`auction-*.yml`).
- Vault deposit on purchase; `AuctionListEvent` + `AuctionPurchaseEvent` for RPG consumers.
- PAPI: `%civs_auction_listings%`, `%civs_auction_my_listings%`.
- `pt_br` + `en` auction menu strings; unit test `AuctionManagerTest`.

### CIVS-008 — SpellPreCastEvent

- `Spell.java` fires cancellable `SpellPreCastEvent` before mana consume / spell effect (non-delayed casts).
- Cancellation blocks mana spend and spell execution.
- `SpellsTests` updated for pre-cast behaviour.

### Key Civs files

| Area | Files |
|------|-------|
| Stats | `stats/StatManager.java`, `stats/StatListener.java`, `skills/SkillManager.java` |
| Auction | `auction/AuctionManager.java`, `commands/AuctionCommand.java`, `menus/auction/*`, `events/Auction*.java` |
| Spells | `spells/Spell.java`, `events/SpellPreCastEvent.java` |
| PAPI | `placeholderexpansion/PlaceHook.java` |
| Config/menus | `resources/hybrid/menus/auction-*.yml`, `main.yml`, translations |

### Civs deploy & test

```powershell
& "C:\Users\Danie\tools\apache-maven-3.9.10\bin\mvn.cmd" -f "..\Civs-1.11.6\pom.xml" package
# → target/civs-1.11.6.jar → server plugins/
```

Follow **`../Civs-1.11.6/docs/SPRINT-2-TEST-PLAN.md`** for StatManager, auction BIN, SpellPreCastEvent, and `/civs reload` checks.

---

## RPG (Sprint 2 — DONE locally)

Uncommitted on `master` at `rpg-server-plugin/`.

| Ticket | GitHub | Status |
|--------|--------|--------|
| RPG-010 Civs territorial objectives (`civs_skill_xp`, `civs_skill_level`) | [#10](https://github.com/Daniel730/civs-quests/issues/10) | ✅ (was pre-closed) |
| RPG-011 SkillTreeManager + StatModifier perks | [#11](https://github.com/Daniel730/civs-quests/issues/11) | ✅ local |
| RPG-012 Quest journal GUI | [#12](https://github.com/Daniel730/civs-quests/issues/12) | ✅ local |
| RPG-013 PAPI `%rpg_quest_progress%` | [#13](https://github.com/Daniel730/civs-quests/issues/13) | ✅ local |
| RPG-014 `/rpg reload` ChestShop re-register | [#14](https://github.com/Daniel730/civs-quests/issues/14) | ✅ local |
| RPG-015 Auction quest objectives | [#15](https://github.com/Daniel730/civs-quests/issues/15) | ✅ local |

**Deferred:** RPG-009 VeinMiner optional objective (P2, not Sprint 2 scope).

### RPG-010 — Civs territorial objectives

- `ObjectiveTypes.CIVS_SKILL_XP` / `CIVS_SKILL_LEVEL` + parsers in `ObjectiveTypeRegistry`.
- `CivsInternalSkillListener` consumes `GainExpEvent`; `CivsHook` reads skill level.
- Example quest: `quests/sprint2_examples.yml`.

### RPG-011 — SkillTreeManager & perks

- `progression/SkillTreeManager` loads YAML perks from `perks/`.
- `PerkDefinition` types: `auraskills-stat` (AuraSkills `StatModifier`) and `civs-territorial` (`TerritorialPerk` → Civs `StatManager`).
- Profile `unlockedPerks`; `/rpg perks` command; example perks `warrior_berserk.yml`, `builder_discount.yml`.

### RPG-012 — Quest journal GUI

- `/rpg journal` opens 54-slot inventory (`QuestJournalGui`, `QuestJournalHolder`, `QuestJournalListener`).
- Quests sorted by status (in progress → not started → locked → completed) with objective progress lore.

### RPG-013 — PAPI expansion

- `%rpg_quest_progress%` — primary active quest formatted progress.
- `%rpg_quest_progress_<questId>%` — per-quest progress string.
- Existing `%rpg_archetype%`, `%rpg_active_quest%` retained.

### RPG-014 — Reload integration re-register

- `RpgServerPlugin.reloadPlugin()` calls `reregisterIntegrationListeners()`.
- ChestShop hook `disable()` + `enable()`; Bukkit listeners unregistered and re-registered.

### RPG-015 — Auction + spell objectives

- `auction_list` / `auction_buy` via `AuctionQuestListener` on Civs `AuctionListEvent` / `AuctionPurchaseEvent`.
- `cast_spell` via `CivsSpellQuestListener` on `SpellPreCastEvent`.
- Example quest: `quests/sprint2_auction.yml`.

### Key RPG files

| Area | Files |
|------|-------|
| Objectives | `quest/objective/ObjectiveTypes.java`, `ObjectiveTypeRegistry.java`, `QuestManager.java` |
| Civs hooks | `listener/CivsInternalSkillListener.java`, `AuctionQuestListener.java`, `CivsSpellQuestListener.java`, `hook/CivsHook.java` |
| Perks | `progression/SkillTreeManager.java`, `perk/*`, `resources/perks/*.yml` |
| GUI | `gui/QuestJournalGui.java`, `gui/QuestJournalHolder.java`, `listener/QuestJournalListener.java` |
| PAPI | `placeholder/RpgPlaceholderExpansion.java` |
| Reload | `RpgServerPlugin.java` (`reloadPlugin`, `reregisterIntegrationListeners`) |
| Quest YAML | `quests/sprint2_examples.yml`, `quests/sprint2_auction.yml` |

### RPG deploy & test

```powershell
& "C:\Users\Danie\tools\apache-maven-3.9.10\bin\mvn.cmd" package
# → target/rpg-server-0.1.0-SNAPSHOT.jar → server plugins/ (after Civs JAR)
```

**In-game checks (RPG):**

1. `/rpg quest start sprint2_civs_skills` — mine ores; verify `civs_skill_xp` / `civs_skill_level` advance.
2. `/rpg quest start sprint2_auction` — list + buy on Civs auction; objectives complete.
3. `/rpg journal` — GUI shows quest status and progress.
4. `/papi parse me %rpg_quest_progress%` and `%rpg_quest_progress_sprint2_civs_skills%`.
5. `/rpg perks` — list perks; unlock `builder_discount` after sprint2 quest chain.
6. `/rpg reload` — repeat ChestShop transaction quest step; no duplicate listener errors.

Requires **Civs Sprint 2 JAR** deployed for auction events, StatManager perks, and `SpellPreCastEvent`.

---

## Cross-repo dependency

```
Civs CIVS-006/007/008  ──events/PAPI──►  RPG RPG-010/011/015
     StatManager              ▲                  TerritorialPerk
     Auction*Event            │                  AuctionQuestListener
     SpellPreCastEvent        └────────────────── CivsSpellQuestListener
```

Deploy **Civs first**, then RPG.

---

## Recommended next actions (user)

0. **Smokeshow playtest fixes (Civs only, local):** glowstone ring removal on upgrade, shop `shop`-group tier gate, town-owner region chest access, farm recipe GUI block materials — see `civs-custom` skill. Deploy Civs JAR; RPG unchanged.
1. **Deploy** `civs-1.11.6.jar` then `rpg-server-0.1.0-SNAPSHOT.jar` to Linux server `plugins/`.
2. **Test Civs** using `Civs-1.11.6/docs/SPRINT-2-TEST-PLAN.md`.
3. **Test RPG** using the in-game checks above (journal, perks, sprint2 quest YAMLs, PAPI).
4. **Commit + PR** Civs from `sprint-2/civs-polish`; commit + PR RPG from `master` (or feature branch).
5. **Sprint 3 started:** daily/weekly quest scaffold — see `SPRINT-3-STATUS.md`. Candidates remaining: VeinMiner (RPG-009), Civs turrets/shields (P1).

---

## Skills for agents

| Skill | Use |
|-------|-----|
| `project-stack` | Scope, backlog, sprint status |
| `civs-custom` | Civs StatManager, auction, spells |
| `rpg-server-plugin` | RPG hooks, build, reload |
| `rpg-quests` | Objectives, YAML, journal |
| `reference-plugins` | Pattern borrowing |

Prior sprint: `SPRINT-1-STATUS.md`. Full matrix: `FEATURE-EXTRACTION.md`.
