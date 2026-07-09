# Deploy — bot-server (mineserver)

Production Minecraft on **daniel@bot-server** (`bot-server`). Use **WSL** for SSH/SCP — keys live in the WSL user (`~/.ssh/id_ed25519`), not Windows OpenSSH.

## Instance choice

| Path | Role |
|------|------|
| `/home/daniel/mineserver` | **Production** (Paper 26.1.2, tmux) |
| `/home/daniel/mineserver_old` | Retired |
| `/home/daniel/mineserver_cleanup_*` | Cleanup snapshot |
| `/home/daniel/odysseus` | Separate stack (not Civs/RPG target) |

`docker-compose.yml` in mineserver is legacy/unused for the live process. The running server is **native Java** (target: **tmux** socket `-L mc`, session `minecraft`), not Docker.

If `tmux -L mc list-sessions` is empty but `pgrep matching ``^/opt/java25/bin/java .+-jar server\.jar``` shows a process, the server was started outside tmux (deploy stop via tmux alone will not work). Use `scripts/wsl-restart-mineserver.sh` for stop/start/restart.

## Build (Windows)

**Order:** Civs first, then RPG (RPG `pom.xml` uses Civs JAR as system dependency).

| Repo | Path | Branch | Command |
|------|------|--------|---------|
| Civs Custom | `C:\Users\Danie\Downloads\Civs-1.11.6\Civs-1.11.6` | `sprint-2/civs-polish` (or current dev) | `mvn package` |
| RPG Server | `C:\Users\Danie\Downloads\Civs-1.11.6\rpg-server-plugin` | `master` | `mvn package` |

Maven: `C:\Users\Danie\tools\apache-maven-3.9.10\bin\mvn.cmd`

**JARs to deploy:**

| Plugin | Local path (WSL) |
|--------|------------------|
| Civs | `/mnt/c/Users/Danie/Downloads/Civs-1.11.6/Civs-1.11.6/target/civs-1.11.6.jar` |
| RPG | `/mnt/c/Users/Danie/Downloads/Civs-1.11.6/rpg-server-plugin/target/rpg-server-0.1.0-SNAPSHOT.jar` |

**Remote plugins dir:** `/home/daniel/mineserver/plugins/`

Filenames on server: `civs-1.11.6.jar`, `rpg-server-0.1.0-SNAPSHOT.jar`

## Runtime on server

| Item | Value |
|------|--------|
| Server root | `/home/daniel/mineserver` |
| Java | `/opt/java25/bin/java` |
| Heap | `-Xms4G -Xmx4G` |
| JAR | `server.jar` (Paper 26.1.2) |
| Process manager | **tmux** socket `-L mc`, session `minecraft` |
| Attach console | `tmux -L mc attach -t minecraft` (detach: `Ctrl+b` then `d`) |

### Restart control (preferred)

From WSL (handles tmux **or** bare Java, RCON graceful stop when tmux is absent):

```bash
wsl bash /mnt/c/Users/Danie/Downloads/Civs-1.11.6/rpg-server-plugin/scripts/wsl-restart-mineserver.sh restart
wsl bash .../scripts/wsl-restart-mineserver.sh stop
wsl bash .../scripts/wsl-restart-mineserver.sh start
```

Remote logic lives in `scripts/mineserver-control-remote.sh` (piped over SSH). Stop order:

1. If `tmux -L mc` session `minecraft` exists → `stop` via console (`send-keys`).
2. Else if `enable-rcon=true` in `server.properties` → RCON `stop` (`mcrcon` or built-in Python client; password read on server only).
3. Else → `SIGTERM`, wait, then `SIGKILL`.

Start always uses:

```bash
cd /home/daniel/mineserver
tmux -L mc new-session -d -s minecraft \
  /opt/java25/bin/java -Xms4G -Xmx4G -jar server.jar nogui
```

Process detection uses Java cmdline `^/opt/java25/bin/java .+-jar server\.jar` and cwd `/home/daniel/mineserver` (not `pgrep -f .../server.jar`, which often misses bare Java).

### Stop / start (manual on server)

Same as the remote script; on the host you can run:

```bash
bash /path/to/mineserver-control-remote.sh stop   # or start | restart
```

## Backup

Before replacing JARs, copy the whole plugins folder:

```bash
TS=$(date +%Y%m%d-%H%M)
cp -a /home/daniel/mineserver/plugins "/home/daniel/mineserver/plugins-backup-${TS}"
```

Run backup **inside WSL bash** (not PowerShell-wrapped SSH) so `$(date …)` and `$TS` are not stripped.

## Deploy steps (manual)

