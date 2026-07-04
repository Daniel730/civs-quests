# Sprint 3 — Status & Handoff

**Updated:** 2026-07-04 (player hub GUI replaces written book)  
**Stack:** Civs, AuraSkills, ChestShop, Essentials, RPGServer, Vault, LuckPerms, PAPI  
**Branch:** `sprint-3/rpg-features`  
**Prior sprint:** `SPRINT-2-STATUS.md` (complete — smokeshow playtest validated)

---

## Session 2026-07-04 (hub navigation fix + Civs locais)

### Fixed

| Issue | Fix |
|-------|-----|
| **Broken Civs tab** | All buttons ran `/cv menu`; now open correct Civs menus via `CivsHook.openMenu()` (`main`, `port`, `region-list`, `auction-browse`, `spell-list`, `blueprints`, `class-list`, `select-town`) |
| **No back navigation** | Footer **← Voltar** with `Deque` screen stack (`PATH_PICKER`, `QUEST_TREE` sub-views) |
| **Tab refresh** | Tab clicks call `resetNavigation(tab)` + `render()` in-place (inventory stays open) |
| **Refresh reopened hub** | `refreshIfOpen()` re-renders current holder; quest progress triggers auto-refresh |

### Shipped (RPG integration)

| Item | Detail |
|------|--------|
| **Path picker sub-GUI** | Início → Escolher Caminho — 3 heads (Guerreiro/Mercador/Construtor), accept without journal |
| **Quest tree sub-GUI** | RPG tab → Árvore de Quests — vertical chain with locked/available/complete panes |
| **Quests tab** | Shows next 5 chain quests with progress bars; click accept/track |
| **Footer quick actions** | ↻ Atualizar \| ← Voltar \| ★ Rastrear \| ↺ Sync \| ✕ Fechar |
| **Civs locais** | Civs tab **Locais / Teleportes** → `MenuManager` `port` menu; Início shortcut; compass **shift+right-click** |

### Civs integration

- `CivsHook.openMenu()`, `openLocationsMenu()`, `openMainMenu()`, `openMenuFromString()` — direct `MenuManager` API (no Civs JAR change required).
- Same destinations as Civs starter book → main menu **ports** icon → `port` menu.

### Test steps

1. `/rpg hub` → **Civs** tab → **Locais / Teleportes** → Civs port list opens (hub closes).
2. Re-open hub → **Início** → **Escolher Caminho** → pick head → **← Voltar** returns to Início.
3. **RPG** → **Árvore de Quests** → click node accept/track → **← Voltar** → footer **★ Rastrear** / **↺ Sync**.
4. Shift+right-click hub compass → Civs locais directly.

---

## Session 2026-07-04 (late) — Player Hub GUI

### Shipped

| Item | Detail |
|------|--------|
| **Central do Reino** | `PlayerHubGui` — 54-slot inventory GUI (NOT written book) |
| **Tabs** | Início \| Civs \| RPG \| Config \| Quests — archetype glass borders like journal |
| **Item** | `RECOVERY_COMPASS` hub item (`rpg-hub-compass`); legacy book marker still works |
| **Commands** | `/rpg hub`, `/rpg menu`, `/rpg guide` (alias), `give\|refresh` |
| **Civs tab** | Click icons → `CivsHook.openMenu()` (`main`, `port`, `region-list`, …) — same as Civs book menus |
| **Config tab** | Green/red wool toggles for notifications + boss bar (profile YAML) |
| **Removed** | `PlayerGuideBookService` WRITTEN_BOOK pages; `PlayerGuideBookListener` |

### Replaced

- `PlayerGuideBookService` (4-page clickable book) → `PlayerHubService` + `PlayerHubGui`
- Join grant: written book → recovery compass with enchant glint
- Welcome messages point to `/rpg hub` instead of `/rpg guide`

---

## Session 2026-07-04 — Guide book + quest tree

### Shipped

