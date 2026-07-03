# Final handoff — read on return

**Prepared:** 2026-07-03 (evening — post CIVS-010 + 14-quest deploy)
**Live server:** `daniel@bot-server` → `/home/daniel/mineserver` (Paper 26.1.2, tmux `-L mc` session `minecraft`)

This is the single page to read when you return. Details live in `SPRINT-1/2/3-STATUS.md`, `DEPLOY.md`, `FEATURE-EXTRACTION.md`.

---

## 1. What is LIVE on the server right now

Server booted **17:30 UTC 2026-07-03** — **clean** (no errors/exceptions; only pre-existing offline-mode + VeinMiner "Hand" WARNs).

| Component | State |
|-----------|-------|
| RPGServer v0.1.0-SNAPSHOT | **14 quests**, 2 perks, custom mob hook active |
| Civs 1.11.6 | Custom mobs enabled (`bandit_chief`), CIVS-010 MVP |
| Config | `sync-on-join-from-civs: true` (user setting preserved); quest notification messages merged |
| Hooks | Vault, Civs, AuraSkills, Essentials, InteractiveBooks, PAPI, LuckPerms, ChestShop, CustomMob |

Log proof:
```
Loaded 1 custom mob definition(s)
[RPGServer] Carregadas 14 quests.
[RPGServer] Civs custom mob hook ativo (CustomMobKillEvent via reflexão).
```

Backup before this deploy: `plugins-backup-20260703-1729`.

---

## 2. What is done LOCALLY but NOT committed

All uncommitted. Nothing was git-committed this session (per your rule).

| Area | State | Repo/branch |
|------|-------|-------------|
| Sprint 2 Civs smokeshow fixes | local, not redeployed | Civs `sprint-2/civs-polish` |
| Sprint 2 RPG (RPG-010–015) | local + deployed | RPG `master` |
| Sprint 3 RPG-016 daily/weekly + `/rpg sync` | local + deployed | RPG `master` |
| CIVS-010 custom mobs + RPG boss chain | local + deployed | Both repos |
| Config bossbar YAML fix + merge script | local + deployed to server | RPG `master` |
| CIVS-009 turret MVP | local only, **not deployed** | Civs `sprint-2/civs-polish` |

---

## 3. What to TEST in-game

**Boss chain (new):**
1. Complete `warrior_path` (or `/rpg sync` + grant LP).
2. `/cv mob spawn bandit_chief` (op/admin).
3. Kill boss → `bandit_chief_slayer` completes; rewards + LP `rpg.quest.bandit_chief_slayer`.
4. `warrior_champion` unlocks in journal → kill 20 zombies + 10 spells + Fighting 10.

**Daily/weekly (unchanged):**
- `daily_hunter` (5 zombies), `daily_quarry` (64 stone, builder).
- Weekly archetype quests with **Semanal** tag.

**Notifications (config merged):**
- Quest objective/complete action-bar + bossbar should appear (no restart needed; `/rpg reload` if unsure).

**Sync:**
- `/rpg sync <player>` — backfills step state, no skill XP grant.

---

## 4. LuckPerms nodes (new this session)

```
rpg.quest.bandit_chief_slayer
rpg.quest.warrior_champion
```

Full list in `SPRINT-3-STATUS.md` → "All quest reward permissions".

---

## 5. What needs COMMIT PERMISSION

When you approve:

- **RPG** (`Daniel730/civs-quests`, `master`): Sprint 2 + Sprint 3 as logical commits, then PR.
- **Civs** (`Daniel730/Civs`, `sprint-2/civs-polish`): CIVS-006–010 + smokeshow fixes, then PR.

