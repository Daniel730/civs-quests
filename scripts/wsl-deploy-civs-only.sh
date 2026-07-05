#!/usr/bin/env bash
# Civs JAR only — stop, backup, scp, start.
set -euo pipefail
HOST="${MINESERVER_HOST:-daniel@bot-server}"
SERVER=/home/daniel/mineserver
PLUGINS="$SERVER/plugins"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
CIVS=/mnt/c/Users/Danie/Downloads/Civs-1.11.6/Civs-1.11.6/target/civs-1.11.6.jar
TS=$(date +%Y%m%d-%H%M)
BACKUP="$SERVER/plugins-backup-civs-$TS"

test -f "$CIVS" || { echo "MISSING CIVS JAR"; exit 1; }

echo "== stopping server =="
ssh -o BatchMode=yes "$HOST" 'bash -s' -- stop < "$SCRIPT_DIR/mineserver-control-remote.sh"

echo "== backup =="
ssh -o BatchMode=yes "$HOST" "cp -a '$PLUGINS' '$BACKUP'"
echo "BACKUP=$BACKUP"

echo "== deploy Civs JAR =="
scp -o BatchMode=yes "$CIVS" "${HOST}:${PLUGINS}/civs-1.11.6.jar"

echo "== starting server =="
ssh -o BatchMode=yes "$HOST" 'bash -s' -- start < "$SCRIPT_DIR/mineserver-control-remote.sh"

LOG="$SERVER/logs/latest.log"
for i in $(seq 1 60); do
  if ssh -o BatchMode=yes "$HOST" "grep -q 'Done (' '$LOG' 2>/dev/null"; then
    break
  fi
  sleep 3
done
sleep 5

echo "=== boot verification ==="
ssh -o BatchMode=yes "$HOST" "grep -iE 'Loaded 7 custom mob|Civs.*Enabling|Done \\(|ERROR.*Civs' '$LOG' | tail -10"
echo "DEPLOY_CIVS_OK backup=$BACKUP"