| Item | Detail |
|------|--------|
| **Central do Reino** | `PlayerHubGui` + `PlayerHubService` — inventory GUI, tabs Início / Civs / RPG / Config / Quests |
| **Commands** | `/rpg hub`, `/rpg menu`, `/rpg guide` (alias), `/rpg settings notifications\|bossbar` |
| **Join** | `player-hub.on-join: true`; compass item; disabled per-quest book + IB auto-grant |
| **Archetype lock** | One path only; `ARCHETYPE_LOCKED` on conflict |
| **Overlap fix** | `daily_hunter` → warrior, `daily_farm` → merchant; neutral dailies removed |
| **Docs** | `QUEST-DESIGN-NOTES.md`, updated `rpg-quests` skill |

### Quest dedup summary

- Removed `lore-book` grants on accept from path + chain YAML (7 files).
- Scheduled quests without archetype fixed (`daily_hunter`, `daily_farm`).
- Journal + `matchesPlayerArchetype()` hide wrong-path content after choice.

---

## Sprint 3 focus

| Priority | Ticket | Scope | Status |
|----------|--------|-------|--------|
| P2 Civs | CIVS-010 Custom mob YAML | `CustomMobManager`, `bandit_chief`, `CustomMobKillEvent`, `/cv mob` | ✅ deployed + GitHub closed |
| P2 RPG | CIVS-010 integration | `custom_mob_kill`, `bandit_chief_slayer`, `CivsCustomMobHook` | ✅ deployed |
| P2 RPG | Warrior path continuation | `warrior_champion.yml` (post-boss chain) | ✅ deployed |
| P2 RPG | Merchant/Builder capstones | `mercador_mestre`, `construtor_mestre` | ✅ this session |
| P2 RPG | Builder daily content | `daily_quarry`, `daily_miner` | ✅ deployed |
| P2 RPG | Merchant dailies | `daily_mercado`, `daily_vendas` | ✅ deployed |
| P2 RPG | RPG-016 Weekly quest content | `weekly_*` + path prerequisites | ✅ deployed |
| P2 RPG | Builder mid-chain | `construtor_armazem` (warehouse + mining 3) | ✅ this session |
| P2 RPG | Warrior spell polish | `sprint2_spells` lore book + fighting XP 250 | ✅ this session |
| P2 RPG | Daily farm content | `daily_farm` (mine_block wheat ×32) | ✅ this session |
| P2 RPG | Weekly PT copy | `/cv menu` gameplay descriptions | ✅ this session |
| P2 RPG | Lore books | 4 intro books + magias_intro on server | ✅ this session |
| P2 RPG | RPG-009 VeinMiner optional objective | hook stubbed; `integrations.veinminer.enabled: false` | ⏸ deferred |
| P2 RPG | Rewards & progression polish | Consolidated reward summary, `civs-skill-xp`, LP auto-grant, 4 perks, capstone balance | ✅ deployed |

---

## Rewards & progression (2026-07-03 late evening)

### Engine

- **`RewardExecutor`** — single chat summary after quest fanfare (title/firework unchanged); formatted Vault money, PT skill names, perk line.
- **`civs-skill-xp`** reward field → `CivsHook.addSkillXp` → Civs `GainExpEvent` (builder path).
- **`lp-group`** reward field → LuckPerms inheritance node (`rpg-warrior`, `rpg-builder`, `rpg-merchant` on capstones).
- **Auto LP on complete** — every quest grants `rpg.quest.<id>` even when YAML omits `permission` (dailies trackable).
- **`progression.reward-multipliers`** in `config.yml` — `money`, `skill-xp`, `civs-skill-xp` (default 1.0).
- **PAPI:** `%rpg_perks_unlocked%`, `%rpg_active_perk_count%`.

### Perks (7 total)

| Perk | Archetype | Unlock quest | Effect |
|------|-----------|--------------|--------|
| `warrior_berserk` | warrior | `warrior_path` | AuraSkills strength +2 |
| `warrior_veteran` | warrior | `warrior_champion` | AuraSkills toughness +3 |
| `builder_discount` | builder | `sprint2_civs_skills` | Civs shop_discount +5% |
| `builder_fortress` | builder | `weekly_builder` | Civs build_speed +10% |
| `builder_master` | builder | `construtor_mestre` | Civs build_speed +15% |
| `merchant_bazaar` | merchant | `mercador_fortuna` | Civs shop_discount +3% |
| `merchant_golden_touch` | merchant | `mercador_mestre` | AuraSkills luck +2 |

