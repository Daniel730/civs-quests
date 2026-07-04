# Quest tree design — territorial RPG

## Core rules

1. **One path per player** — accepting `warrior_path`, `merchant_path`, or `builder_path` locks the other two forever (`QuestManager.isConflictingPath`).
2. **Archetype-scoped content** — every story quest and scheduled quest has `archetype:`; dailies/weeklies also require path prerequisite in `requires:`.
3. **No cross-path objectives** — entry quests use different verbs/regions (warrior = altar; builder = shelter + quarry; merchant = plot + shack + shop).
4. **Journal** — path choice when archetype unset; then only matching story chain + dailies/weeklies.

## Path chains (unique, non-overlapping)

### Warrior

```
warrior_path → sprint2_spells → warrior_siege_prep → bandit_chief_slayer → warrior_champion
Perks: warrior_berserk → warrior_veteran → warrior_duelist
```

### Merchant

```
merchant_path → sprint2_auction → merchant_shop_front → mercador_fortuna → mercador_mestre
Perks: merchant_bazaar → merchant_golden_touch → merchant_trader (Civs shop discount)
```

### Builder

```
builder_path → sprint2_civs_skills → builder_town_hall → construtor_armazem → construtor_mestre
Perks: builder_discount → builder_master; weekly_builder → builder_fortress
```

## Dailies / weeklies (by archetype)

| Archetype | Daily | Weekly |
|-----------|-------|--------|
| Warrior | `daily_hunter` | `weekly_warrior`, `weekly_boss_hunter` |
| Merchant | `daily_mercado`, `daily_vendas` | `weekly_merchant` |
| Builder | `daily_quarry`, `daily_miner`, `daily_farm` | `weekly_builder` |

## Rewards by path

| Path | Primary rewards |
|------|-----------------|
| Warrior | Fighting XP, warrior perks |
| Merchant | Farming XP + money, shop discounts |
| Builder | Foraging/mining + Civs building/mining XP, territorial perks |

## Enforcement

- `acceptQuest` / journal → `ARCHETYPE_LOCKED` when path or archetype conflicts.
- Path accept forces `profile.archetype` permanently.
- `archetype: neutral` on a quest bypasses archetype lock (reserved for future shared dailies).
