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

## Sprint 2 polish (smokeshow playtest fixes — local, redeploy Civs JAR)

| Bug | Fix |
|-----|-----|
| Glowstone rings stack on town upgrade | `TownManager.removeTown(..., destroyRing=true)` when evolving town |
| Shop tier gate (`shop` group) | `SkillManager.getShopTierCap` + `isShopItemAvailable` — building skill level unlocks current tier only for `shop`-group items (e.g. copper_shop at tier 2 hides coal_shop) |
| Town owner blocked on member-placed shop | `ProtectionHandler.townLeadershipMayUseRegionChest` — town `owner` (all regions) or `recruiter` on `shop`-group regions; fixes synthetic `ally` role from `Region.getPeople()` merge |
| Plot rename enter title shows type name | `CommonScheduler.playerInRegion` — use `region.getDisplayName(player)` for enter title, not `regionType.getDisplayName` |
| Farm GUI crash / block mismatch | `MenuUtil.toItemMaterial` + `CVItem.createItemStack` map `WATER`/`REDSTONE_WIRE`/crops to display items before `ItemStack` construction |

Redeploy: `mvn package` → `target/civs-1.11.6.jar`. Existing duplicate glowstone rings at Kingdomshow need one-time manual cleanup (break ring blocks or re-upgrade after deploy).

**Note:** Food XP from eating (`CivilianListener`) AND harvesting (`SkillListener`) — distinct by design.

## Sprint 3 — CIVS-009 turret + town shield MVP (local)

| Item | Status |
|------|--------|
| `arrow_turret` effect | ✅ existing — shoots arrows at hostile mobs (tick) + intruders (enter); vars `damage.speed.spread`; skips Civs custom mobs and town members/allies |
| `damage_turret` effect | ✅ — direct % max-HP damage on interval; used by `basic_turret`; same friendly-fire guard |
| `power_shield` effect | ✅ — reduces incoming player damage by configurable %; region + town; action-bar/particle/sound feedback when absorbing damage |
| Region YAML | `defense/basic_turret.yml`, `defense/town_shield.yml` (`power_shield:25`, iron upkeep) |
| Config | `use-turrets: true`, `use-shields: true`, `default-town-shield-reduction: 15`, `shield-feedback-*`, `turret-fire-particles` |
| Shop | defense group via `ShopMenu` / folder `defense` |
| Tests | `ArrowTurretTests`, `PowerShieldEffectTests` (`ShieldParams` parsing) |
| Siege hook | Town shield requires `power > 0` or grace (matches `ProtectionHandler` explosion shield) |
| Not in scope | per-chunk turret/shield limits, KingdomX nexus GUI |

**CIVS-009:** Turret MVP + town shield MVP done locally — ready for GitHub ticket comment / deploy validation.

## Sprint 3 — CIVS-010 custom mob YAML MVP (local)

| Item | Status |
|------|--------|
| YAML DSL | ✅ `resources/hybrid/mobs/*.yml` + data folder `plugins/Civs/mobs/` override |
| Fields | `id`, `display` (locale key), `type`, `health`, `damage`, optional `despawn-seconds` (0/off), `drops` (material, amount/min-max, chance) |
| PDC tag | ✅ `civs:custom_mob_id` on spawn |
| `CustomMobKillEvent` | ✅ fired on death with `mobId`, `killer`, `location` |
| Spawn | ✅ `/civs mob spawn <id>` (admin), `/civs mob list` |
| Region stub | ✅ `custom_mob:<mob_id>` effect on `RegionUpkeepEvent`, announces nearby spawn coords |
| Examples | `bandit_chief.yml` (Pillager, 80 HP, 8 dmg), `bandit_scout.yml`, `wild_boar.yml` |
| Config | `use-custom-mobs: true` |
| Tests | `CustomMobDefinitionTests` (YAML parse) |
| i18n | en + pt_br (`custom-mob-*` keys) |
| Not in scope | boss phases, skills, MythicMobs import |

## Sprint 3 — farm I/O, hovel economy, mob spawn fix (local)

