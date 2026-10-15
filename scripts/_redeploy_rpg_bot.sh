#!/usr/bin/env bash
set -euo pipefail
HOST=daniel@bot-server
SERVER=/home/daniel/mineserver
PLUGINS="$SERVER/plugins"
SCRIPT=/mnt/c/Users/Danie/Downloads/Civs-1.11.6/rpg-server-plugin/scripts/mineserver-control-remote.sh
RPG_JAR=/mnt/c/Users/Danie/Downloads/Civs-1.11.6/rpg-server-plugin/target/rpg-server-0.1.2.jar

sed -i 's/\r$//' "$SCRIPT"
test -f "$RPG_JAR"

echo "== stop =="
ssh -o BatchMode=yes "$HOST" 'bash -s' -- stop < "$SCRIPT"
echo "== scp rpg =="
scp -o BatchMode=yes "$RPG_JAR" "${HOST}:${PLUGINS}/rpg-server-0.1.2.jar"
echo "== start =="
ssh -o BatchMode=yes "$HOST" 'bash -s' -- start < "$SCRIPT"

LOG="$SERVER/logs/latest.log"
for i in $(seq 1 60); do
  if ssh -o BatchMode=yes "$HOST" "grep -q 'Done (' \"$LOG\" 2>/dev/null"; then
    break
  fi
  sleep 3
done
sleep 6

ssh -o BatchMode=yes "$HOST" "grep -iE 'hide-heart|Pacote|HUD composto|Enabling RPG|Done \\(' \"$LOG\" | tail -20"
ssh -o BatchMode=yes "$HOST" "curl -sI -m 3 http://127.0.0.1:8765/hide-vanilla-hearts.zip | head -3"
ssh -o BatchMode=yes "$HOST" "echo PLAYERS=\$(ls \"$PLUGINS/Civs/players\" | wc -l) TOWNS=\$(ls \"$PLUGINS/Civs/towns\" | wc -l) REGIONS=\$(ls \"$PLUGINS/Civs/regions\" | wc -l)"
echo RPG_REDEPLOY_OK
