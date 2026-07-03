# Final handoff — read on return

**Prepared:** 2026-07-03 (late night — post playtest fixes + clean deploy)  
**Live server:** `daniel@bot-server` → `/home/daniel/mineserver` (Paper 26.1.2, tmux `-L mc` session `minecraft`)

Single page for session context. Details: `SPRINT-3-STATUS.md`, `DEPLOY.md`, `GITHUB-SYNC.md`.

---

## 1. What is LIVE on the server right now

Server booted **~20:26 UTC 2026-07-03** — **clean** (no Civs/RPG errors after full restart deploy).

| Component | State |
|-----------|-------|
| RPGServer v0.1.0-SNAPSHOT | **22 quests**, **9 perks**, LuckPerms API connected |
| Civs 1.11.6 | 3 custom mobs (`bandit_chief`, `bandit_scout`, `wild_boar`), auction empty states |
| Hooks | Vault, Civs, AuraSkills, Essentials, InteractiveBooks, PAPI, **LuckPerms**, ChestShop, CustomMob, VeinMiner |
| Deploy rule | **Never hot-swap JARs** — use `scripts/wsl-deploy-bot-server.sh` (stop → backup → scp → start) |

Log proof:
```
Loaded 3 custom mob definition(s)
[RPGServer] LuckPerms API conectada.
[RPGServer] Carregadas 22 quests.
[RPGServer] Carregados 9 perks.
Done (112s)
```

Backup: `plugins-backup-20260703-2024`.

---

## 2. Playtest errors (fixed this session)

| Symptom | Cause | Fix |
|---------|-------|-----|
| Civs spam: `CivSkills`, `BuiltInCivState`, `MobSpawnFeedback` not found | JAR replaced while server running | Full restart deploy |
| RPG join: `StatOperation` NoClassDefFoundError | Same stale Civs classloader | Same |
| LuckPerms never connected | Paper `libraries:` loaded isolated LP API copy | Removed library loader; `ServicesManager` hook |
| `/cv mob spawn` failed | `MobSpawnFeedback` not in running classloader | Same restart fix |

---

## 3. What to TEST in-game (next session)

1. **Join** — perks apply without console errors; journal opens.
2. **Custom mobs** — `/cv mob spawn bandit_chief|bandit_scout|wild_boar` — spawn particles + combat works.
3. **Quest chain** — boss kill → capstone LP groups (`rpg-warrior`, etc.).
4. **Auction** — empty browse + my-listings show hint lore.
5. **Farm GUI** — deposit/withdraw (prior sprint fix; re-verify).

---

## 4. Deploy commands (WSL)

```bash
# Build (Windows Maven paths via WSL mount)
mvn -f ../Civs-1.11.6/pom.xml package -DskipTests
mvn -f pom.xml package

# Full deploy (both JARs + quests + books)
bash scripts/wsl-deploy-bot-server.sh

# Civs-only (still requires restart after scp)
bash scripts/deploy-civs-only.sh && bash scripts/wsl-restart-mineserver.sh restart
```

---

## 5. GitHub / sprint

- Branches: Civs `sprint-3/civs-features`, RPG `sprint-3/rpg-features`
- Open: CIVS-009 turrets/shields, RPG-009 VeinMiner (deferred)
- Sync: `GITHUB-SYNC.md` after each deploy batch

---

## 6. Recommended next (Sprint 3 polish)

1. CIVS-009 turret MVP deploy + town shields
2. More custom mob variety + region spawn weights
3. Quest balance from playtest notes (daily_farm, weekly_boss_hunter)
4. VeinMiner objective (RPG-009) when ready
