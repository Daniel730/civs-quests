#!/usr/bin/env bash
set -euo pipefail
HOST="${MINESERVER_HOST:-daniel@bot-server}"
SERVER=/home/daniel/mineserver
PLUGINS="$SERVER/plugins"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
CIVS=/mnt/c/Users/Danie/Downloads/Civs-1.11.6/Civs-1.11.6/target/civs-1.11.6.jar

test -f "$CIVS" || { echo "MISSING CIVS JAR"; exit 1; }
echo "== local jar =="
ls -l "$CIVS"

echo "== stopping server (mineserver-control-remote.sh) =="
ssh -o BatchMode=yes "$HOST" 'bash -s' -- stop < "$SCRIPT_DIR/mineserver-control-remote.sh"

echo "== backup =="
BACKUP=$(ssh -o BatchMode=yes "$HOST" 'TS=$(date +%Y%m%d-%H%M); cp -a /home/daniel/mineserver/plugins /home/daniel/mineserver/plugins-backup-$TS && echo /home/daniel/mineserver/plugins-backup-$TS')
echo "BACKUP=$BACKUP"

echo "== scp civs jar =="
scp -o BatchMode=yes "$CIVS" "${HOST}:${PLUGINS}/civs-1.11.6.jar"
ssh -o BatchMode=yes "$HOST" "ls -l ${PLUGINS}/civs-1.11.6.jar ${PLUGINS}/rpg-server-0.1.0-SNAPSHOT.jar"

echo "DONE_BACKUP=$BACKUP"
echo "== start with: wsl bash $SCRIPT_DIR/wsl-restart-mineserver.sh start =="
