#!/usr/bin/env bash
set -euo pipefail
HOST=daniel@bot-server
SERVER=/home/daniel/mineserver
PLUGINS="$SERVER/plugins"
QUESTS="$PLUGINS/RPGServer/quests"
RPG=/mnt/c/Users/Danie/Downloads/Civs-1.11.6/rpg-server-plugin/target/rpg-server-0.1.0-SNAPSHOT.jar
QUEST_SRC=/mnt/c/Users/Danie/Downloads/Civs-1.11.6/rpg-server-plugin/src/main/resources/quests
TS=$(date +%Y%m%d-%H%M)
BACKUP="$SERVER/plugins-backup-$TS"

test -f "$RPG"

echo "Stopping server..."
ssh -o BatchMode=yes "$HOST" "tmux -L mc send-keys -t minecraft stop C-m || true"
for i in $(seq 1 60); do
  if ! ssh -o BatchMode=yes "$HOST" "pgrep -f $SERVER/server.jar >/dev/null"; then
    break
  fi
  sleep 2
done
ssh -o BatchMode=yes "$HOST" "tmux -L mc kill-session -t minecraft 2>/dev/null || true"

echo "Backing up plugins to $BACKUP..."
ssh -o BatchMode=yes "$HOST" "cp -a '$PLUGINS' '$BACKUP'"

echo "Deploying JAR and weekly quest YAMLs..."
scp -o BatchMode=yes "$RPG" "${HOST}:${PLUGINS}/"
ssh -o BatchMode=yes "$HOST" "mkdir -p '$QUESTS'"
scp -o BatchMode=yes \
  "$QUEST_SRC/weekly_warrior.yml" \
  "$QUEST_SRC/weekly_merchant.yml" \
  "$QUEST_SRC/weekly_builder.yml" \
  "${HOST}:${QUESTS}/"

echo "Starting server..."
ssh -o BatchMode=yes "$HOST" "cd '$SERVER' && tmux -L mc new-session -d -s minecraft /opt/java25/bin/java -Xms4G -Xmx4G -jar server.jar nogui"

echo "Waiting for quest load line..."
for i in $(seq 1 90); do
  if ssh -o BatchMode=yes "$HOST" "grep -q 'Carregadas 10 quests' '$SERVER/logs/latest.log' 2>/dev/null"; then
    echo "SUCCESS: Carregadas 10 quests"
    ssh -o BatchMode=yes "$HOST" "grep -E 'Carregadas [0-9]+ quests|RPGServer' '$SERVER/logs/latest.log' | tail -8"
    echo "Backup: $BACKUP"
    exit 0
  fi
  sleep 2
done

echo "Quest count line not found yet; tail log:"
ssh -o BatchMode=yes "$HOST" "grep -E 'Carregadas [0-9]+ quests|RPGServer|Falha ao carregar' '$SERVER/logs/latest.log' | tail -15"
exit 1
