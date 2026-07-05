# Sprint 4 — Exploration, Skill Tree GUI, Rebirth

**Completed:** 2026-07-04  
**Deploy:** done 2026-07-04 16:50 UTC — backup `plugins-backup-20260704-1648`. See `SPRINT-4-DEPLOY.md`.

## Scope — all done locally

| Ticket | Area | Status |
|--------|------|--------|
| RPG-018 | DiscoveryService + POI/biome objectives + Codex | **done** |
| RPG-019 | HuntSpawnService + timed quest mobs | **done** |
| RPG-020 | SkillTreeGui (Skyrim-style) + perk branches | **done** |
| RPG-021 | Path traits (class buff/debuff) | **done** |
| RPG-022 | Rebirth + Path Essence (capstone-gated) | **done** |
| RPG-023 | Quest rotation pools (daily/weekly) | **done** |
| RPG-024 | Progression + exploration quest content | **done** (21 new YAMLs, 50 total) |
| CIVS-012 | CustomMobManager.spawnForQuest + new mob defs | **done** |

## Build

```
Civs mvn compile: OK
RPG mvn compile: OK
```

## New systems

- **Objectives:** `open_hub`, `discover_poi`, `discover_biome`, `enter_combat`; `spawn-on-accept` on hunts
- **DiscoveryService** + `discoveries/pois.yml` (9 POIs — tune coords before deploy)
- **HuntSpawnService** → Civs `spawnForQuest(owner, partyRadius)`
- **SkillTreeGui** + **CodexGui** — `/rpg tree`, `/rpg codex`
- **RebirthService** — `/rpg rebirth` after capstone
- **PathTraitService** — class buff/debuff on path lock
- **LootTableService** — `hunt_common`, `hunt_rare`, `warrior_rare`
- **21 perks** (9 legacy + 12 branch)
- **7 Civs custom mobs** (4 new hunt targets)

## Commands

| Command | Description |
|---------|-------------|
| `/rpg tree` | Skyrim-style perk tree GUI |
| `/rpg codex` | Exploration discoveries |
| `/rpg rebirth` | Renascimento (requires capstone) |
| `/rpg poi mark <id>` | Admin: register POI at current location |

## Before deploy

1. Set real coordinates in `plugins/RPGServer/discoveries/pois.yml` (or `/rpg poi mark`)
2. Rebuild Civs JAR → update RPG `system` scope if needed
3. Smoke test: welcome quest, hunt spawn, skill tree unlock, rebirth preview
4. Set `quests.rotation.daily-count` / `weekly-count` in config when ready to limit rotatives

## Tonight's server safety

**No deploy.** Do not copy new `quests/` or JAR to production until smoke-tested.
