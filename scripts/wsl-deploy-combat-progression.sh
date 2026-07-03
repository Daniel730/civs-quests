#!/usr/bin/env bash
set -euo pipefail
HOST=daniel@bot-server
SERVER=/home/daniel/mineserver
PLUGINS="$SERVER/plugins"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
CIVS=/mnt/c/Users/Danie/Downloads/Civs-1.11.6/Civs-1.11.6/target/civs-1.11.6.jar
RPG=/mnt/c/Users/Danie/Downloads/Civs-1.11.6/rpg-server-plugin/target/rpg-server-0.1.0-SNAPSHOT.jar
QUESTS=/mnt/c/Users/Danie/Downloads/Civs-1.11.6/rpg-server-plugin/src/main/resources/quests
PERKS=/mnt/c/Users/Danie/Downloads/Civs-1.11.6/rpg-server-plugin/src/main/resources/perks
CONFIG=/mnt/c/Users/Danie/Downloads/Civs-1.11.6/rpg-server-plugin/src/main/resources/config.yml
MERGE="$SCRIPT_DIR/merge-server-rpg-config.py"
TS=$(date +%Y%m%d-%H%M)
BACKUP="$SERVER/plugins-backup-$TS"

test -f "$RPG"
test -f "$CIVS"

ssh -o BatchMode=yes "$HOST" "cp -a '$PLUGINS' '$BACKUP'"
echo "BACKUP=$BACKUP"

scp -o BatchMode=yes "$CIVS" "${HOST}:${PLUGINS}/civs-1.11.6.jar"
scp -o BatchMode=yes "$RPG" "${HOST}:${PLUGINS}/rpg-server-0.1.0-SNAPSHOT.jar"

ssh -o BatchMode=yes "$HOST" "mkdir -p '$PLUGINS/RPGServer/quests' '$PLUGINS/RPGServer/perks'"
scp -o BatchMode=yes "$QUESTS"/*.yml "${HOST}:${PLUGINS}/RPGServer/quests/"
scp -o BatchMode=yes "$PERKS"/*.yml "${HOST}:${PLUGINS}/RPGServer/perks/"

scp -o BatchMode=yes "$CONFIG" "$MERGE" "${HOST}:/tmp/"
ssh -o BatchMode=yes "$HOST" "python3 /tmp/merge-server-rpg-config.py '$PLUGINS/RPGServer/config.yml' /tmp/config.yml"

sed 's/\r$//' "$SCRIPT_DIR/mineserver-control-remote.sh" | ssh -o BatchMode=yes "$HOST" "bash -s start"

LOG="$SERVER/logs/latest.log"
for i in $(seq 1 90); do
  if ssh -o BatchMode=yes "$HOST" "grep -q 'Done (' '$LOG' 2>/dev/null"; then break; fi
  sleep 3
done
sleep 8

echo "=== verification ==="
ssh -o BatchMode=yes "$HOST" "grep -iE 'Carregadas|Carregados|RPGServer habilitado|Done \\(' '$LOG' | tail -15"
ssh -o BatchMode=yes "$HOST" "grep -A3 reward-multipliers '$PLUGINS/RPGServer/config.yml'"
echo "DEPLOY_OK backup=$BACKUP"