### Reward balance table (money / AuraSkills XP / Civs XP)

| Tier | Quest | Money | skill-xp | civs-skill-xp | Perk |
|------|-------|------:|----------|---------------|------|
| Path | `warrior_path` | 10 | fighting 75 | — | warrior_berserk |
| Path | `builder_path` | 25 | foraging 75 | building 50 | — |
| Path | `merchant_path` | 25 | farming 50 | — | — |
| Mid | `construtor_armazem` | 100 | foraging 150 | building 75 | — |
| Mid | `sprint2_spells` | 40 | fighting 250 | — | — |
| Mid | `mercador_fortuna` | 100 | farming 200 | — | merchant_bazaar |
| Weekly | `weekly_*` | 300–400 | 500–750 | building/mining (builder) | builder_fortress |
| Boss | `bandit_chief_slayer` | 500 | fighting 1000 | — | — |
| Capstone | `warrior_champion` | 250 | fighting 500 | — | warrior_veteran + lp-group |
| Capstone | `construtor_mestre` | 400 | foraging 300 | mining 400, building 200 | builder_master + lp-group |
| Capstone | `mercador_mestre` | 500 | farming 600 | — | merchant_golden_touch + lp-group |
| Daily | `daily_*` | 30–50 | 40–100 | mining 60–100 (builder) | — |
| Daily | `daily_farm` | 40 | farming 100 | — | — |

### Test checklist

1. Complete any quest → title/firework (if enabled) **then** `★ Recompensas recebidas` bullet list in chat.
2. Builder quest with `civs-skill-xp` → Civs mining/building XP notification in-game.
3. Capstone complete → LP node `rpg.quest.<id>`, optional `lp-group`, perk auto-unlock once in summary.
4. `/papi parse me %rpg_perks_unlocked%` after perk unlock.
5. `/rpg reload` → 7 perks loaded, 18+ quests.

---

## Session handoff (2026-07-03 night batch) — mid-chain + daily farm

### What shipped

- **Builder mid-chain:** `construtor_armazem` — warehouse + Civs mining level 3; requires `sprint2_civs_skills`; `construtor_mestre` now requires `construtor_armazem` (LP chain via `unlockFollowUpQuestPermissions`).
- **Warrior spells polish:** `sprint2_spells` — `lore-book: magias_intro`, fighting XP 250 (proportional to 8 spell casts vs path 75/4 objs), `/cv menu` description.
- **Daily variety:** `daily_farm` — harvest proxy via `mine_block: wheat` ×32, farming XP 100.
- **Weekly descriptions:** PT copy tied to `/cv menu`, ChestShop, and real Civs gameplay.
- **InteractiveBooks:** `warrior_intro`, `merchant_intro`, `builder_intro`, `magias_intro` (+ chain books) deployed to `plugins/InteractiveBooks/books/`.
- **Deploy:** `mvn package` → bot-server; log: **Carregadas 22 quests**, 9 perks; backup `plugins-backup-20260703-2024`.

### Quest inventory (21 production YAML → server loads 22 incl. legacy)

`warrior_path`, `builder_path`, `merchant_path`, `sprint2_civs_skills`, `sprint2_auction`, `sprint2_spells`, `construtor_armazem`, `construtor_mestre`, `mercador_fortuna`, `mercador_mestre`, `bandit_chief_slayer`, `warrior_champion`, `daily_hunter`, `daily_quarry`, `daily_miner`, `daily_mercado`, `daily_vendas`, `daily_farm`, `weekly_warrior`, `weekly_merchant`, `weekly_builder`.

**Not loaded:** `quests/dev/sprint1_examples.yml`.

### Path chains (updated)

| Archetype | Chain |
|-----------|-------|
| Warrior | `warrior_path` → `sprint2_spells` → `bandit_chief_slayer` → `warrior_champion` |
| Merchant | `merchant_path` → `sprint2_auction` → `mercador_fortuna` → `mercador_mestre` |
| Builder | `builder_path` → `sprint2_civs_skills` → `construtor_armazem` → `construtor_mestre` |

