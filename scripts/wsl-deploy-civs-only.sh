#!/usr/bin/env bash
# Deploy Civs JAR only — stop before scp, backup plugins, restart, verify boot.
set -euo pipefail
HOST="${MINESERVER_HOST:-daniel@bot-server}"
SERVER=/home/daniel/mineserver
PLUGINS="$SERVER/plugins"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
CIVS=/mnt/c/Users/Danie/Downloads/Civs-1.11.6/Civs-1.11.6/target/civs-1.11.6.jar
TS=$(date +%Y%m%d-%H%M)
BACKUP="$SERVER/plugins-backup-$TS"

test -f "$CIVS" || { echo "MISSING CIVS JAR — run mvn package in Civs first"; exit 1; }

echo "== stopping server =="
bash "$SCRIPT_DIR/wsl-restart-mineserver.sh" stop

echo "== backup =="
ssh -o BatchMode=yes "$HOST" "cp -a '$PLUGINS' '$BACKUP'"
echo "BACKUP=$BACKUP"

echo "== deploy Civs JAR =="
scp -o BatchMode=yes "$CIVS" "${HOST}:${PLUGINS}/civs-1.11.6.jar"

echo "== starting server =="
bash "$SCRIPT_DIR/wsl-restart-mineserver.sh" start

LOG="$SERVER/logs/latest.log"
for i in $(seq 1 90); do
  if ssh -o BatchMode=yes "$HOST" "grep -q 'Done (' '$LOG' 2>/dev/null"; then
    break
  fi
  sleep 3
done
sleep 5

echo "=== boot verification ==="
ssh -o BatchMode=yes "$HOST" "grep -iE 'Civs|BlueprintGenerator|Done \\(' '$LOG' | tail -25"
echo "=== errors (should be empty) ==="
ssh -o BatchMode=yes "$HOST" "grep -iE 'ERROR|ClassCastException|key must be lowercase|BlueprintGenerator' '$LOG' | grep -iE 'Civs|Blueprint|ClassCast' | tail -15 || true"
echo "DEPLOY_OK backup=$BACKUP"
