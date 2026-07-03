---
name: civs-custom
description: >-
  Civs Custom 1.11.6 development — towns, regions, internal skills, events, PAPI,
  native features from reference plugins (auction, mobs, turrets). Use when editing
  Civs source under ../Civs-1.11.6/, not rpg-server-plugin.
---

# Civs Custom

## Target

| Item | Value |
|------|-------|
| Root | `../Civs-1.11.6/` (package `org.redcastlemedia.multitallented.civs`) |
| Paper / Java | 26.1.2 / 25 |
| Maven | `C:\Users\Danie\tools\apache-maven-3.9.10\bin\mvn.cmd compile` |
| Hard depend | Vault |

## Civs owns (do not move to RPG)

Towns, regions, protections, siege, classes/spells, `%civs_mana%`, town bank, internal skills, shop discounts, tutorials, alliances, custom menus (`CustomMenu` + YAML).

## Internal skills (Sprint 1 done)

- XP choke point: `Civilian.resolveSkillXpGrant()` → `GainExpEvent` (cancelable).
- Public API: `Civilian.addSkillXp(String skillKey, double amount)`.
- Listener: `skills/SkillListener.java` — mining, food harvest, fishing, combat by weapon.
- PAPI: `%civs_skill_<name>_level%`, `%civs_skill_<name>_xp%`.
- Config: `resources/hybrid/skills/*.yml`; `use-skills: true` in server config.

**Note:** Food XP from eating (`CivilianListener`) AND harvesting (`SkillListener`) — distinct by design.

## Events RPG consumes

| Event | Status | Use |
|-------|--------|-----|
| `RegionCreatedEvent` | ✅ fired | `build_region` |
| `TownCreatedEvent`, `TownEvolveEvent` | ✅ | territorial quests |
| `EnterCombatEvent` | ✅ | warrior quests |
| `GainExpEvent` | ✅ Sprint 1 | Civs-internal skill objectives |
| `SpellPreCastEvent` | ⚠️ class exists, not fired | wire when needed |
| `AuctionListEvent`, `CustomMobKillEvent` | ❌ not yet | Sprint 2+ native features |

Prefer **firing Bukkit events** over exposing manager singletons to RPG.

## Key paths

| Area | Path |
|------|------|
| Civilian / XP | `civilians/Civilian.java`, `CivilianManager.java` |
| Skills | `skills/SkillManager.java`, `SkillListener.java`, `Skill.java` |
| Events | `events/*.java` |
| PAPI | `placeholderexpansion/PlaceHook.java` |
| Regions | `regions/`, `regions/effects/` (29 effects) |
| Menus | `menus/`, `resources/hybrid/menus/` |
| Spells | `spells/`, `class-types/` |

## Native features to build (reference study)

Study `../reference-plugins/` — patterns only, no copy-paste GPL.

| Feature | Reference | Pattern |
|---------|-----------|---------|
| Auction BIN | EzAuction | ListingManager + GUI + SQLite + Vault |
| Custom mobs | MythicMobs wiki | YAML in `civs-mobs/` + PDC + `CustomMobKillEvent` |
| Turrets/shields | KingdomX GUIs | Region effect + YAML menus |
| Light NPC | Citizens ShopTrait | Simplified: 1 NPC = 1 YAML shop |

**Do not** build chest shop — server uses ChestShop.

## Civs Sprint 2 tickets (next)

1. **StatManager** — territorial stats (pattern AuraSkills, Civs-owned).
2. **AuctionManager** — BIN list, expiry, tax, `CustomMenu` GUI.
3. Wire `SpellPreCastEvent` on spell cast.
4. P1: turret region effect, town shields.

## Testing

1. `mvn compile` → deploy `target/civs-1.11.6.jar`.
2. Mine/harvest/fish/kill → Civs skill XP + messages.
3. `/papi parse me %civs_skill_mining_level%`.
4. Cancel `GainExpEvent` in test listener → XP blocked.

Update `SPRINT-1-STATUS.md` § Civs when completing tickets.