### Capstone reward audit ✅

| Quest | lp-group | unlocks-perk | Chain unlock |
|-------|----------|--------------|--------------|
| `warrior_champion` | `rpg-warrior` | `warrior_veteran` | requires `bandit_chief_slayer` |
| `construtor_mestre` | `rpg-builder` | `builder_master` | requires `construtor_armazem` |
| `mercador_mestre` | `rpg-merchant` | `merchant_golden_touch` | requires `mercador_fortuna` |

Mid-chain LP nodes: `construtor_armazem` grants `rpg.quest.construtor_armazem`; completing it auto-grants `rpg.quest.construtor_mestre` via follow-up unlock.

### Lore books (InteractiveBooks)

| Book ID | Quest |
|---------|-------|
| `warrior_intro` | `warrior_path` |
| `merchant_intro` | `merchant_path` |
| `builder_intro` | `builder_path` |
| `magias_intro` | `sprint2_spells` |
| `leilao_intro` | `sprint2_auction` |
| `boss_guide` | `bandit_chief_slayer` |
| `chefe_bandido` | (boss context) |

---

## Session handoff (2026-07-03 night) — quest narrative polish

### What shipped

- **Capstones:** `mercador_mestre` (requires `mercador_fortuna`: shop_revenue + auction_buy + balance_min), `construtor_mestre` (requires `sprint2_civs_skills`: warehouse + civs_skill_level mining 5).
- **Dev quests hidden:** `sprint1_examples` moved to `quests/dev/` (not loaded); renamed `sprint2_examples.yml` → `sprint2_civs_skills.yml`.
- **Lore books:** `leilao_intro` on `sprint2_auction`, `chefe_bandido` on `bandit_chief_slayer` — player instructions via `/cv menu`, no admin commands.
- **Descriptions:** all production quests PT copy with Civs terms; weekly `requires` aligned (spells/auction/civs_skills).
- **Builder path:** `join_town` objective (accept town invite).
- **Dailies split:** `daily_mercado` (buy) + `daily_vendas` (sell) + `daily_miner` (vein_mine).
- **LP chain:** `unlockFollowUpQuestPermissions` grants `rpg.quest.mercador_mestre` on `mercador_fortuna` complete (no duplicate permission on parent reward).

### Quest inventory (19 production YAML → `saveResource` seeds 19)

`warrior_path`, `builder_path`, `merchant_path`, `sprint2_civs_skills`, `sprint2_auction`, `sprint2_spells`, `daily_hunter`, `daily_quarry`, `daily_mercado`, `daily_miner`, `daily_vendas`, `weekly_warrior`, `weekly_merchant`, `weekly_builder`, `bandit_chief_slayer`, `warrior_champion`, `mercador_fortuna`, `mercador_mestre`, `construtor_mestre`.

**Not loaded:** `quests/dev/sprint1_examples.yml` (admin reference only).

### Path chains

| Archetype | Chain |
|-----------|-------|
| Warrior | `warrior_path` → `sprint2_spells` → `bandit_chief_slayer` → `warrior_champion` |
| Merchant | `merchant_path` → `sprint2_auction` → `mercador_fortuna` → `mercador_mestre` |
| Builder | `builder_path` → `sprint2_civs_skills` → `construtor_mestre` |

### All quest reward permissions (LP nodes)

```
rpg.quest.warrior_path
rpg.quest.builder_path
rpg.quest.merchant_path
rpg.quest.sprint2_civs_skills
rpg.quest.sprint2_auction
rpg.quest.sprint2_spells
rpg.quest.weekly_warrior
rpg.quest.weekly_merchant
rpg.quest.weekly_builder
rpg.quest.bandit_chief_slayer
rpg.quest.warrior_champion
rpg.quest.mercador_fortuna
rpg.quest.mercador_mestre
rpg.quest.construtor_mestre
```

(`daily_*` quests: money + skill XP only — no permission reward. `sprint1_examples` dev-only → `rpg.quest.sprint1_done`.)

