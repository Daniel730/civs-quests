#!/usr/bin/env bash
# Full deploy: Civs + RPG JARs, quest YAMLs, books — always stop before scp (no hot-swap).
set -euo pipefail
HOST="${MINESERVER_HOST:-daniel@bot-server}"
SERVER=/home/daniel/mineserver
PLUGINS="$SERVER/plugins"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
CIVS=/mnt/c/Users/Danie/Downloads/Civs-1.11.6/Civs-1.11.6/target/civs-1.11.7.jar
RPG=/mnt/c/Users/Danie/Downloads/Civs-1.11.6/rpg-server-plugin/target/rpg-server-0.1.2.jar
QUESTS=/mnt/c/Users/Danie/Downloads/Civs-1.11.6/rpg-server-plugin/src/main/resources/quests
BOOKS=/mnt/c/Users/Danie/Downloads/Civs-1.11.6/rpg-server-plugin/src/main/resources/books
IB_BOOKS="$PLUGINS/InteractiveBooks/books"
TS=$(date +%Y%m%d-%H%M)
BACKUP="$SERVER/plugins-backup-$TS"

test -f "$CIVS" || { echo "MISSING CIVS JAR — run mvn package in Civs first"; exit 1; }
test -f "$RPG" || { echo "MISSING RPG JAR — run mvn package in RPG first"; exit 1; }

echo "== stopping server =="
ssh -o BatchMode=yes "$HOST" 'bash -s' -- stop < "$SCRIPT_DIR/mineserver-control-remote.sh"

echo "== backup =="
ssh -o BatchMode=yes "$HOST" "cp -a '$PLUGINS' '$BACKUP'"
echo "BACKUP=$BACKUP"

echo "== deploy JARs =="
# Remove older versioned jars so Paper does not double-load
ssh -o BatchMode=yes "$HOST" "rm -f '$PLUGINS'/civs-*.jar '$PLUGINS'/rpg-server-*.jar"
scp -o BatchMode=yes "$CIVS" "${HOST}:${PLUGINS}/civs-1.11.7.jar"
scp -o BatchMode=yes "$RPG" "${HOST}:${PLUGINS}/rpg-server-0.1.2.jar"

echo "== deploy quests + books =="
ssh -o BatchMode=yes "$HOST" "mkdir -p '$PLUGINS/RPGServer/quests' '$IB_BOOKS'"
scp -o BatchMode=yes "$QUESTS"/*.yml "${HOST}:${PLUGINS}/RPGServer/quests/"
if compgen -G "$BOOKS/*.yml" > /dev/null; then
  scp -o BatchMode=yes "$BOOKS"/*.yml "${HOST}:${IB_BOOKS}/"
fi

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
ssh -o BatchMode=yes "$HOST" "grep -iE 'LuckPerms API|Carregadas|Carregados|custom mob|RPGServer habilitado|Done \\(' '$LOG' | tail -20"
echo "=== errors (should be empty) ==="
ssh -o BatchMode=yes "$HOST" "grep -E 'ERROR|ClassNotFoundException|NoClassDefFoundError' '$LOG' | grep -E 'Civs|RPGServer' | tail -10 || true"
echo "DEPLOY_OK backup=$BACKUP"
