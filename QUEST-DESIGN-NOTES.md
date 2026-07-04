# Quest tree design — territorial RPG

## Core rules

1. **One path per player** — accepting `warrior_path`, `merchant_path`, or `builder_path` locks the other two forever.
2. **Archetype-scoped content** — dailies, weeklies, and story quests require matching `archetype:` in YAML.
3. **No neutral overlap** — quests without `archetype` are only visible **before** path choice (legacy cleanup: assign archetype to all scheduled quests).
4. **Single guide book** — `/rpg guide` + inventory item; no per-quest InteractiveBooks spam on accept.
5. **Journal** — shows path choice (if unset), then only the player's archetype chain + matching dailies/weeklies.

## Path chains (unique, non-overlapping)

| Archetype | Path | Mid chain | Boss / capstone | Capstone perk |
|-----------|------|-----------|-----------------|---------------|
| **Warrior** | `warrior_path` → `warrior_berserk` | `sprint2_spells` → `bandit_chief_slayer` | `warrior_champion` | `warrior_veteran` + `rpg-warrior` |
| **Merchant** | `merchant_path` → `merchant_trader` | `sprint2_auction` → `mercador_fortuna` | `mercador_mestre` | `merchant_golden_touch` + `rpg-merchant` |
| **Builder** | `builder_path` | `sprint2_civs_skills` → `construtor_armazem` | `construtor_mestre` | `builder_master` + `rpg-builder` |

Shared territorial quests were removed from overlap: each path uses different Civs regions, shop vs combat objectives, and perk IDs.

## Dailies / weeklies (by archetype)

| Archetype | Daily | Weekly |
|-----------|-------|--------|
| Warrior | `daily_hunter`, `weekly_warrior`, `weekly_boss_hunter` | combat / boss |
| Merchant | `daily_mercado`, `daily_vendas`, `daily_farm` | `weekly_merchant` |
| Builder | `daily_quarry`, `daily_miner` | `weekly_builder` |

## Deduped / removed overlap

- Removed auto-grant of `warrior_intro`, `merchant_intro`, `builder_intro`, `magias_intro`, `boss_guide`, `leilao_intro` on quest accept (replaced by **Guia do Reino**).
- `daily_hunter` and `daily_farm` now require `warrior` / `merchant` archetype (were neutral = all paths).
- Path accept blocked when another path started or archetype set (`ARCHETYPE_LOCKED`).

## Player-facing entry

1. Join → **Guia do Reino** (one book) + welcome title.
2. Tab **Início** → choose path via `[Abrir Diário]`.
3. Accept exactly one path quest in journal.
4. Follow chain hints in guide (dynamic next quest) and journal.
