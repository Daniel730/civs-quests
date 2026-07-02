---
name: rpg-server-plugin
description: >-
  RPG Server plugin for Paper 26.1.2 — quests, perks, and orchestration layer
  integrating Civs, AuraSkills, Vault, LuckPerms, and PlaceholderAPI. Use when
  editing rpg-server-plugin source, configs, quests, hooks, or deployment.
---

# RPG Server Plugin

## Target

| Item | Value |
|------|-------|
| Minecraft / Paper | **26.1.2** (`paper-api`, e.g. `26.1.2.build.72-stable`) |
| Java | **25** |
| Plugin | `RPGServer` — `dev.daniel730.rpgserver.RpgServerPlugin` |
| Version | `0.1.0-SNAPSHOT` |
| Civs sibling | `../Civs-1.11.6/` (JAR: `target/civs-1.11.6.jar`) |

## Build

Maven: `C:\Users\Danie\tools\apache-maven-3.9.10\bin\mvn.cmd`

```powershell
& "C:\Users\Danie\tools\apache-maven-3.9.10\bin\mvn.cmd" compile
& "C:\Users\Danie\tools\apache-maven-3.9.10\bin\mvn.cmd" test
```

Output JAR: `target/rpg-server-0.1.0-SNAPSHOT.jar`. Deploy to server `plugins/`.

## Role in the stack

This plugin is the **orchestration layer** — quests, archetypes, perk unlocks, and LuckPerms gates. It does **not** replace:

| System | Owner | RPG plugin uses it for |
|--------|-------|------------------------|
| Skill XP (Farming, Mining, Fighting…) | **AuraSkills** | Triggers, rewards, stat modifiers — never duplicate XP tables |
| Towns, regions, territorial rules | **Civs** | Events, placeholders, optional manager reads |
| Economy | **Vault** | Quest rewards, balance checks |
| Permission grants / tracks | **LuckPerms** | Perk and quest-line unlocks |
| UI placeholders | **PlaceholderAPI** | Expose `%rpg_*%`; consume `%civs_*%` and `%auraskills_*%` |

## Architecture

```
RpgServerPlugin
├── hook/
│   ├── CivsHook           # RegionCreatedEvent, TownCreatedEvent, managers (read-only)
│   ├── AuraSkillsHook     # AuraSkillsApi, SkillsUser, SkillsLoadEvent
│   ├── VaultHook          # Economy
│   ├── LuckPermsHook      # Node grants, tracks
│   └── PlaceholderHook    # expansion %rpg_*%
├── quest/
│   ├── QuestManager       # active/completed state per player (YAML)
│   ├── QuestDefinition    # quests/*.yml
│   └── listeners/         # Civs + AuraSkills + Bukkit
├── progression/
│   ├── SkillTreeManager   # perks via LuckPerms + AuraSkills StatModifiers
│   └── Archetype          # warrior / builder / merchant
└── config.yml             # integration toggles
```

### Init order

1. `onEnable` — Vault, LuckPerms, register listeners
2. `SkillsLoadEvent` (AuraSkills) — validate skills, load perk tree
3. Civs ready (`PluginEnableEvent` or softdepend) — territorial quests
4. PlaceholderAPI — register `%rpg_*%` after hooks are live

## Maven coordinates

| Dependency | Coordinates | Version | Scope |
|------------|-------------|---------|-------|
| Paper API | `io.papermc.paper:paper-api` | `26.1.2.build.72-stable` | provided |
| AuraSkills | `dev.aurelium:auraskills-api-bukkit` | `2.3.5` | provided |
| Vault | `net.milkbowl.vault:VaultAPI` | `1.7` | provided |
| PlaceholderAPI | `me.clip:placeholderapi` | `2.12.2` | provided |
| LuckPerms | `net.luckperms:api` | `5.5` | provided |
| Civs | JAR local | `1.11.6` | provided/system |

Repositories: Paper (`repo.papermc.io`), PAPI (`repo.extendedclip.com`), Vault (`nexus.hc.to`), Maven Central (AuraSkills, LuckPerms).

Civs JAR path (system scope):

```xml
<systemPath>${project.basedir}/../Civs-1.11.6/target/civs-1.11.6.jar</systemPath>
```

## Integration hooks

### Civs (no public Maven artifact)

