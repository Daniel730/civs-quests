# Quest design notes — reference plugin patterns

Study-only repos live in `../reference-plugins/`. Runtime integrations use server plugins via `softdepend`.

## Architecture note — Civs tutorials vs RPG quests (2026-07-03)

**Problem:** Returning players see Civs `TutorialManager` steps (build shelter, shop, etc.) and RPG path quests (`warrior_path`, `merchant_path`, `builder_path`) tracking the *same* territorial actions in parallel. Journal shows RPG; Civs menu shows tutorial — no cross-link, so progression feels duplicated.

**Recommended improvement (Sprint 4):** Add a thin **onboarding router** in RPG `PlayerProfileListener` after join:

1. If Civs tutorial incomplete *and* RPG archetype unset → show one title: *"Civs ensina o básico · RPG escolhe seu destino"* with clickable `/rpg journal`.
2. When player accepts a path quest, set profile flag `onboarding-source: rpg` and optionally call Civs API to **skip** redundant tutorial steps that overlap (`replaces-civs-tutorial:` YAML field already planned in quest schema).
3. Journal section **"Escolha seu caminho"** (implemented Sprint 3) becomes the RPG-side front door; Civs tutorial remains for players who never open `/rpg journal`.

This keeps option-2 parallel paths without merging engines — just a **router + skip map**, not a unified quest engine.

---

## Borrowed patterns (what we took)

| Reference | Pattern | RPG/Civs implementation |
|-----------|---------|---------------------------|
| **BeautyQuests** | `StageType` registry + YAML stage definitions | `ObjectiveTypeRegistry` — extensible parsers, not enums |
| **BeautyQuests** | Branching via requirements | Quest `requires:` chain in `QuestManager.meetsRequirements` |
| **LMBishop Quests** | Event-indexed task types (`BlockBreak`, `PlayerKill`) | `BukkitQuestListener`, `CivsQuestListener`, hook-based listeners |
| **ChestShop** | `TransactionEvent` with BUY/SELL types | `ChestShopHook` reflection listener → `shop_buy` / `shop_sell` / `shop_revenue` |
| **KingdomX** | Kingdom invite acceptance flow | `join_town` objective via Civs `PlayerAcceptsTownInviteEvent` |
| **Heroes** | Class-tier perk gates (design only) | Archetype YAML + `SkillTreeManager` + LuckPerms nodes on quest complete |
| **AuraSkills** | `StatModifier` with named ids | Perk `auraskills-stat` → `AuraSkillsHook.addStatModifier` id `rpg_<perk_id>` |
| **VeinMiner** | Vein break batch event | `VeinMinerHook` → `PlayerVeinMineEvent` → `vein_mine` objective counts blocks in vein |

## Do NOT build natively (use server plugin or Civs core)

| Domain | Use instead |
|--------|-------------|
| Player skill XP / combat stats | **AuraSkills** — never duplicate XP ledger in RPG |
| Player chest shops | **ChestShop** — no Civs native shop while ChestShop is on server |
| Full quest engine GUI | **RPG** journal + YAML — do not ship BeautyQuests |
| MythicMobs bosses | **Civs** custom mob YAML + `CustomMobKillEvent` |
| Auction house plugin | **Civs** native BIN auction |
| Citizens NPCs | **Civs** light NPC (future P1) |
| Heroes classes/spells runtime | **Civs** spells + mana; RPG archetype YAML only |

## Objective type ownership

| Type | Event source |
|------|--------------|
| `build_region`, `join_town` | Civs Bukkit events |
| `skill_level` | AuraSkills `SkillLevelUpEvent` |
| `civs_skill_xp`, `civs_skill_level` | Civs `GainExpEvent` / level check |
| `shop_*` | ChestShop `TransactionEvent` |
| `vein_mine` | VeinMiner `PlayerVeinMineEvent` (config-gated) |
| `auction_*`, `custom_mob_kill`, `cast_spell` | Civs-native events |

## License reminder

- **MIT** (BeautyQuests, EzAuction): adapt with attribution.
- **GPL** (ChestShop, AuraSkills, LMBishop Quests): runtime softdepend OK; reference code = patterns only.
- **CC-BY-ND** (Heroes): design reference only — zero code copy.
- **Closed** (MythicMobs, KingdomX core): wiki/spec + YAML structure.

---

## Archetype quest trees (2026-07-04)

Three **non-overlapping** story paths. Accepting a path quest sets `profile.archetype` permanently; other paths are blocked in `QuestManager.isConflictingPath`. Dailies/weeklies require matching archetype + path prerequisite.

### Warrior — combat & spells

```
warrior_path (altar, zombies, Fighting 5)
  └─ sprint2_spells (cast_spell ×5, fireball ×3)
       └─ warrior_siege_prep (command_tent, pillager ×10)  ← NEW
            └─ bandit_chief_slayer (custom_mob_kill bandit_chief)
                 └─ warrior_champion (cast_spell ×10, Fighting 10)
                      └─ perks: warrior_berserk → warrior_veteran → warrior_duelist
```

**Rewards:** Fighting XP heavy, warrior perks only.  
**Dailies:** `daily_hunter` (warrior). **Weeklies:** `weekly_warrior`, `weekly_boss_hunter`.

### Merchant — shops & auction

```
merchant_path (plot7x7, shack, Farming 5, shop_sell)
  └─ sprint2_auction (auction_list, auction_buy)
       └─ merchant_shop_front (flower_shop, shop_buy ×3)  ← NEW
            └─ mercador_fortuna (shop_revenue, balance_min)
                 └─ mercador_mestre (shop_revenue, auction_buy, capital)
                      └─ perks: merchant_bazaar → merchant_golden_touch → merchant_trader
```

**Rewards:** Farming XP + money; `merchant_trader` = Civs shop discount.  
**Dailies:** `daily_mercado`, `daily_vendas`. **Weeklies:** `weekly_merchant`.

### Builder — territory & Civs skills

```
builder_path (shelter, mine stone ×32, Foraging 5)
  └─ sprint2_civs_skills (civs_skill_xp mining, civs_skill_level 2)
       └─ builder_town_hall (join_town, council_room)  ← NEW
            └─ construtor_armazem (warehouse, civs_skill_level mining 3)
                 └─ construtor_mestre (town_hall, mining 5, civs building XP)
                      └─ perks: builder_discount → builder_master (+ weekly builder_fortress)
```

**Rewards:** Foraging/mining + Civs building/mining XP, territorial perks.  
**Dailies:** `daily_quarry`, `daily_miner`, `daily_farm`. **Weeklies:** `weekly_builder`.

### Path entry uniqueness (no shared objectives)

| Path | Entry objectives | Removed from this path |
|------|------------------|------------------------|
| Warrior | altar, zombies, Fighting | shelter (builder-only) |
| Builder | shelter, quarry stone, Foraging | join_town, council_room (moved to `builder_town_hall`) |
| Merchant | plot, shack, Farming, shop_sell | — (already unique) |

### Enforcement (`QuestManager`)

- `acceptQuest` / journal accept → `isConflictingPath` before start.
- Path accept → `maybeSetArchetype` forces archetype on `warrior_path` / `merchant_path` / `builder_path`.
- `matchesPlayerArchetype` hides archetype-tagged dailies until path chosen; `archetype: neutral` bypasses lock.
