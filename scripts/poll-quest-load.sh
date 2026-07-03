#!/usr/bin/env bash
for i in $(seq 1 60); do
  out=$(ssh -o BatchMode=yes daniel@bot-server "grep Carregadas /home/daniel/mineserver/logs/latest.log 2>/dev/null | tail -1")
  if [ -n "$out" ]; then
    echo "$out"
    ssh -o BatchMode=yes daniel@bot-server "grep -E 'Carregadas|RPGServer.*Enabling|Falha ao carregar quest' /home/daniel/mineserver/logs/latest.log | tail -15"
    exit 0
  fi
  sleep 3
done
echo "timeout waiting for quest load"
ssh -o BatchMode=yes daniel@bot-server "tail -30 /home/daniel/mineserver/logs/latest.log"
exit 1
