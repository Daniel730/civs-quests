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

**Sprint 1 DONE** — see [SPRINT-1-STATUS.md](../rpg-server-plugin/SPRINT-1-STATUS.md).

| Area | Done |
|------|------|
| Civs CIVS-001–004 | GainExpEvent, SkillListener, PAPI skills, addSkillXp |
| RPG RPG-001–008 | ObjectiveTypeRegistry, 7+ objective types, rewards, ChestShop/Essentials/IB hooks |

**Sprint 2 next:** Civs StatManager + auction BIN; RPG SkillTreeManager + journal GUI + Civs territorial objectives.

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
2. Check `SPRINT-1-STATUS.md` for what's done.
3. Minimal diff; match existing code style.
4. `mvn compile` in the repo you changed.
5. Update `SPRINT-1-STATUS.md` or backlog if completing a ticket.

## Anti-patterns

See `.cursor/rules/project-scope.mdc` — especially: no AuraSkills replacement, no native chest shop, no reference plugin installs.