### In-game test checklist

1. `/rpg reload` — log shows **Carregadas 19 quests.**
2. `/rpg journal` — no `[DEV]` quests; sprint2 names in PT (Expansão Territorial, Primeiro Leilão, Iniciação às Magias).
3. Complete `mercador_fortuna` → `mercador_mestre` unlocks via LP chain (no manual grant).
4. Complete `sprint2_civs_skills` → `construtor_mestre` unlocks.
5. Start `sprint2_auction` / `bandit_chief_slayer` → lore books grant (InteractiveBooks).
6. Weekly quests require mid-path quests (spells/auction/civs_skills), not just starter path.
7. Dailies: `daily_mercado` (buy) vs `daily_vendas` (sell) — no awkward duplicate with weekly.

---

## Session handoff (2026-07-03 evening) — quest audit + LP chain fix

### Deploy (RPG polish — 2026-07-03 17:30 UTC)

- `mvn package` → `rpg-server-0.1.0-SNAPSHOT.jar` deployed (config bossbar fix + 2 new quests).
- Quest YAMLs copied: `sprint3_boss.yml` (updated), `warrior_champion.yml`, `daily_quarry.yml`.
- Server `config.yml` merged (missing `messages.*` keys added; `sync-on-join-from-civs: true` preserved).
- Log verified: `[RPGServer] Carregadas 14 quests.` + `Civs custom mob hook ativo` + `Loaded 1 custom mob definition(s)`.
- Backup: `plugins-backup-20260703-1729`.

### CIVS-010 + boss quest chain (deployed earlier same day)

