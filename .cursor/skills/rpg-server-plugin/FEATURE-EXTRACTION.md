# Feature Extraction — Reference Plugins → Civs + RPG

**Policy (revised):** Plugins **on the live server** are integrated via `softdepend` hooks where they cover a domain. Reference plugins **not on server** (KingdomX, Heroes, BeautyQuests, MythicMobs, Citizens, AuctionHouse) remain **study-only** in `../reference-plugins/` — patterns inform native Civs/RPG implementation.

**Reference repos:** `../reference-plugins/` (study patterns; do not copy GPL/CC-BY-ND code verbatim).

## Server plugin stack (19 plugins — production)

| Plugin | Role on server | Integration | Quest/perk/economy without reimplementing |
|--------|----------------|-------------|-------------------------------------------|
| **MiniMOTD** | MOTD / server list branding | **Skip** | — |
| **AuraSkills** | Primary skill XP, levels, stats, mana | **RPG softdepend** (`AuraSkillsHook`) | `skill_level` objectives, `SkillLevelUpEvent`, `XpGainEvent` multipliers, perk `StatModifier`, quest rewards via `addSkillXp()`, PAPI `%auraskills_*%` |
| **ChestShop** | Player sign+chest shops | **RPG softdepend** (`ChestShopHook`) | `shop_buy` / `shop_sell` objectives via `TransactionEvent`; merchant archetype quests; no native Civs chest shop unless ChestShop absent |
| **ChestSort** | Inventory sorting QoL | **Skip** | — |
| **ChunkLoader** | Keep chunks loaded | **Skip** | — |
| **Chunky** | World pre-generation | **Skip** | — |
| **Civs** | Towns, regions, siege, territorial economy, internal building skills, spells/mana | **Core** (events + managers + PAPI) | `build_region`, town/siege quests, `%civs_*%`, shop discounts via Civs internal skills, karma, power, mana for spells |
| **Essentials** | Economy provider (Vault), kits, warps, homes | **RPG softdepend** (`EssentialsHook`) | `balance_min` objectives, quest kit rewards (`/kit`), warp-to-location quest steps; balance via Vault (Essentials as provider) |
| **EssentialsChat** | Chat formatting / channels | **PAPI only** (optional) | Quest messages use existing chat; no hook required |
| **FastAsyncWorldEdit** | Fast region paste/edit for builders | **Civs native** (WorldEdit-compatible) | Region construction, schematic placement; Civs already softdepends `WorldEdit` — FAWE is WE fork |
| **GSit** | Sit/lay on blocks (cosmetic) | **Skip** | — |
| **InteractiveBooks** | Custom written books with click actions | **RPG softdepend** (`InteractiveBooksHook`) | Quest lore delivery, clickable “accept quest” books, tutorial handouts |
| **LuckPerms** | Permissions, tracks, groups | **RPG softdepend** (existing) | Perk/quest-line unlocks, requirement gates |
| **NBTAPI** | NBT library for other plugins | **Skip** | Transitive dependency only |
| **PlaceholderAPI** | Placeholder bridge | **RPG softdepend** (existing) | `%rpg_*%`, `%civs_*%`, `%auraskills_*%`, `%vault_eco_balance%` |
| **prism** | Block rollback / logging (admin) | **Skip** | Admin tool; no quest integration |
| **RPGServer** | Quests, archetypes, perk orchestration | **This project** | — |
| **Vault** | Economy abstraction | **RPG depend** (existing) | `earn_money` objectives, quest payouts |
| **VeinMiner** | Vein-mining enabler | **RPG softdepend** (optional) | `vein_mine` objective or Excavation XP synergy via Bukkit block-break events |

### Dual skill system (document clearly)

| System | Owner | Used for |
|--------|-------|----------|
| **AuraSkills** | AuraSkills plugin | Quest objectives (`skill_level`), perk stat modifiers, primary player progression (Fighting, Mining, Farming…), `%auraskills_*%` |
| **Civs internal skills** | Civs `SkillManager` | Territorial/building perks, **shop discounts**, region bonuses — **not** quest XP ledger |

