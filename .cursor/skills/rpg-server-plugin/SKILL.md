---
name: rpg-server-plugin
description: >-
  RPG Server plugin bootstrap, hooks, build, and deployment on Paper 26.1.2. Use
  for RpgServerPlugin wiring, Maven, softdepends, and hook lifecycle. For quests
  see rpg-quests skill; for scope see project-stack skill.
---

# RPG Server Plugin

> **Related skills:** [project-stack](../project-stack/SKILL.md) · [rpg-quests](../rpg-quests/SKILL.md) · [civs-custom](../civs-custom/SKILL.md) · [SPRINT-1-STATUS.md](SPRINT-1-STATUS.md) · [FEATURE-EXTRACTION.md](FEATURE-EXTRACTION.md)

## Target

| Item | Value |
|------|-------|
| Paper / Java | 26.1.2 / 25 |
| Main | `dev.daniel730.rpgserver.RpgServerPlugin` |
| Version | `0.1.0-SNAPSHOT` |
| Civs sibling | `../Civs-1.11.6/` → JAR `target/civs-1.11.6.jar` |

## Build & deploy

```powershell
& "C:\Users\Danie\tools\apache-maven-3.9.10\bin\mvn.cmd" compile
```

Output: `target/rpg-server-0.1.0-SNAPSHOT.jar`. Deploy with Civs JAR to Linux `plugins/`.

## Role

**Orchestration only** — quests, archetypes, perks, LuckPerms. Does not own towns, player XP, or shops.

| Owner | RPG hook |
|-------|----------|
| AuraSkills | `AuraSkillsHook` — objectives, rewards, StatModifier perks |
| Civs | `CivsHook` + listeners — regions, towns, `GainExpEvent` (optional) |
| ChestShop | `ChestShopHook` — merchant objectives |
| Essentials | `EssentialsHook` — balance, kits, warps |
| InteractiveBooks | `InteractiveBooksHook` — lore books |
| Vault / LuckPerms / PAPI | existing hooks |

## Package layout

```
dev.daniel730.rpgserver/
├── RpgServerPlugin.java
├── hook/          # integrations (graceful no-op when disabled)
├── quest/         # QuestManager, RewardExecutor, objective/
├── listener/      # Civs, AuraSkills, Bukkit, ChestShop, Economy
├── profile/       # PlayerProfile YAML
├── command/       # /rpg
├── placeholder/   # %rpg_*%
└── config/        # PluginConfig
```

## Init order

1. Vault, LuckPerms, config
2. Wait `SkillsLoadEvent` before AuraSkills API
3. Civs + ChestShop + Essentials + InteractiveBooks hooks
4. Register listeners + PAPI expansion

## plugin.yml

```yaml
depend: [Vault]
softdepend: [Civs, AuraSkills, ChestShop, Essentials, InteractiveBooks, PlaceholderAPI, LuckPerms, VeinMiner]
loadafter: [Vault, Civs, AuraSkills, ChestShop, Essentials, PlaceholderAPI, LuckPerms]
```

## Maven (provided scope)

Paper API, AuraSkills API, Vault, PAPI, LuckPerms, Civs JAR (system path in pom.xml). ChestShop/Essentials/IB: reflection or optional JAR — missing plugin must not break load.

## Critical rules

- **Never** duplicate AuraSkills XP in RPG YAML.
- **Never** replace AuraSkillsHook with CivsSkillHook.
- **Never** build native Civs chest shop (server has ChestShop).
- Hooks no-op when `integrations.<plugin>.enabled: false` or plugin absent.

## Quest details

See [rpg-quests](../rpg-quests/SKILL.md) skill.

## Pitfalls

| Issue | Fix |
|-------|-----|
| AuraSkills before load | Wait `SkillsLoadEvent` |
| ChestShop classpath | Reflection for `TransactionEvent` |
| Dual mana | Document `%civs_mana%` vs AuraSkills mana per perk |
| `/rpg reload` | ChestShop listener not re-registered yet (known gap) |
