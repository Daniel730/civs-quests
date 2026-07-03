#!/usr/bin/env bash
set -euo pipefail
HOST=daniel@bot-server
SERVER=/home/daniel/mineserver
PLUGINS="$SERVER/plugins"
RPG=/mnt/c/Users/Danie/Downloads/Civs-1.11.6/rpg-server-plugin/target/rpg-server-0.1.0-SNAPSHOT.jar
BOOKS_DIR=/mnt/c/Users/Danie/Downloads/Civs-1.11.6/rpg-server-plugin/src/main/resources/books
WARRIOR_QUEST=/mnt/c/Users/Danie/Downloads/Civs-1.11.6/rpg-server-plugin/src/main/resources/quests/warrior_path.yml
TS=$(date +%Y%m%d-%H%M)
BACKUP="$SERVER/plugins-backup-$TS"
IB_BOOKS="$PLUGINS/InteractiveBooks/books"

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
ssh -o BatchMode=yes "$HOST" "mkdir -p '$IB_BOOKS' '$PLUGINS/RPGServer/quests'"
scp -o BatchMode=yes "$BOOKS_DIR"/*.yml "${HOST}:${IB_BOOKS}/"
scp -o BatchMode=yes "$WARRIOR_QUEST" "${HOST}:${PLUGINS}/RPGServer/quests/warrior_path.yml"

ssh -o BatchMode=yes "$HOST" "cd '$SERVER' && tmux -L mc new-session -d -s minecraft /opt/java25/bin/java -Xms4G -Xmx4G -jar server.jar nogui"

for i in $(seq 1 90); do
  if ssh -o BatchMode=yes "$HOST" "grep -q 'Done (' '$SERVER/logs/latest.log' 2>/dev/null"; then break; fi
  sleep 3
done
sleep 5

echo "=== RPGServer / InteractiveBooks lines ==="
ssh -o BatchMode=yes "$HOST" "grep -iE 'RPGServer|InteractiveBooks|quest book|Carregadas' '$SERVER/logs/latest.log' | tail -60"
