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

Add new types: register in `ObjectiveTypes` + parser in registry + listener method.

## Quest YAML schema (actual — matches `QuestManager.parseQuest` + `RewardDefinition`)

```yaml
id: warrior_path
name: "Caminho do Guerreiro"
archetype: warrior
description: "..."
lore-book: warrior_intro        # optional, root-level (InteractiveBooks)
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
Each gated by `integrations.*.enabled` and hook presence. `lore-book` is a **quest-root**
field granted on quest start (`ensureQuestStarted`), not part of `rewards:`.

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
| `%rpg_active_quest%` | ✅ |
| `%rpg_quest_progress%` | P1 |

## Pitfalls

- Wait for `SkillsLoadEvent` before AuraSkills API on enable.
- ChestShop: use reflection so missing JAR doesn't break load.
- `quests.max-active` — enforce when implementing quest start flow.
- `/rpg reload` — re-register ChestShop listener (known gap).