| Item | Fix |
|------|-----|
| Custom mob `/civs mob spawn` invisible | `ProtectionHandler` cancelled Pillager in `deny_mob_spawn` towns; `CustomMobManager` plugin-spawn bypass + safe Y + `setPersistent` |
| Mob findability | `MobCommand` announces world + block coords; turrets skip `civs:custom_mob_id` |
| Farm input/output | `RegionChestUtil` — icon chest = inputs; nearest other structure chest = outputs (`farm` group); single-chest farms fall back to icon chest |
| Farm tools | `Util.mergeToolRequirements`; durable `reagents` wear as tools; `item-groups` hoe/axe/shears/fishing_rod`; region-type/recipe GUIs cycle tool/material alternatives cleanly |
| NPC Hovel economy | `bank-payout` on `RegionUpkeep`; `npc_hovel.yml` → town bank ($4/hour tick ≈ $96/day before gov buffs); throttled online town-member message |

**Town shields:** `PowerShieldEffect` listens `EntityDamageEvent`; stacks best % from `getRegionEffectsAt` + town `power_shield`. Region upkeep gates shield (like turrets). Existing `ProtectionHandler` explosion blocking unchanged.

## Sprint 3 polish batch (local)

| Item | Status |
|------|--------|
| Auction BIN UX | ✅ empty browse/my-listings titles, shift-click confirm, seller notify (`AuctionFeedback`), menu refresh after buy |
| Farm polish | ✅ `RegionChestUtilTest` single-chest I/O; region menu shows `farm-tool-durability`; pt_br/en `farm-tool-tip` on tool slots |
| Custom mobs | ✅ `bandit_camp.yml` example (`custom_mob:bandit_scout`); per-region spawn cooldown (`custom-mob-region-spawn-cooldown-seconds`); `/civs mob list` shows type/HP/dmg |
| Town shields | ✅ YAML `shield-percent:` on region type (town_shield 25%, default town 15% config) |
| Turret upkeep | ✅ `basic_turret` arrow/iron upkeep; region menu `turret-repair-warning` lists missing chest materials when defense offline |

## Events RPG consumes

| Event | Status | Use |
|-------|--------|-----|
| `RegionCreatedEvent` | ✅ fired | `build_region` |
| `TownCreatedEvent`, `TownEvolveEvent` | ✅ | territorial quests |
| `EnterCombatEvent` | ✅ | warrior quests |
| `GainExpEvent` | ✅ Sprint 1 | Civs-internal skill objectives |
| `AuctionListEvent`, `AuctionPurchaseEvent` | ✅ Sprint 2 | auction quests |
| `CustomMobKillEvent` | ✅ CIVS-010 MVP | boss/kill quests (RPG listener next) |
| `SpellPreCastEvent` | ⚠️ class exists, not fired | wire when needed |

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
| Custom mobs | MythicMobs wiki | YAML in `mobs/` + PDC `civs:custom_mob_id` + `CustomMobKillEvent` ✅ MVP |
| Turrets/shields | KingdomX GUIs | Region effect + YAML menus |
| Light NPC | Citizens ShopTrait | Simplified: 1 NPC = 1 YAML shop |

**Do not** build chest shop — server uses ChestShop.

## Civs Sprint 2 tickets (next)

1. **StatManager** — territorial stats (pattern AuraSkills, Civs-owned).
2. **AuctionManager** — BIN list, expiry, tax, `CustomMenu` GUI.
3. Wire `SpellPreCastEvent` on spell cast.
4. P1: turret region effect, town shields.

## Testing

1. `mvn package` → deploy `target/civs-1.11.6.jar` via **full restart** (`rpg-server-plugin/scripts/wsl-deploy-bot-server.sh` or stop → scp → start). **Never hot-swap JARs while server runs** — causes `NoClassDefFoundError` for new classes.
2. Mine/harvest/fish/kill → Civs skill XP + messages.
3. `/papi parse me %civs_skill_mining_level%`.
4. Cancel `GainExpEvent` in test listener → XP blocked.

Update `SPRINT-1-STATUS.md` § Civs when completing tickets.