1. `wsl ssh -o BatchMode=yes daniel@bot-server echo ok` — must print `ok`
2. Build both JARs if missing or stale
3. `wsl bash .../scripts/wsl-restart-mineserver.sh stop`
4. Timestamped `plugins-backup-*`
5. From WSL: `scp` both JARs to `daniel@bot-server:/home/daniel/mineserver/plugins/`
6. `wsl bash .../scripts/wsl-restart-mineserver.sh start`
7. Verify (checklist below)

## Verification checklist

- [ ] `pgrep -af '^/opt/java25/bin/java'` and cwd `/home/daniel/mineserver` (`readlink -f /proc/<pid>/cwd`)
- [ ] `tmux -L mc list-sessions` shows `minecraft`
- [ ] `logs/latest.log` lists **Civs (1.11.6)** and **RPGServer (0.1.0-SNAPSHOT)** in the 19-plugin set
- [ ] Log contains `[RPGServer] Loading server plugin` and Civs enable line after world load
- [ ] In-game: `/rpg quest list` (optional smoke test)
- [ ] After quest sync deploy: `/rpg sync <player>` or enable `progression.sync-on-join-from-civs`
- [ ] **Sync GitHub issues (both repos)** — close deployed tickets, comment partial work; see [GITHUB-SYNC.md](GITHUB-SYNC.md)

## Quest profile sync

Admin command `/rpg sync [player]` backfills RPG YAML from Civs/AuraSkills/Vault (step state only; no skill XP).
Bulk offline: `scripts/bulk-sync-quest-profiles.py` on server. See [rpg-quests SKILL](../rpg-quests/SKILL.md).

Config flag `progression.sync-on-join-from-civs: true` in `plugins/RPGServer/config.yml` runs silent sync on every join (no rewards, no chat). Safe after smokeshow validation — enable via:

```bash
ssh daniel@bot-server "sed -i 's/sync-on-join-from-civs: false/sync-on-join-from-civs: true/' /home/daniel/mineserver/plugins/RPGServer/config.yml"
# or edit manually; /rpg reload picks up config changes
```

## Sprint 3 weekly — LuckPerms test permissions

Grant path-completion nodes before weekly quests unlock (`requires:` chain). Prefix defaults to `rpg.quest.` (`integrations.luckperms.quest-permission-prefix`).

| Quest ID | Archetype | Requires quest | LP node to grant |
|----------|-----------|----------------|------------------|
| `weekly_warrior` | warrior | `warrior_path` | `rpg.quest.weekly_warrior` |
| `weekly_merchant` | merchant | `merchant_path` | `rpg.quest.weekly_merchant` |
| `weekly_builder` | builder | `builder_path` | `rpg.quest.weekly_builder` |

**Prerequisite path nodes** (complete starter path or `/rpg sync` after Civs tutorial):

| Path quest | LP node |
|------------|---------|
| `warrior_path` | `rpg.quest.warrior_path` |
| `merchant_path` | `rpg.quest.merchant_path` |
| `builder_path` | `rpg.quest.builder_path` |

**Example — unlock weekly warrior for smokeshow:**

```bash
# LuckPerms console or /lp user <name> permission set ...
lp user smokeshow permission set rpg.quest.warrior_path true
lp user smokeshow permission set rpg.quest.weekly_warrior true
```

**In-game checks:** `/rpg reload` → 11 quests (8 base + 3 weekly + sprint examples if copied). `/rpg journal` shows **Semanal** tag. Weekly quests reset on calendar week rollover (`quests.reset-timezone`, default UTC).

Copy weekly YAMLs to server if `plugins/RPGServer/quests/` already existed before deploy:

```bash
scp src/main/resources/quests/weekly_*.yml daniel@bot-server:/home/daniel/mineserver/plugins/RPGServer/quests/
```

## One-liner template (WSL bash)

Save as `scripts/wsl-deploy-bot-server.sh` and run: `wsl bash /mnt/c/Users/Danie/Downloads/Civs-1.11.6/rpg-server-plugin/scripts/wsl-deploy-bot-server.sh`

