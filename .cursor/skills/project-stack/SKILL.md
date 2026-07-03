---
name: project-stack
description: >-
  Master context for Civs Custom + RPG Server territorial RPG stack on Paper 26.1.2.
  Covers scope, server plugins, dual-skill system, ownership, backlog, and sprint
  status. Use at start of any implementation task in either repository.
---

# Project stack — Civs + RPG

## Repositories

| Repo | Root | JAR output |
|------|------|------------|
| Civs Custom | `../Civs-1.11.6/` | `target/civs-1.11.6.jar` |
| RPG Server | `rpg-server-plugin/` | `target/rpg-server-0.1.0-SNAPSHOT.jar` |
| Reference (read-only) | `../reference-plugins/` | — |

## Architecture

```
[Civs] towns · regions · siege · spells · internal skills · AH/mobs (native P2+)
   ↑ events + PAPI
[RPG] quests · archetypes · perks · LuckPerms — orchestrates, no XP ledger
   ↑ softdepend
[AuraSkills] player XP/stats  [ChestShop] shops  [Vault+Essentials] money
```

## Dual skill system

| System | Owner | RPG uses for |
|--------|-------|--------------|
| AuraSkills | AuraSkills plugin | `skill_level` objectives, rewards, `StatModifier` perks |
| Civs internal | Civs `SkillManager` | Shop discounts, territorial XP; `GainExpEvent` only |

**Never** store skill levels in RPG YAML.

## Production server (19 plugins)

Integrate: **AuraSkills, ChestShop, Civs, Essentials, InteractiveBooks, LuckPerms, PAPI, Vault, VeinMiner**.

Skip: MiniMOTD, ChestSort, ChunkLoader, Chunky, GSit, EssentialsChat, NBTAPI, prism. FAWE = Civs building (WorldEdit fork).

## Reference plugins (NOT on server → implement natively)

| Reference | Implement in | Priority |
|-----------|--------------|----------|
| KingdomX | Civs — turrets, shields, outposts | P1 |
| MythicMobs | Civs — mob YAML DSL, bosses | P2 |
| AuctionHouse | Civs — BIN auction | P0 |
| Citizens | Civs — light NPC | P1 |
| Heroes | Civs — spell/class design | P2 |
| BeautyQuests / LMBishop Quests | RPG — objective patterns only | done Sprint 1 |

## Sprint status (2026-07-03)

**Sprint 1 & 2 DONE, Sprint 3 in progress (all local/uncommitted).** See [SPRINT-1-STATUS.md](../rpg-server-plugin/SPRINT-1-STATUS.md), [SPRINT-2-STATUS.md](../rpg-server-plugin/SPRINT-2-STATUS.md), [SPRINT-3-STATUS.md](../rpg-server-plugin/SPRINT-3-STATUS.md). Live-server deploy + return checklist: [FINAL-HANDOFF.md](../rpg-server-plugin/FINAL-HANDOFF.md).

| Sprint | Area | Done |
|--------|------|------|
| 1 | Civs CIVS-001–004 | GainExpEvent, SkillListener, PAPI skills, addSkillXp |
| 1 | RPG RPG-001–008 | ObjectiveTypeRegistry, 7+ objective types, rewards, ChestShop/Essentials/IB hooks |
| 2 | Civs CIVS-006–008 | StatManager, auction BIN, SpellPreCastEvent + smokeshow bug fixes |
| 2 | RPG RPG-010–015 | Civs territorial objectives, SkillTreeManager+perks, journal GUI, PAPI progress, reload re-register, auction/spell objectives |
| 3 | RPG-016 | Daily/weekly quest scaffold + content (`sprint3_daily`, `weekly_*`); `/rpg sync` profile backfill |

**Live server:** pre-Sprint-3 JAR (7 quests, 2 perks). Sprint 2/3 changes need deploy + commit.
**Sprint 3 remaining:** [CIVS-009](https://github.com/Daniel730/Civs/issues/9) turret MVP local (shields open, not deployed), [RPG-009](https://github.com/Daniel730/civs-quests/issues/9) VeinMiner (deferred). CIVS-010 closed + deployed. GitHub sync: [GITHUB-SYNC.md](../rpg-server-plugin/GITHUB-SYNC.md).

## Backlog quick ref

Full matrix: [FEATURE-EXTRACTION.md](../rpg-server-plugin/FEATURE-EXTRACTION.md).

**Civs P0 remaining:** StatManager, auction house BIN.  
**RPG P1:** SkillTreeManager, `%rpg_quest_progress%`, CivsInternalSkillListener (optional), RPG-009 VeinMiner.

## Tutorials vs quests

- **Civs tutorials** — first-time mechanics (build, menu, upkeep).
- **RPG quests** — archetype paths (warrior/builder/merchant), same regions, independent state.
- Flag `replaces-civs-tutorial:` in quest YAML when superseding (future).

## Workflow for agents

1. Read this skill + the repo-specific skill (`civs-custom` or `rpg-server-plugin` / `rpg-quests`).
2. Check `SPRINT-*-STATUS.md` for what's done.
3. Minimal diff; match existing code style.
4. `mvn compile` in the repo you changed.
5. Update sprint status or `FEATURE-EXTRACTION.md` if completing a ticket.
6. **Sync GitHub issues (both repos)** — create/close/comment via `gh`; see [GITHUB-SYNC.md](../rpg-server-plugin/GITHUB-SYNC.md). Run after sprint work, deploy, and before handoff.

## Anti-patterns

See `.cursor/rules/project-scope.mdc` — especially: no AuraSkills replacement, no native chest shop, no reference plugin installs.
