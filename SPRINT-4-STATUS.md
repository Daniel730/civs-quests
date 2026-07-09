# Sprint 4 — Master Plan Complete

**Completed:** 2026-07-09  
**Build:** Civs + RPG `mvn compile` OK

## Delivered (master plan)

| Area | Status |
|------|--------|
| Discovery + Codex + 5 objective types | done |
| Hunt spawns + loot tables + party credit | done |
| Path traits (Civs + AuraSkills) | done |
| Skill tree GUI (3-column Skyrim layout) | done |
| 37 perks, capstone exclusive choice | done |
| Rebirth + confirm GUI + Path Essence | done |
| Rotation pools (2 daily / 1 weekly) | done |
| 56 quests + 3 rescue chains | done |
| 27 POIs + `/rpg poi mark` + export | done |
| Tutorial CHOOSE → welcome quest bridge | done |
| Guide NPCs (4 static guides in Civs) | done |
| Error reporting module | done |

## Content counts

- **Quests:** 56 YAML (excl. dev examples)
- **Perks:** 37 YAML
- **POIs:** 27 in `discoveries/pois.yml`
- **Custom mobs:** 7 in Civs

## Deploy

Rebuild **both** JARs (Civs first → RPG). Not auto-deployed from this session.

## Quick test

1. `/rpg hub` → path picker → PATH_DETAIL preview → accept path
2. `/rpg tree` → 3-column perk branches
3. `/rpg codex` → discoveries
4. Walk POIs / council at 2036,68,-2005
5. Right-click guide NPCs near village
6. `/rpg rebirth` after capstone (confirm GUI)
7. `/rpg poi mark <id>` (admin) to tune coords
