#!/usr/bin/env bash
set -euo pipefail
HOST=daniel@bot-server
SERVER=/home/daniel/mineserver
PLUGINS="$SERVER/plugins"
RPG=/mnt/c/Users/Danie/Downloads/Civs-1.11.6/rpg-server-plugin/target/rpg-server-0.1.0-SNAPSHOT.jar
TS=$(date +%Y%m%d-%H%M)
BACKUP="$SERVER/plugins-backup-$TS"

test -f "$RPG"
ssh -o BatchMode=yes "$HOST" echo ok

ssh -o BatchMode=yes "$HOST" "tmux -L mc send-keys -t minecraft stop C-m" || true
for i in $(seq 1 60); do
  if ! ssh -o BatchMode=yes "$HOST" "pgrep -f $SERVER/server.jar >/dev/null"; then break; fi
  sleep 2
done
ssh -o BatchMode=yes "$HOST" "tmux -L mc kill-session -t minecraft 2>/dev/null || true"

ssh -o BatchMode=yes "$HOST" "cp -a '$PLUGINS' '$BACKUP'"
echo "BACKUP=$BACKUP"

scp -o BatchMode=yes "$RPG" "${HOST}:${PLUGINS}/"

ssh -o BatchMode=yes "$HOST" "cd '$SERVER' && tmux -L mc new-session -d -s minecraft /opt/java25/bin/java -Xms4G -Xmx4G -jar server.jar nogui"

for i in $(seq 1 90); do
  if ssh -o BatchMode=yes "$HOST" "grep -q 'Done (' '$SERVER/logs/latest.log' 2>/dev/null"; then break; fi
  sleep 3
done
sleep 5

echo "=== RPGServer lines ==="
ssh -o BatchMode=yes "$HOST" "grep -i RPGServer '$SERVER/logs/latest.log' | tail -20"
