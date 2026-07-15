#!/usr/bin/env bash
set -euo pipefail
HOST="daniel@bot-server"
SERVER=/home/daniel/mineserver
PLUGINS="$SERVER/plugins"
TS=$(date +%Y%m%d_%H%M%S)
BACKUP="/home/daniel/mineserver_cleanup_$TS"

echo "== quarantine backup: $BACKUP =="
ssh -o BatchMode=yes "$HOST" "mkdir -p '$BACKUP' && cp -a '$PLUGINS/civs-1.11.6.jar' '$PLUGINS/rpg-server-0.1.0-SNAPSHOT.jar' '$BACKUP/' 2>/dev/null; cp -a '$SERVER/plugins/Civs' '$BACKUP/Civs_before' 2>/dev/null; echo BACKUP_DONE"

echo "== deploying jars =="
scp -o BatchMode=yes \
  /mnt/c/Users/Danie/Downloads/Civs-1.11.6/Civs-1.11.6/target/civs-1.11.6.jar \
  /mnt/c/Users/Danie/Downloads/Civs-1.11.6/rpg-server-plugin/target/rpg-server-0.1.0-SNAPSHOT.jar \
  "${HOST}:${PLUGINS}/"

echo "BACKUP_PATH=$BACKUP"
echo "DEPLOY_JARS_DONE"