RPG **never** stores parallel skill XP. Quest rewards call AuraSkills API; Civs `GainExpEvent` is for Civs-internal skill hooks only (e.g. building XP in regions), not a replacement for AuraSkills.

## Integration matrix

| Domain | Use existing (softdepend) | Implement in Civs | Implement in RPG | Skip |
|--------|---------------------------|-------------------|------------------|------|
| Skill XP & player levels | **AuraSkills** | Civs internal skills (discounts/building only) | Objectives, rewards, multipliers | — |
| Stats / combat attributes | **AuraSkills** `StatModifier` | Civs `StatManager` for territorial perks | Perk application via hooks | — |
| Player shops / merchant quests | **ChestShop** `TransactionEvent` | Fallback shop only if ChestShop absent | `ChestShopQuestListener`, YAML objectives | Native shop MVP (removed from Sprint 1) |
| Economy / payouts | **Vault** (+ Essentials provider) | Town bank, upkeep | `earn_money`, balance checks | — |
| Permissions / unlocks | **LuckPerms** | Civs town ranks | Quest/perk gates | — |
| Display | **PAPI** | `%civs_*%` | `%rpg_*%` | — |
| Quest lore / handouts | **InteractiveBooks** | — | Book grant on quest start/complete | — |
| Kits / warps (rewards) | **Essentials** | — | Reward executor, location objectives | — |
| Vein mining objectives | **VeinMiner** (optional event) | — | Optional `vein_mine` objective | — |
| Towns, regions, siege | — | **Civs** (core) | Event listeners, objectives | — |
| Turrets, town shields | — | **Civs** (KingdomX patterns) | Siege quest hooks | — |
| Custom mob YAML / bosses | — | **Civs** (MythicMobs patterns) | `CustomMobKillEvent` consumer | MythicMobs runtime |
| Auction house | — | **Civs** native BIN (AuctionHouse not on server) | `AuctionListEvent` / purchase objectives | AuctionHouse plugin |
| NPC merchants / waypoints | — | **Civs** light NPC (Citizens not on server) | Dialog quest stages (optional) | Citizens runtime |
| Heroes-style spells / classes | — | **Civs** spells + mana | Class archetype YAML only | Heroes runtime |
| Quest framework / stages | — | — | **RPG** `ObjectiveTypeRegistry` | BeautyQuests runtime |
| MOTD, sort, chunks, sit, prism, NBTAPI | — | — | — | **Skip** |

## Ownership

| Domain | Owner | RPG role |
|--------|-------|----------|
| Player skill XP, stats (Fighting, Mining…) | **AuraSkills** | Objectives, rewards, perk modifiers — never own XP ledger |
| Territorial skills, shop discounts, building XP | **Civs** | Listen `GainExpEvent` for Civs-internal objectives only |
| Towns, regions, siege, AH, custom mobs | **Civs** | Listen events for objectives |
| Quest progress, archetypes, perk gates | **RPG** | YAML state + LuckPerms |
| Money | **Vault** (Essentials provider) | Payouts, balance objectives |
| Player shops | **ChestShop** | Merchant quest objectives |

## Sprint 1 blockers (Civs) — DONE

- **CIVS-001** ✅ `GainExpEvent` fired from `Civilian.awardSkill()` / `addSkillXp()` (cancellable, before XP apply)
- **CIVS-002** ✅ Centralized `SkillListener` (mining, farming/crop, fishing, combat) + `fishing.yml` + translations; removed duplicated XP code from `DeathListener` / `ProtectionHandler`
- **CIVS-003** ✅ PAPI `%civs_skill_<name>_level%` / `_xp%` in `PlaceHook`
- **CIVS-004** ✅ Public `Civilian.addSkillXp()`; `Skill.addRawExp()`; bonus XP persisted as `skills.<name>._bonus-exp`
- ~~**CIVS-005** Native chest shop MVP~~ → **deferred / fallback only** — production uses **ChestShop**

## Sprint 1 (RPG) — DONE (RPG-009 deferred)

