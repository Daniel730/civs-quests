# Sprint 4 — Deploy 2026-07-04 (evening)

**Deploy:** done to `daniel@bot-server` `/home/daniel/mineserver/plugins/`

## Town membership fix (join_town)

**Root cause:** `join_town` only advanced on `PlayerAcceptsTownInviteEvent` — town **founders** never fired that event. `CivsHook.isTownMember` iterated all towns via `getPeople()` instead of `TownManager.getTownsForPlayer(rawPeople)`.

**Fix:**
- `CivsHook.isTownMember` → `getTownsForPlayer` (owners + members)
- `QuestManager.checkTownMembership` + `checkBuiltRegions` + `backfillCivsState`
- Backfill on: player join, quest accept, `TownCreatedEvent`, `/rpg sync`
- `progression.sync-on-join-from-civs: true` in default config
- `builder_town_hall` text: founders count

## POI coordinates

`discoveries/pois.yml` tuned for production village:
- `council_village`: x=2036, y=68, z=-2005, radius=56
- `guild_market`, `spawn_obelisk`, `abandoned_port` near council cluster
- Hunt POIs offset for exploration

## New quests (10 YAML, Portuguese)

| ID | Focus |
|----|-------|
| `builder_fundador` | Town owner/member + council POI |
| `daily_conselho` | Daily council visit |
| `explorer_vila_conselho` | Neutral exploration chain |
| `coop_muralha` | Co-op combat after town hall |
| `warrior_defesa_vila` | Warrior patrol + pillagers |
| `merchant_feira_guilda` | Market POI + ChestShop sell |
| `weekly_comunidade` | Weekly town growth |
| `town_prefeitura` | Town hall upgrade chain |
| `daily_patrolha` | Daily warrior combat |
| `neutral_porto_perdido` | Abandoned port hunt |

## In-game smoke test

1. Town owner with `builder_town_hall` active → rejoin or `/rpg sync` → `join_town` completes
2. Accept `builder_town_hall` while already in town → instant `join_town` backfill
3. Walk to council (2036, 68, -2005) → `discover_poi` / Codex
4. `/rpg journal` → new town/exploration quests visible
5. `daily_miner` vein_mine with VeinMiner enabled

## GitHub

- RPG-009: VeinMiner objective verified (`daily_miner`, `VeinMinerHook`) — commented
- Sprint 4 P1 issues (RPG-018–022, CIVS-012): deployed with this build — commented