- **Events** (stable contract): `RegionCreatedEvent`, `TownCreatedEvent`, `TownEvolveEvent`, `PlayerAcceptsTownInviteEvent`, `EnterCombatEvent`, `PlayerEnterRegionEvent`
- **Managers** (read-only): `TownManager`, `RegionManager`, `CivilianManager`
- **PAPI**: `%civs_townname%`, `%civs_power%`, `%civs_mana%`, `%civs_karma%`, etc.
- **Avoid**: `GainExpEvent`, `SpellPreCastEvent` — defined but not fired in Civs 1.11.6

### AuraSkills

```java
AuraSkillsApi api = AuraSkillsApi.get();
SkillsUser user = api.getUser(uuid);
user.getSkillLevel(Skills.FIGHTING);
```

- **Events**: `SkillsLoadEvent`, `SkillLevelUpEvent`, `XpGainEvent` (cancelable, `setAmount()` for quest multipliers)
- **Perks**: add/remove `StatModifier` with unique id `rpg_<perk_id>`
- **Do not**: implement parallel skill XP, level tables, or mana regen

### Vault + LuckPerms

- Rewards: `Economy.depositPlayer()` / `withdrawPlayer()`
- Quick checks: `Permission.has(player, "rpg.quest.tier2")` via Vault
- Persistent grants: LuckPerms API — `user.data().add(Node.builder("rpg.tree.warrior.tier2").build())`
- Permission prefix from config: `rpg.quest.<id>`, `rpg.tree.<branch>.tier<N>`, `rpg.class.<archetype>`

### PlaceholderAPI

Register expansion `rpg`:

| Placeholder | Meaning |
|-------------|---------|
| `%rpg_active_quest%` | Current quest id |
| `%rpg_quest_progress%` | Step progress |
| `%rpg_archetype%` | warrior / builder / merchant |

Consume `%civs_*%` and `%auraskills_*%` in menus — do not reimplement.

## Quest design

### Starter archetypes (mirror Civs tutorials)

| Archetype | Civs trigger | AuraSkills skill | Example objective |
|-----------|--------------|------------------|-------------------|
| **warrior** | `EnterCombatEvent`, kills | Fighting, Archery | Win combat, reach Fighting 10 |
| **builder** | `RegionCreatedEvent` | Building (Civs internal) | Place farm/house region |
| **merchant** | town bank, shop | — | Earn X via Vault, join town |

Quest YAML lives in `src/main/resources/quests/*.yml`. Max active quests: `config.yml` → `quests.max-active` (default 3).

### Quest categories

| Category | Primary trigger | Reward type |
|----------|-----------------|-------------|
| Tutorial | join / `/rpg quest start` | Vault money + AuraSkills XP via API |
| Territorial | Civs region/town events | LuckPerms perk, construction bonus |
| Social | town invite, population PAPI | Civs karma, Wisdom XP |
| Combat | `EnterCombatEvent` | Fighting XP multiplier on `XpGainEvent` |
| Economy | Vault balance, upkeep paid | Foraging/Excavation XP |
| Endgame | `TownEvolveEvent`, `%civs_power%` | leadership track LuckPerms |

### XP rule (critical)

- **AuraSkills owns all skill XP.** Quest completion may call `user.addSkillXp()` or apply a temporary multiplier on `XpGainEvent` — never maintain a second XP ledger.
- Civs `skills/*.yml` XP is territorial/building-specific; do not mirror those tables in RPG plugin.
- Quest "progress" is quest-step state in RPG YAML, not skill levels.

## Config (`config.yml`)

Integration toggles under `integrations.*`. Respect `enabled: false` — hooks must no-op gracefully.

## Workflow

1. One logical change per commit; imperative message.
2. `mvn compile` (or `mvn test` when tests exist) before push.
3. Do **not** modify Civs source from this project — consume JAR/events only.
4. Commit `.cursor/skills/` and `.cursor/rules/` (project brain).

## Pitfalls

| Issue | Fix |
|-------|-----|
| Duplicate skill XP system | Delegate to AuraSkills; use `XpGainEvent.setAmount()` for bonuses only |
| Two mana systems | Civs `%civs_mana%` vs AuraSkills mana — perks must declare which |
| Access AuraSkills before load | Wait for `SkillsLoadEvent`, not `onEnable` |
| Hard-depend Civs JAR | Prefer Bukkit events; managers for read-only queries |
| Overlap with Civs tutorials | RPG quests complement or replace — document choice in quest YAML `replaces-civs-tutorial:` |

## Server plugin list (context)

Civs 1.11.6, AuraSkills 2.3.12, Vault, LuckPerms 5.5.55, PAPI 2.12.2, Essentials, ChestShop, FAWE, InteractiveBooks, VeinMiner — secondary integrations optional (ChestShop for merchant quests, InteractiveBooks for lore).
