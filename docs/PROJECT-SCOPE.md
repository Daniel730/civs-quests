# Civs + RPG Server — Project Scope & Vision

**Last updated:** 2026-07-04  
**Stack:** Paper 26.1.2, Java 25  
**Repos:** `Civs-1.11.6` (world engine) + `rpg-server-plugin` (orchestration)

---

## 1. Product vision

A **territorial RPG Minecraft server** blending:

| Inspiration | What we borrow | Where it lives |
|-------------|----------------|----------------|
| **Skyrim** | Skill trees, perk branches, archetype paths, exploration codex, hunt bosses | RPG plugin (SkillTreeGui, Codex, perks, hunt quests) |
| **Valheim** | Town building, territorial progression, cooperative structures, upkeep | Civs (regions, towns, siege, internal building skills) |
| **Enshrouded** | POI discovery, biome exploration, mystery locations, timed hunt spawns | RPG DiscoveryService + Civs custom mobs |
| **Territorial RPG** | Towns, regions, power, ranks, auction, spells | Civs core |
| **Classic RPG quests** | Chains, dailies/weeklies, rewards, YAML questlines | RPG QuestManager |

**Onboarding:** Civs `TutorialManager` handles first-time mechanics; RPG quests run parallel mid/endgame paths.

---

## 2. Architecture

```
[Civs Custom]  towns · regions · siege · spells · internal skills · custom mobs
      ↑ events + PAPI + menus
[RPG Server]   quests · archetypes · perks · LuckPerms · hub GUI · skill tree · codex
      ↑ softdepend
[AuraSkills]   player XP/stats  [ChestShop] shops  [Vault+Essentials] money  [LuckPerms] gates
```

### Ownership

| Domain | Owner |
|--------|-------|
| Player skill XP & combat stats | **AuraSkills** |
| Territorial/building skills, shop discounts | **Civs** |
| Towns, regions, siege, custom mobs | **Civs** |
| Quest step state, archetypes | **RPG** YAML only |
| Money | **Vault** |
| Unlocks | **LuckPerms** |

---

## 3. Civs Custom

- Towns, regions, upkeep, custom mobs (`custom_mob:<id>` on region upkeep)
- `guild_thief` in council_room via `RegionUpkeepEvent` + `CustomMobSpawnEffect`
- Hunt mobs via `spawnForQuest` for RPG quests

---

## 4. RPG Server

- **Hub GUI** (`/rpg hub`) — Civs tab opens menus; back returns to hub
- **Quests** — 50+ YAML, objective registry, `/rpg sync` backfill
- **Skill tree / Codex / Rebirth** — Sprint 4 systems
- **Rewards** — Vault, AuraSkills XP, LuckPerms, loot tables, perks

---

## 5. Build & deploy

```powershell
& "C:\Users\Danie\tools\apache-maven-3.9.10\bin\mvn.cmd" -f "..\Civs-1.11.6\pom.xml" package
& "C:\Users\Danie\tools\apache-maven-3.9.10\bin\mvn.cmd" package
wsl bash /mnt/c/Users/Danie/Downloads/Civs-1.11.6/rpg-server-plugin/scripts/wsl-deploy-bot-server.sh
```

---

## 6. In-game test checklist

1. Hub → Civs menu → Back returns to Central do Reino
2. `welcome` quest: open hub + discover POI → rewards fire
3. Council room: `guild_thief` spawns on upkeep when player nearby (300s cooldown)
4. Hunt quests: spawn-on-accept + kill credit via `CustomMobKillEvent`
5. `/rpg tree`, `/rpg codex`, path archetype lock
