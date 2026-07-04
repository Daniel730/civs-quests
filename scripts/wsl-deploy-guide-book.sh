#!/usr/bin/env bash
set -euo pipefail
HOST=daniel@bot-server
SERVER=/home/daniel/mineserver
PLUGINS="$SERVER/plugins"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
RPG=/mnt/c/Users/Danie/Downloads/Civs-1.11.6/rpg-server-plugin/target/rpg-server-0.1.0-SNAPSHOT.jar
QUESTS=/mnt/c/Users/Danie/Downloads/Civs-1.11.6/rpg-server-plugin/src/main/resources/quests
CONFIG=/mnt/c/Users/Danie/Downloads/Civs-1.11.6/rpg-server-plugin/src/main/resources/config.yml
TS=$(date +%Y%m%d-%H%M)
BACKUP="$SERVER/plugins-backup-$TS"

test -f "$RPG"

echo "== stopping server =="
ssh -o BatchMode=yes "$HOST" 'bash -s' -- stop < "$SCRIPT_DIR/mineserver-control-remote.sh"

echo "== backup =="
ssh -o BatchMode=yes "$HOST" "cp -a '$PLUGINS' '$BACKUP'"
echo "BACKUP=$BACKUP"

echo "== deploy RPG + quests + config =="
scp -o BatchMode=yes "$RPG" "${HOST}:${PLUGINS}/rpg-server-0.1.0-SNAPSHOT.jar"
ssh -o BatchMode=yes "$HOST" "mkdir -p '$PLUGINS/RPGServer/quests'"
scp -o BatchMode=yes "$QUESTS"/*.yml "${HOST}:${PLUGINS}/RPGServer/quests/"
scp -o BatchMode=yes "$CONFIG" "${HOST}:${PLUGINS}/RPGServer/config.yml"

echo "== starting server =="
ssh -o BatchMode=yes "$HOST" 'bash -s' -- start < "$SCRIPT_DIR/mineserver-control-remote.sh"

LOG="$SERVER/logs/latest.log"
for i in $(seq 1 90); do
  if ssh -o BatchMode=yes "$HOST" "grep -q 'Done (' '$LOG' 2>/dev/null"; then
    break
  fi
  sleep 3
done
sleep 5

echo "=== boot verification ==="
ssh -o BatchMode=yes "$HOST" "grep -iE 'RPGServer|Carregadas|Done \\(' '$LOG' | tail -20"
