---
name: rpg-quests
description: >-
  RPG Server quest system — ObjectiveTypeRegistry, quest YAML schema, rewards,
  requirements, listeners. Use when adding objectives, quests, RewardExecutor,
  or quest-related listeners in rpg-server-plugin.
---

# RPG quests

## Quest engine (Sprint 1)

- **Registry:** `quest/objective/ObjectiveTypeRegistry.java` — extensible types (not enum).
- **State:** `PlayerProfile` YAML — `active-quests`, `completed-quests`, `completed-objectives`, partial counts.
- **Manager:** `QuestManager` — load YAML, progress, chain gates, completion → `RewardExecutor`.
- **Never** store skill XP in profile — delegate to AuraSkills API.

## Objective types (implemented)

| type | Listener / source | YAML fields |
|------|-------------------|-------------|
| `build_region` | Civs `RegionCreatedEvent` | `region: shelter` |
| `skill_level` | AuraSkills `SkillLevelUpEvent` | `skill: fighting`, `level: 5` |
| `kill_mob` | Bukkit `EntityDeathEvent` | `mob: ZOMBIE`, `amount: 5` |
| `mine_block` | `BlockBreakEvent` | `block: STONE`, `amount: 10` |
| `earn_money` | Vault balance delta | `amount: 1000` |
| `balance_min` | Essentials/Vault | `amount: 500` |
| `shop_buy` / `shop_sell` / `shop_revenue` | ChestShop `TransactionEvent` | `amount`, optional filters |
| `join_town` | Civs `PlayerAcceptsTownInviteEvent` | optional `town:` filter |
| `vein_mine` | VeinMiner `PlayerVeinMineEvent` | optional `block:`, `amount` |

**Crop harvest:** no `harvest_crop` type — use `mine_block: wheat` on mature crop break (`daily_farm`).

### Sprint 3 quest examples

| ID | Schedule | Chain / notes |
|----|----------|---------------|
| `construtor_armazem` | — | Mid builder: warehouse + `civs_skill_level` mining 3; requires `sprint2_civs_skills` |
| `construtor_mestre` | — | Capstone: requires `construtor_armazem`; `lp-group` + `builder_master` perk |
| `daily_farm` | daily | `mine_block: wheat` ×32; farming XP |
| `sprint2_spells` | — | `lore-book: magias_intro`; fighting XP ~250 for 8 spell casts |

Add new types: register in `ObjectiveTypes` + parser in registry + listener method.

## Quest YAML schema (actual — matches `QuestManager.parseQuest` + `RewardDefinition`)

```yaml
id: warrior_path
name: "Caminho do Guerreiro"
archetype: warrior
description: "..."
lore-book: warrior_intro        # optional, deprecated — use Central do Reino (/rpg hub); IB only if grant-on-quest-start: true
requires:                       # RPG-004 chain — list of prior quest ids
  - merchant_path
objectives:
  - id: kill_zombies
    type: kill_mob
    mob: zombie                 # matched case-insensitively / substring
    amount: 10
    description: "Mate 10 zumbis"
rewards:
  money: 100                    # Vault deposit
  skill-xp:                     # AuraSkills addSkillXp per skill
    fighting: 500
  permission: rpg.quest.warrior_path   # single LuckPerms node
  essentials-kit: starter_warrior      # optional
  warp: marketplace                    # optional (Essentials)
```

Rules: `.cursor/rules/rpg-quests.mdc`. Examples: `src/main/resources/quests/`, `sprint1_examples.yml`.

## Rewards (`RewardExecutor` / `RewardDefinition`)

Fields: `money`, `skill-xp.<skill>`, `permission` (one node), `essentials-kit`, `warp`.
Each gated by `integrations.*.enabled` and hook presence. `lore-book` is deprecated — use **`PlayerHubService`** (`/rpg hub`) for player help; IB grant only when `integrations.interactivebooks.grant-on-quest-start: true`.

## Player hub GUI (Sprint 3 — replaces written book)

