---
name: reference-plugins
description: >-
  Study-only reference plugin repos at ../reference-plugins/ for borrowing patterns
  into Civs Custom or RPG Server. Use when implementing features inspired by
  KingdomX, Heroes, BeautyQuests, MythicMobs, Citizens, ChestShop, or AuctionHouse
  without installing those plugins on the server.
---

# Reference plugins (study only)

**Path:** `../reference-plugins/` (sibling to both repos, not deployed).

## Cloned repos

| Folder | License | Use for |
|--------|---------|---------|
| BeautyQuests | MIT | `StageType` registry, requirements, GUI patterns → RPG |
| Quests (LMBishop) | GPL | `TaskType` + event indexing → RPG objectives |
| ChestShop-3 | GPL | Event flow (already use runtime ChestShop) |
| Citizens2 | GPL | NPC/shop trait patterns → Civs light NPC |
| AuraSkills | GPL | skills.yml structure, StatModifier — **runtime = server plugin** |
| KingdomsX | partial | GUI YAML, turrets, nexus menus → Civs |
| Heroes | CC-BY-ND | **Design only** — class tiers, no code copy |
| EzAuction, Auction-House | MIT/commercial | Auction listing lifecycle → Civs |
| MythicMobs | closed | Wiki/spec only → Civs mob YAML DSL |

## Rules

1. **Runtime:** use server plugins (AuraSkills, ChestShop) via softdepend — do not ship reference JARs.
2. **Code:** reimplement patterns; MIT may adapt with attribution; GPL = patterns only; Heroes = zero copy.
3. **Native when absent:** AuctionHouse, MythicMobs, Citizens, KingdomX core → build in Civs. BeautyQuests → patterns in RPG only.

## Where to look (quick)

| Need | Read |
|------|------|
| Quest stage registry | `BeautyQuests/api/.../StageType.java`, `StageTypeRegistry.java` |
| Task types | `Quests/bukkit/.../tasktype/type/*.java` |
| Chest shop events | `ChestShop-3/.../TransactionEvent.java` |
| Auction flow | `EzAuction/.../AuctionManager*.java` |
| Kingdom GUIs | `KingdomsX/resources/languages/*/guis/` |

Full matrix: [FEATURE-EXTRACTION.md](../rpg-server-plugin/FEATURE-EXTRACTION.md).
