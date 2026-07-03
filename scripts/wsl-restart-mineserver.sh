#!/usr/bin/env bash
# WSL: stop/start mineserver on bot-server (works with or without tmux).
set -euo pipefail
HOST="${MINESERVER_HOST:-daniel@bot-server}"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ACTION="${1:-restart}"

case "$ACTION" in
  stop|start|restart) ;;
  *)
    echo "usage: $0 [stop|start|restart]" >&2
    exit 2
    ;;
esac

ssh -o BatchMode=yes "$HOST" 'bash -s' -- "$ACTION" < "$SCRIPT_DIR/mineserver-control-remote.sh"