- **GUI:** `PlayerHubGui` + `PlayerHubHolder` + `PlayerHubListener` — 54-slot chest inventory matching `QuestJournalGui` aesthetics (archetype glass panes, enchant glint on actions).
- **Item:** `PlayerHubService` — `RECOVERY_COMPASS` with lore marker `rpg-hub-compass` (legacy `rpg-guide-book` WRITTEN_BOOK still opens hub).
- **Commands:** `/rpg hub`, `/rpg menu`, `/rpg guide` (alias), `/rpg hub give|refresh`.
- **Tabs:** Início (profile, choose path, next quest), Civs (click → `/cv` commands), RPG (journal, perks, dailies/weeklies), Config (notification/bossbar toggles), Quests (mini preview → journal).
- **Join:** `player-hub.on-join: true` — gives compass item; no WRITTEN_BOOK pages.
- **Settings:** GUI toggles call `QuestFeedbackService.toggleNotifications/toggleBossBar` (saved in profile YAML).
- **Archetype lock:** starting one path blocks other paths (`ARCHETYPE_LOCKED`).

## Requirements

- `requires:` — YAML **list** of prior quest ids (all must be completed).
- LuckPerms gate per quest: `rpg.quest.<id>` (config prefix `integrations.luckperms.quest-permission-prefix`).

## Listeners map

| Class | Events |
|-------|--------|
| `CivsQuestListener` | Civs region/town (extend for Sprint 2) |
| `AuraSkillsQuestListener` | Skill level up |
| `BukkitQuestListener` | kill, mine, craft… |
| `ChestShopQuestListener` | TransactionEvent (reflection, registered by `ChestShopHook`) |
| `EconomyQuestListener` | balance tracking on join |
| `CivsInternalSkillListener` | Civs `GainExpEvent` (placeholder — no objective type yet) |

## Adding an objective (checklist)

1. Add constant + YAML parser in `ObjectiveTypeRegistry`.
2. Handle progress in appropriate listener (index quests by type at load).
3. Support partial count in `PlayerProfile` if amount > 1.
4. Add example in `quests/*.yml`.
5. Document in this skill if non-obvious.
6. `mvn compile`.

## PAPI (expand in P1)

| Placeholder | Status |
|-------------|--------|
| `%rpg_archetype%` | ✅ |
| `%rpg_active_quest%` | ✅ (primary active quest name) |
| `%rpg_tracked_quest%` | ✅ (explicitly tracked quest name) |
| `%rpg_tracked_progress%` | ✅ (current objective + count for tracked quest) |
| `%rpg_quest_progress%` | ✅ (primary active quest progress) |
| `%rpg_quest_progress_<id>%` | ✅ (per-quest progress) |

## Pitfalls

- Wait for `SkillsLoadEvent` before AuraSkills API on enable.
- ChestShop: use reflection so missing JAR doesn't break load.
- `quests.max-active` — enforce when implementing quest start flow.
- `/rpg reload` — re-register ChestShop listener (known gap).

## Quest progress sync (`/rpg sync`)

Backfills RPG **step state only** from Civs/AuraSkills/Vault — never grants AuraSkills XP.

| Command | Permission | Behavior |
|---------|------------|----------|
| `/rpg sync` | `rpg.admin` | Sync all online players (no rewards) |
| `/rpg sync <player>` | `rpg.admin` | Sync one online player |
| `/rpg sync --rewards [player]` | `rpg.admin` | Sync and run `RewardExecutor` for newly completed quests |

Config: `progression.sync-on-join-from-civs: true` — silent sync on join (no rewards, no chat).

### Objective sync sources

| type | Source |
|------|--------|
| `build_region` | Civs `building` accomplishments, `items` stash, or owned `RegionManager` regions |
| `skill_level` | AuraSkills `getSkillLevel` |
| `civs_skill_level` | Civs `Civilian` internal skill level |
| `civs_skill_xp` | Civs skill `getTotalExp()` (floor) |
| `balance_min` | Vault balance |
| `earn_money` | Vault balance delta since quest start |
| `kill_mob` | Civs `sword`/`axe` accomplishment counts |
| `shop_*`, `auction_*`, `cast_spell`, `mine_block` | RPG progress only (no external backfill) |

Offline bulk migrate: `scripts/bulk-sync-quest-profiles.py` (Civs + AuraSkills YAML → RPG profiles).