GitHub: [CIVS-010 closed](https://github.com/Daniel730/Civs/issues/10). Open: [CIVS-009](https://github.com/Daniel730/Civs/issues/9) (turrets partial), [RPG-009](https://github.com/Daniel730/civs-quests/issues/9) (VeinMiner deferred).

---

## 6. Deploy commands (WSL)

Build (Windows):
```powershell
& "C:\Users\Danie\tools\apache-maven-3.9.10\bin\mvn.cmd" -f "..\Civs-1.11.6\pom.xml" package
& "C:\Users\Danie\tools\apache-maven-3.9.10\bin\mvn.cmd" package
```

Verify logs:
```bash
wsl ssh -o BatchMode=yes daniel@bot-server "grep -E 'Carregadas|custom mob' /home/daniel/mineserver/logs/latest.log | tail"
wsl ssh -o BatchMode=yes daniel@bot-server "grep -iE 'error|exception|severe' /home/daniel/mineserver/logs/latest.log | grep -ivE 'offline mode|VeinMiner.*Hand' || echo CLEAN"
```

Config merge (add missing keys only):
```bash
wsl scp .../scripts/merge-server-rpg-config.py .../config.yml daniel@bot-server:/tmp/
wsl ssh daniel@bot-server "python3 /tmp/merge-server-rpg-config.py /home/daniel/mineserver/plugins/RPGServer/config.yml /tmp/config.yml"
```

---

## 7. Stuck agent resolved

Subagent `161fabed` (2026-07-03 15:35) was tasked to merge missing `messages.*` keys into server `config.yml` but **never started** (user message only, 0 assistant turns). Completed this session via `merge-server-rpg-config.py` — preserved `sync-on-join-from-civs: true`.

---

## 8. Player journey map (Sprint 3 polish)

```
                    ┌─────────────────────────────────────┐
                    │  LOGIN — Civs tutorial (parallel)   │
                    │  RPG: "Escolha seu caminho" (journal)│
                    └──────────────┬──────────────────────┘
                                   │
          ┌────────────────────────┼────────────────────────┐
          ▼                        ▼                        ▼
   warrior_path            merchant_path             builder_path
   (+ warrior_intro)        (+ merchant_intro)        (+ builder_intro)
          │                        │                        │
          ▼                        ▼                        ▼
   sprint2_spells           sprint2_auction           sprint2_civs_skills
   Iniciação às Magias      Primeiro Leilão           Expansão Territorial
          │                        │                        │
          ▼                        ▼                        ▼
   bandit_chief_slayer      mercador_fortuna          construtor_mestre
   (+ boss_guide)            Fortuna do Mercador       Mestre Construtor
          │
          ▼
   warrior_champion
   Campeão Guerreiro (capstone)

   ════════════ DAILY / WEEKLY (parallel) ════════════
   daily_hunter · daily_quarry · daily_mercado · daily_vendas · daily_miner
   weekly_warrior · weekly_merchant · weekly_builder
```

**Daily CTA on login:** title *"Missão Diária disponível!"* once per day when a daily is pending.

---

## 9. Smokeshow test path — returning user (ideal)

Use profile **smokeshow** (or reset RPG YAML + `/rpg sync`):

| Step | Action | Expected |
|------|--------|----------|
| 1 | Join server | Welcome title + daily CTA if dailies pending |
| 2 | `/rpg journal` | Section **Escolha seu caminho** (no archetype) or **Seu Caminho** |
| 3 | Accept `warrior_path` | InteractiveBooks `warrior_intro` + quest book granted |
| 4 | `/rpg sync` (if Civs progress exists) | Objectives backfill, no skill XP grant |
| 5 | Complete path → accept `sprint2_spells` | Journal shows **Próximo:** chain hint |
| 6 | Complete spells → `bandit_chief_slayer` | `boss_guide` lore book on accept |
| 7 | `/cv mob spawn bandit_chief` → kill | Quest completes, `warrior_champion` unlocks |
| 8 | Next login (new day) | **Missão Diária disponível!** title once |
| 9 | Complete `daily_hunter` | Diária tag, resets next calendar day |

LP nodes: grant `rpg.quest.warrior_path` after path complete (automatic on reward) or `/lp user <name> permission set rpg.quest.<id> true` for testing locked quests.

---

## 10. Next when you return

- [ ] Playtest boss chain + `daily_quarry` in-game
- [ ] Grant new LP nodes if testing permissions
- [ ] Decide: deploy Civs smokeshow fixes + turret MVP (CIVS-009)?
- [ ] Approve git commits + PRs for both repos
- [ ] Optional: enable VeinMiner objective (`integrations.veinminer.enabled: true`) + sample quest
