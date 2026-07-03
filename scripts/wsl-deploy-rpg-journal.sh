#!/usr/bin/env bash
set -euo pipefail

HOST=daniel@bot-server
SERVER=/home/daniel/mineserver
PLUGINS="$SERVER/plugins"
RPG=/mnt/c/Users/Danie/Downloads/Civs-1.11.6/rpg-server-plugin/target/rpg-server-0.1.0-SNAPSHOT.jar
TS=$(date +%Y%m%d-%H%M)
BACKUP="$SERVER/plugins-backup-$TS"

test -f "$RPG"

echo "== stopping server =="
ssh -o BatchMode=yes "$HOST" "tmux -L mc send-keys -t minecraft stop C-m || true"
for i in $(seq 1 60); do
  if ssh -o BatchMode=yes "$HOST" "pgrep -f $SERVER/server.jar >/dev/null"; then
    sleep 2
  else
    break
  fi
done
ssh -o BatchMode=yes "$HOST" "tmux -L mc kill-session -t minecraft 2>/dev/null || true"

echo "== backup =="
ssh -o BatchMode=yes "$HOST" "cp -a '$PLUGINS' '$BACKUP'"

echo "== scp rpg jar =="
scp -o BatchMode=yes "$RPG" "${HOST}:${PLUGINS}/"

echo "== starting server =="
ssh -o BatchMode=yes "$HOST" "cd '$SERVER' && tmux -L mc new-session -d -s minecraft /opt/java25/bin/java -Xms4G -Xmx4G -jar server.jar nogui"

echo "DEPLOY_DONE backup=$BACKUP"