```bash
#!/usr/bin/env bash
set -euo pipefail
HOST=daniel@bot-server
SERVER=/home/daniel/mineserver
PLUGINS="$SERVER/plugins"
CIVS=/mnt/c/Users/Danie/Downloads/Civs-1.11.6/Civs-1.11.6/target/civs-1.11.6.jar
RPG=/mnt/c/Users/Danie/Downloads/Civs-1.11.6/rpg-server-plugin/target/rpg-server-0.1.0-SNAPSHOT.jar
TS=$(date +%Y%m%d-%H%M)
BACKUP="$SERVER/plugins-backup-$TS"

test -f "$CIVS" && test -f "$RPG"
SCRIPT=/mnt/c/Users/Danie/Downloads/Civs-1.11.6/rpg-server-plugin/scripts/wsl-restart-mineserver.sh
bash "$SCRIPT" stop
ssh -o BatchMode=yes "$HOST" "cp -a '$PLUGINS' '$BACKUP'"
scp -o BatchMode=yes "$CIVS" "$RPG" "${HOST}:${PLUGINS}/"
bash "$SCRIPT" start
echo "Deployed. Backup: $BACKUP"
```

## SSH prerequisites (WSL)

```bash
wsl ls -la ~/.ssh/
wsl ssh -o BatchMode=yes daniel@bot-server echo ok
```

If SSH fails:

1. Generate key in WSL: `ssh-keygen -t ed25519`
2. Install pubkey: `ssh-copy-id daniel@bot-server`
3. Ensure `bot-server` resolves (SSH config `Host bot-server` → real hostname/IP)
4. Do **not** rely on Windows `ssh` unless the same key is configured there

## Last deploy (2026-07-03)

- **Result:** Success
- **Target:** `/home/daniel/mineserver`
- **JARs:** `civs-1.11.6.jar` (2340722 bytes), `rpg-server-0.1.0-SNAPSHOT.jar` (96018 bytes)
- **Backup:** `/home/daniel/mineserver/plugins-backup-` (use timestamped backup on next run via WSL bash script)
- **Verified:** Java/tmux up; log shows 19 plugins including Civs + RPGServer
- **Note (2026-07-03):** Added `wsl-restart-mineserver.sh` for RCON/tmux-aware restart when Java runs outside tmux


## Troubleshooting: ChestShop / Vault errors after Civs deploy

**Symptom:** `Error occurred while enabling ChestShop` with `NoClassDefFoundError: net/milkbowl/vault/economy/Economy`, or Civs/RPG fail with missing Vault classes. Logs may look like a Vault **load-order** problem.

**Actual cause:** Vault enabled correctly, then the server began **shutting down while plugins were still enabling** (race). Example sequence in `logs/`:

1. `[Vault] Enabled`
2. `[Server console handler/ERROR] ... IOError` (tmux stdin closed)
3. `[Thread-0/INFO]: Stopping server`
4. `[Vault] Disabling Vault` **before** `[ChestShop] Enabling`
5. ChestShop/Civs enable fails because Vault is already disabled

This happens when **restart/stop runs twice** before the first boot finishes (`Done (...)!`), or when `deploy-civs-only.sh` used the old `pgrep -f .../server.jar` wait (Java cmdline is `-jar server.jar` only, so the wait returned immediately and `tmux kill-session` could interrupt startup).

**Fix:**

- Use `scripts/wsl-restart-mineserver.sh stop` / `start` (or `restart`) only — it waits on the real Java PID in `/home/daniel/mineserver`.
- After Civs-only deploy, run **one** `wsl bash .../wsl-restart-mineserver.sh start` and wait for `Done` in `logs/latest.log` before any second restart.
- No `plugins.yml` or Paper load-order change required; Vault already loads before ChestShop/Civs on a clean boot.

**Verify:** `grep -E 'ChestShop.*Enabling|Vault loaded|ERROR.*ChestShop' logs/latest.log` should show enable + `Vault loaded!` with no ERROR lines.


## Public Minecraft access (DuckDNS)

| Item | Value |
|------|--------|
| LAN IP (port forward target) | **192.168.1.252** (not 192.168.1.50 — see below) |
| Minecraft port | **25565** (server-port in server.properties) |
| Client address | **forever-server.duckdns.org** (no :25565 needed on default port) |
| Router (CHITA-Hub5) | TCP **25565** WAN → **192.168.1.252:25565** |

**Why 192.168.1.50 appeared:** bot-server can show both .50 (old primary NM address) and .252 (reserved target) on enp4s0; hostname -I lists .50 first. Forward the router to **.252**.

Network fix on the host (gateway, static .252, DuckDNS refresh): copy or use scripts/fix-minecraft-network.sh → ~/fix-minecraft-network.sh, then **sudo ~/fix-minecraft-network.sh** (password required). Token: /etc/duckdns.token.

From outside LAN, verify DNS and port: Resolve-DnsName forever-server.duckdns.org; Test-NetConnection forever-server.duckdns.org -Port 25565. On the same Wi-Fi, DuckDNS may fail without router NAT hairpin — use LAN IP or mobile data to test.
