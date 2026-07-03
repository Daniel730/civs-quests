HOST=daniel@bot-server
SERVER=/home/daniel/mineserver
PLUGINS="$SERVER/plugins"

echo "=== process ==="
ssh -o BatchMode=yes "$HOST" "pgrep -af server.jar || true"
ssh -o BatchMode=yes "$HOST" "tmux -L mc list-sessions 2>/dev/null || true"

for i in $(seq 1 60); do
  if ssh -o BatchMode=yes "$HOST" "grep -q 'Done (' '$SERVER/logs/latest.log' 2>/dev/null"; then echo "Done found"; break; fi
  sleep 3
done
sleep 2

echo "=== Carregadas quests ==="
ssh -o BatchMode=yes "$HOST" "grep 'Carregadas' '$SERVER/logs/latest.log' | tail -5"
echo "=== RPG enable block ==="
ssh -o BatchMode=yes "$HOST" "grep -E 'RPGServer|Carregadas|WARN.*RPG|ERROR.*RPG' '$SERVER/logs/latest.log' | tail -35"
echo "=== sprint3 file ==="
ssh -o BatchMode=yes "$HOST" "ls -la '$PLUGINS/RPGServer/quests/sprint3_daily.yml'"
