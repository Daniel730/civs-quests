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

## 8. Next when you return

- [ ] Playtest boss chain + `daily_quarry` in-game
- [ ] Grant new LP nodes if testing permissions
- [ ] Decide: deploy Civs smokeshow fixes + turret MVP (CIVS-009)?
- [ ] Approve git commits + PRs for both repos
- [ ] Optional: enable VeinMiner objective (`integrations.veinminer.enabled: true`) + sample quest
