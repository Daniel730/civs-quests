#!/usr/bin/env bash
# Runs on bot-server (stdin via SSH or directly). Stop/start Paper in tmux -L mc.
set -euo pipefail

SERVER="${MINESERVER_ROOT:-/home/daniel/mineserver}"
JAVA="${MINESERVER_JAVA:-/opt/java25/bin/java}"
JAVA_OPTS="${MINESERVER_JAVA_OPTS:--Xms4G -Xmx4G}"
JAR="${MINESERVER_JAR:-server.jar}"
TMUX_SOCKET="${MINESERVER_TMUX_SOCKET:-mc}"
TMUX_SESSION="${MINESERVER_TMUX_SESSION:-minecraft}"
STOP_TIMEOUT="${MINESERVER_STOP_TIMEOUT:-120}"

ACTION="${1:-restart}"

tmux_mc() {
  tmux -L "$TMUX_SOCKET" "$@"
}

java_pids() {
  local pid cwd
  while read -r pid; do
    [[ -z "$pid" ]] && continue
    cwd="$(readlink -f "/proc/$pid/cwd" 2>/dev/null || true)"
    if [[ "$cwd" == "$SERVER" ]]; then
      echo "$pid"
    fi
  done < <(pgrep -f '^/opt/java25/bin/java .+-jar server\.jar' 2>/dev/null || true)
}

rcon_stop() {
  local props="$SERVER/server.properties"
  [[ -f "$props" ]] || return 1
  local enable port password
  enable="$(grep -E '^enable-rcon=' "$props" | cut -d= -f2- | tr -d ' \r')"
  [[ "$enable" == "true" ]] || return 1
  port="$(grep -E '^rcon.port=' "$props" | cut -d= -f2- | tr -d ' \r')"
  password="$(grep -E '^rcon.password=' "$props" | cut -d= -f2- | tr -d ' \r')"
  [[ -n "$port" && -n "$password" ]] || return 1

  if command -v mcrcon >/dev/null 2>&1; then
    mcrcon -H 127.0.0.1 -P "$port" -p "$password" stop >/dev/null 2>&1 && return 0
  fi

  python3 - "$port" "$password" <<'PY'
import socket, struct, sys
port, password = sys.argv[1], sys.argv[2]

def packet(req_id, pkt_type, body):
    payload = body.encode("utf-8") + b"\x00\x00"
    return struct.pack("<iii", len(payload) + 8, req_id, pkt_type) + payload

def read_packet(sock):
    raw = sock.recv(4)
    if len(raw) < 4:
        return None
    (length,) = struct.unpack("<i", raw)
    data = b""
    while len(data) < length:
        chunk = sock.recv(length - len(data))
        if not chunk:
            break
        data += chunk
    if len(data) < 8:
        return None
    req_id, pkt_type = struct.unpack("<ii", data[:8])
    body = data[8:-2].decode("utf-8", errors="replace")
    return req_id, pkt_type, body

sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
sock.settimeout(15)
sock.connect(("127.0.0.1", int(port)))
sock.sendall(packet(1, 3, password))
read_packet(sock)
sock.sendall(packet(2, 2, "stop"))
read_packet(sock)
sock.close()
PY
}

graceful_stop() {
  local pids
  mapfile -t pids < <(java_pids)
  if [[ ${#pids[@]} -eq 0 ]]; then
    echo "mineserver: no Java process in $SERVER"
    tmux_mc kill-session -t "$TMUX_SESSION" 2>/dev/null || true
    return 0
  fi

  echo "mineserver: stopping PIDs: ${pids[*]}"

  if tmux_mc has-session -t "$TMUX_SESSION" 2>/dev/null; then
    echo "mineserver: tmux stop command"
    tmux_mc send-keys -t "$TMUX_SESSION" stop C-m || true
  elif rcon_stop; then
    echo "mineserver: RCON stop sent"
  else
    echo "mineserver: no tmux session and RCON unavailable; sending SIGTERM"
    kill -TERM "${pids[@]}" 2>/dev/null || true
  fi

  local i
  for ((i = 0; i < STOP_TIMEOUT; i += 2)); do
    mapfile -t pids < <(java_pids)
    [[ ${#pids[@]} -eq 0 ]] && break
    sleep 2
  done

  mapfile -t pids < <(java_pids)
  if [[ ${#pids[@]} -gt 0 ]]; then
    echo "mineserver: force kill remaining PIDs: ${pids[*]}"
    kill -KILL "${pids[@]}" 2>/dev/null || true
    sleep 2
  fi

  tmux_mc kill-session -t "$TMUX_SESSION" 2>/dev/null || true
  mapfile -t pids < <(java_pids)
  if [[ ${#pids[@]} -gt 0 ]]; then
    echo "mineserver: ERROR still running: ${pids[*]}" >&2
    return 1
  fi
  echo "mineserver: stopped"
}

start_server() {
  mapfile -t pids < <(java_pids)
  if [[ ${#pids[@]} -gt 0 ]]; then
    echo "mineserver: already running PIDs: ${pids[*]}" >&2
    return 1
  fi

  tmux_mc kill-session -t "$TMUX_SESSION" 2>/dev/null || true
  cd "$SERVER"
  echo "mineserver: starting in tmux -L $TMUX_SOCKET session $TMUX_SESSION"
  tmux_mc new-session -d -s "$TMUX_SESSION" \
    "$JAVA" $JAVA_OPTS -jar "$JAR" nogui
  sleep 2
  mapfile -t pids < <(java_pids)
  if [[ ${#pids[@]} -eq 0 ]]; then
    echo "mineserver: ERROR Java did not start" >&2
    return 1
  fi
  echo "mineserver: started PID ${pids[0]}"
  if tmux_mc has-session -t "$TMUX_SESSION" 2>/dev/null; then
    echo "mineserver: tmux session active"
  else
    echo "mineserver: WARN Java running but tmux session missing" >&2
  fi
}

case "$ACTION" in
  stop) graceful_stop ;;
  start) start_server ;;
  restart) graceful_stop; start_server ;;
  *)
    echo "usage: $0 [stop|start|restart]" >&2
    exit 2
    ;;
esac