- Civs: `CustomMobManager`, `bandit_chief.yml`, `CustomMobKillEvent`, `/cv mob spawn|list`.
- RPG: `custom_mob_kill` objective, `CivsCustomMobHook` (getKiller fix), `bandit_chief_slayer` quest.
- GitHub: [CIVS-010 closed](https://github.com/Daniel730/Civs/issues/10) with evidence.

### Config fix (local + server)

- Duplicate YAML key `messages.quest-bossbar` (boolean vs title map) → split to `quest-bossbar.enabled` + `quest-bossbar-title`.
- `PluginConfig` reads both old and new paths for backward compatibility.
- Merge script: `scripts/merge-server-rpg-config.py` (deep-merge missing keys only).

### New quest content

| Quest ID | File | Notes |
|----------|------|-------|
| `bandit_chief_slayer` | `sprint3_boss.yml` | requires `warrior_path`; LP `rpg.quest.bandit_chief_slayer` |
| `warrior_champion` | `warrior_champion.yml` | requires `bandit_chief_slayer`; LP `rpg.quest.warrior_champion` |
| `daily_quarry` | `daily_quarry.yml` | builder daily, `mine_block` stone ×64 |

### `/cv mob` command note

Civs main command is `/cv` (not `/civs`). Subcommand `@CivsCommand(keys={"mob"})` → `/cv mob spawn bandit_chief`. No alias fix needed.

---

## Session handoff (2026-07-03) — everything done this session

All work below is **local + uncommitted** except weekly quest deploy (2026-07-03 15:34 UTC).

### Deploy (weekly quests — 2026-07-03 15:34 UTC)

- `mvn package` → `rpg-server-0.1.0-SNAPSHOT.jar` deployed to bot-server `plugins/`.
- `weekly_warrior.yml`, `weekly_merchant.yml`, `weekly_builder.yml` copied to `plugins/RPGServer/quests/`.
- Log verified: `[RPGServer] Carregadas 11 quests.` → `[RPGServer] Enabling RPGServer v0.1.0-SNAPSHOT` → `Done (147.117s)!`
- Backups: `plugins-backup-20260703-1517`, `plugins-backup-20260703-1516`.

### 1. Sprint 2 playtest bug fixes (Civs-side, local)
Smokeshow validation surfaced fixes recorded in `SPRINT-2-STATUS.md` → "Recommended next actions #0":
- Glowstone ring removal on region upgrade.
- Shop `shop`-group tier gate.
- Town-owner region chest access.
- Farm recipe GUI block-material rendering.

RPG plugin unchanged by these; Civs JAR rebuild + redeploy required for them to go live.

### 2. Quest profile sync (`/rpg sync`)
- `QuestProgressSync` backfills RPG YAML step state from Civs / AuraSkills / Vault — **never** grants skill XP (ownership rule honored).
- Admin command `/rpg sync [player]`; `grantRewards=false` path updates step state only.
- Bulk offline backfill: `scripts/bulk-sync-quest-profiles.py`.
- Optional server flag `progression.sync-on-join-from-civs` (leave off until validated).

### 3. Daily + weekly quest content (RPG-016)
- `schedule: daily | weekly` field, `QuestScheduleReset` period rollover by `quests.reset-timezone` (UTC).
- Profile `quest-completion-times`; expired scheduled quests reset on join before Civs sync.
- Journal shows Diária / Semanal tags.
- Content files: `sprint3_daily.yml` (`daily_hunter`), `weekly_warrior.yml`, `weekly_merchant.yml`, `weekly_builder.yml` — **deployed** to server quests folder.

### Quest audit fixes (2026-07-03 evening)

- **LP chain unlock:** `QuestManager.unlockFollowUpQuestPermissions` — on quest complete, auto-grants `rpg.quest.<id>` for quests listing the completed quest in `requires` (fixes blocked follow-ups e.g. `bandit_chief_slayer` → `warrior_champion`).
- **YAML polish (PT):** path quests, `sprint2_spells` → "Iniciação às Magias", `sprint2_auction` → "Primeiro Leilão", `sprint2_civs_skills` → "Expansão Territorial"; boss/champion copy player-facing (no admin commands).
- **`bandit_chief_slayer`** now requires `warrior_path` + `sprint2_spells`; **`warrior_champion`** capstone = `cast_spell` + Fighting 10 (zombie grind removed).
- **New quests:** `mercador_fortuna` (`shop_revenue` + `balance_min`, requires `sprint2_auction`), `daily_mercado` (daily `shop_buy`, merchant archetype).
- **`sprint1_examples`** omitted from `saveResource` (admin/dev reference only; not seeded on fresh install).

### Quest inventory (16 YAML in repo → 15+ loaded on server)

`warrior_path`, `builder_path`, `merchant_path`, `sprint2_civs_skills` (`sprint2_examples.yml`), `sprint2_auction`, `sprint2_spells`, `daily_hunter` (`sprint3_daily.yml`), `daily_quarry`, `daily_mercado`, `weekly_warrior`, `weekly_merchant`, `weekly_builder`, `bandit_chief_slayer` (`sprint3_boss.yml`), `warrior_champion`, `mercador_fortuna`. (`sprint1_examples` not auto-seeded.)

`QuestManager.saveResource` seeds 15 on first install (excludes `sprint1_examples`).

### Git state (no commits made this session)
| Repo | Branch | State |
|------|--------|-------|
| RPG (`Daniel730/civs-quests`) | `master` | Uncommitted local changes (needs commit permission) |
| Civs (`Daniel730/Civs`) | `sprint-2/civs-polish` | Uncommitted local changes (needs commit permission) |

---

## RPG-016 — Daily/weekly quest scaffold

### What shipped

- **`schedule`** field on quest YAML: `daily` or `weekly` (default: one-shot / none).
- **`QuestScheduleReset`** — period expiry by `quests.reset-timezone` (default `UTC`).
- **Profile** `quest-completion-times` — timestamp on scheduled quest complete; `clearQuestState()` on period rollover.
- **Join hook** — `resetExpiredScheduledQuests()` before Civs sync.
- **Journal** — shows schedule type (Diária / Semanal) in lore.
- **Example:** `quests/sprint3_daily.yml` — `daily_hunter` (5 zombies, money + Fighting XP).
- **Weekly quests (2026-07-03):** `weekly_warrior.yml`, `weekly_merchant.yml`, `weekly_builder.yml` — archetype-gated, harder than daily, `schedule: weekly`.

### Key files

| Area | Files |
|------|-------|
| Schedule | `quest/QuestSchedule.java`, `quest/QuestScheduleReset.java` |
| Engine | `quest/Quest.java`, `quest/QuestManager.java` |
| Profile | `profile/PlayerProfile.java`, `profile/ProfileManager.java` |
| Join | `listener/PlayerProfileListener.java` |
| GUI | `gui/QuestJournalGui.java` |
| Config | `config.yml` → `quests.reset-timezone` |
| Example | `quests/sprint3_daily.yml`, `quests/weekly_*.yml` |

### YAML example

```yaml
id: daily_hunter
name: "Caçador Diário"
schedule: daily
objectives:
  - id: kill_zombies
    type: kill_mob
    mob: zombie
    amount: 5
    description: "Mate 5 zumbis"
rewards:
  money: 50
  skill-xp:
    fighting: 100
```

### Weekly quest IDs & LuckPerms

| Quest ID | Archetype | Requires | Reward permission |
|----------|-----------|----------|-------------------|
| `weekly_warrior` | warrior | `warrior_path` | `rpg.quest.weekly_warrior` |
| `weekly_merchant` | merchant | `merchant_path` | `rpg.quest.weekly_merchant` |
| `weekly_builder` | builder | `builder_path` | `rpg.quest.weekly_builder` |

**Path prerequisites (grant or earn via quest completion / `/rpg sync`):**

| Quest ID | Reward permission |
|----------|-------------------|
| `warrior_path` | `rpg.quest.warrior_path` |
| `merchant_path` | `rpg.quest.merchant_path` |
| `builder_path` | `rpg.quest.builder_path` |

**All quest reward permissions (LP nodes):**

```
rpg.quest.warrior_path
rpg.quest.builder_path
rpg.quest.merchant_path
rpg.quest.sprint1_done
rpg.quest.sprint2_civs_skills
rpg.quest.sprint2_auction
rpg.quest.sprint2_spells
rpg.quest.weekly_warrior
rpg.quest.weekly_merchant
rpg.quest.weekly_builder
rpg.quest.bandit_chief_slayer
rpg.quest.warrior_champion
rpg.quest.mercador_fortuna
```

(`daily_hunter`, `daily_quarry`, and `daily_mercado` have no permission reward — money + skill XP only.)

Grant path completion permissions first or use `/rpg sync` after Civs tutorial progress before weekly quests unlock in journal.

### In-game checks

1. `/rpg reload` — verify **11** quests loaded (or **10** if `sprint2_spells.yml` removed); copy `weekly_*.yml` to server `plugins/RPGServer/quests/` when folder already exists.
3. `/rpg journal` — weekly quests show **Semanal** tag when unlocked.
4. Complete a weekly quest — rewards grant; journal shows Concluída.
5. Next calendar week (or wait for period rollover) — rejoin; quest resets to Não iniciada.

**Daily checks (unchanged):**

1. Kill 5 zombies on `daily_hunter` — quest auto-starts and completes; rewards grant.
2. `/rpg journal` — shows **Diária** tag; status Concluída.
3. Next calendar day — rejoin; quest resets to Não iniciada.

---

## CIVS-009 — Turret region effect MVP (2026-07-03)

### What shipped (Civs only — no town shields)

- **`arrow_turret`** — shoots arrows at hostile mobs on region tick; intruders on enter (existing, hardened).
- **`damage_turret`** — direct % max-HP damage on interval (new; `basic_turret` uses this).
- **`basic_turret.yml`** — level-1 defense region, range 12, 12% damage, fence + dispenser build.
- **Config:** `use-turrets: true` master gate; `disable-arrow-turret-shooting-at-mobs` unchanged.
- **Translations:** `basic_turret` en + pt_br.
- **Tests:** `ArrowTurretTests` — `TurretParams` parsing.

### Key files (Civs repo)

| Area | Path |
|------|------|
| Effects | `regions/effects/ArrowTurret.java`, `TurretParams.java` |
| Region YAML | `resources/hybrid/item-types/defense/basic_turret.yml`, `arrow_trap.yml` |
| Config | `resources/hybrid/config.yml` → `use-turrets` |
| Tests | `src/test/.../ArrowTurretTests.java` |

### In-game checks

1. `/civs reload` — `basic_turret` appears under Shop → Defense.
2. Place `basic_turret` in town; spawn zombie in range — takes damage each tick.
3. Place `arrow_trap`; stock arrows in chest; verify arrow fire + mob damage.
4. Set `use-turrets: false` — turrets stop firing.

**Deferred:** town shields (`POWER_SHIELD`), per-chunk turret limits, KingdomX-style turret GUI.

---

## CIVS-010 — Custom mob MVP (2026-07-03) ✅ deployed

### What shipped

- **`CustomMobManager`** — YAML mob definitions, spawn, kill tracking.
- **`bandit_chief.yml`** — boss mob with elevated HP/damage.
- **`CustomMobKillEvent`** — fired on custom mob death; RPG listens via reflection.
- **`/cv mob spawn|list`** — admin spawn (op or `civs.admin`); players use `/cv` not `/civs`.
- **RPG:** `custom_mob_kill` objective + `bandit_chief_slayer` quest + `warrior_champion` follow-up.

### In-game checks

1. `/cv mob list` — shows `bandit_chief`.
2. `/cv mob spawn bandit_chief` (op) — boss spawns at player location.
3. Kill boss → `CustomMobKillEvent` → RPG quest progress on `bandit_chief_slayer`.
4. `/rpg journal` — boss quest visible after `warrior_path` complete.

---

## Server log — 2026-07-03 17:30 boot (14 quests deploy)

**Clean.** RPGServer + Civs custom mobs enabled.

```
Loaded 1 custom mob definition(s)
[RPGServer] Carregadas 14 quests.
[RPGServer] Civs custom mob hook ativo (CustomMobKillEvent via reflexão).
[RPGServer] RPGServer habilitado (v0.1.0-SNAPSHOT).
```

---

## Server log — 2026-07-03 15:34 boot (weekly deploy)

**Clean.** RPGServer enabled; weekly quests loaded.

```
[RPGServer] Carregadas 11 quests.
[RPGServer] Enabling RPGServer v0.1.0-SNAPSHOT
```

> **Note:** 11 = all repo quest YAMLs (paths + sprint examples + `daily_hunter` + 3 weekly + `sprint2_spells`). Original RPG-016 target was **10** (excl. `sprint2_spells`).

---

### Player journey polish (2026-07-03 evening — RPG-017)

- **Journal sections:** Escolha seu caminho / Seu Caminho / Missões Diárias / Semanais / Expansão Territorial
- **Chain hints:** Próximo / Requer in journal lore + quest book header
- **InteractiveBooks:** `warrior_intro`, `merchant_intro`, `builder_intro`, `boss_guide` (PT, clickable)
- **Daily login CTA:** `Missão Diária disponível!` once per day (`daily-cta-shown-day` in profile)
- **Docs:** journey map + smokeshow path in `FINAL-HANDOFF.md`; architecture note in `QUEST-DESIGN-NOTES.md`

---

## Recommended next actions

> **On return, start with [`FINAL-HANDOFF.md`](FINAL-HANDOFF.md)** — live state, test plan, commit permissions, deploy commands.

1. **In-game validate** boss chain: `/cv mob spawn bandit_chief` → kill → `bandit_chief_slayer` completes → `warrior_champion` unlocks.
2. **In-game validate** `daily_quarry` (mine 64 stone, Diária tag).
3. **In-game validate** quest notifications/bossbar (config merged; `/rpg reload` optional).
4. **Optional:** enable `progression.sync-on-join-from-civs: true` on server (already true on live config).
5. **Sprint 3 continue:** deploy Civs turret MVP (CIVS-009) or VeinMiner (RPG-009).
6. **Commit + PR** Sprint 2 + Sprint 3 when ready.

---

## Skills for agents

| Skill | Use |
|-------|-----|
| `rpg-quests` | Schedule field, YAML, journal |
| `rpg-server-plugin` | Build, deploy |
| `civs-custom` | CIVS-009 turrets |

Full matrix: `FEATURE-EXTRACTION.md`.