- **RPG-001** ✅ `ObjectiveTypeRegistry` — 9 objective parsers
- ~~**RPG-002** `CivsSkillHook` replaces `AuraSkillsHook`~~ → **cancelled** — kept `AuraSkillsHook`; `CivsInternalSkillListener` placeholder added
- **RPG-003** ✅ Objectives `kill_mob`, `mine_block`, `earn_money` + `build_region`, `skill_level`
- **RPG-004** ✅ Quest chain `requires:` (`QuestManager.meetsRequirements`)
- **RPG-005** ✅ Rewards: Vault + AuraSkills XP + LuckPerms node (`RewardExecutor`)
- **RPG-006** ✅ ChestShop `shop_buy` / `shop_sell` / `shop_revenue` via reflection `TransactionEvent`
- **RPG-007** ✅ `EssentialsHook` — `balance_min` objective, kit/warp rewards (config-gated)
- **RPG-008** ✅ `InteractiveBooksHook` — grant `lore-book` on quest start
- **RPG-009** ✅ `VeinMinerHook` — `vein_mine` objective via `PlayerVeinMineEvent`; `daily_miner.yml`; config default `enabled: true`

## Civs backlog (by priority)

**P0:** GainExpEvent (internal skills), skill listeners, StatManager, auction house BIN (no AuctionHouse on server)  
**P1:** Turrets, town shields, outposts, town ranks GUI, light NPC merchant (no Citizens)  
**P2:** Custom mob YAML DSL, boss phases, Heroes-inspired spells, class tiers  
**P3:** Claim map, peace treaties, bid auctions, NPC waypoints  
**Deferred:** Native chest shop (ChestShop on server)

## RPG backlog (by priority)

**P0:** Objective registry, 8+ objective types, requirements, rewards, AuraSkillsHook, ChestShopQuestListener  
**P1:** SkillTreeGui, DiscoveryService, HuntSpawnService, RebirthService, Civs light NPC  
**P2:** Daily/weekly rotation pools ✅ (YAML), Codex GUI, path traits, loot tables  
**P3:** In-game quest editor, Dynmap markers  

## Reference plugins — still native (not on server)

| Reference | Study for | Implement in |
|-----------|-----------|--------------|
| **KingdomX** | Turrets, shields, outposts, siege UI | Civs P1 |
| **MythicMobs** | Mob YAML DSL, skills, phases | Civs P2 + `CustomMobKillEvent` |
| **AuctionHouse** | BIN listing UX, fees | Civs P0 auction (no plugin on server) |
| **Citizens** | NPC merchants, waypoints | Civs P1 light NPC |
| **Heroes** | Class tiers, spell binding | Civs P2 spells |
| **BeautyQuests** | Stage types, branching | RPG `ObjectiveTypeRegistry` patterns only |

## License when borrowing

| Repo | License | Action |
|------|---------|--------|
| BeautyQuests, EzAuction | MIT | Adapt with attribution |
| AuraSkills, ChestShop, LMBishop Quests, Citizens | GPL | **Runtime softdepend OK** on server; reference code = patterns only |
| Heroes (khobbits) | CC-BY-ND | Design reference only |
| MythicMobs, KingdomX core | Closed | Wiki/spec + YAML structure |

## Pending product decision

**Resolved:** Option 2 — Civs tutorials (onboarding) + RPG quests (mid/endgame parallel). Unify Sprint 4+.

## Event contract (RPG consumes)

| Event | Source | Use |
|-------|--------|-----|
| `SkillLevelUpEvent`, `XpGainEvent` | **AuraSkills** | `skill_level` objectives, XP multipliers |
| `GainExpEvent` | Civs (wire P0) | Civs-internal skill objectives only |
| `RegionCreatedEvent` | Civs | `build_region` |
| `TownCreatedEvent`, `TownEvolveEvent` | Civs | territorial quests |
| `EnterCombatEvent` | Civs | warrior quests |
| `TransactionEvent` | **ChestShop** | merchant buy/sell objectives |
| `AuctionListEvent`, `AuctionPurchaseEvent` | Civs (new) | merchant quests (no AuctionHouse plugin) |
| `CustomMobKillEvent` | Civs (new) | boss quests (no MythicMobs) |
