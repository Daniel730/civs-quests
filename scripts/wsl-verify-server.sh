#!/usr/bin/env bash
set -euo pipefail
HOST=daniel@bot-server
LOG=/home/daniel/mineserver/logs/latest.log
for i in $(seq 1 40); do
  if ssh -o BatchMode=yes "$HOST" "grep -q 'Done (' '$LOG' 2>/dev/null"; then
    break
  fi
  sleep 5
done
sleep 3
ssh -o BatchMode=yes "$HOST" "grep -E 'Carregadas|Carregados|LuckPerms API|RPGServer habilitado|Done \\(' '$LOG' | tail -12"
