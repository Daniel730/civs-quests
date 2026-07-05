#!/usr/bin/env bash
# Runs ON bot-server (stdin via SSH or directly). Lists RPG error reports + recent log errors.
# Output: JSON summary on stdout; optional --markdown for human-readable summary.
set -euo pipefail

SERVER="${MINESERVER_ROOT:-/home/daniel/mineserver}"
REPORT_DIR="${RPG_ERROR_REPORT_DIR:-$SERVER/plugins/RPGServer/error-reports}"
LOG="${MINESERVER_LOG:-$SERVER/logs/latest.log}"
TMUX_SOCKET="${MINESERVER_TMUX_SOCKET:-mc}"
TMUX_SESSION="${MINESERVER_TMUX_SESSION:-minecraft}"
SEND_RPG_ERRORS="${SEND_RPG_ERRORS:-1}"
LOG_TAIL_LINES="${LOG_ERROR_TAIL_LINES:-40}"
RECENT_REPORTS="${RECENT_REPORTS:-5}"
FORMAT="${FORMAT:-json}"

usage() {
  cat <<'EOF'
Usage: fetch-error-reports.sh [--json|--markdown] [--no-command]

  --json          JSON summary (default)
  --markdown      Markdown summary for Cursor / automation
  --no-command    Skip sending "/rpg errors" to the Minecraft console

Environment:
  MINESERVER_ROOT, RPG_ERROR_REPORT_DIR, MINESERVER_LOG
  MINESERVER_TMUX_SOCKET, MINESERVER_TMUX_SESSION
  SEND_RPG_ERRORS=0|1, LOG_ERROR_TAIL_LINES, RECENT_REPORTS
EOF
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --json) FORMAT=json; shift ;;
    --markdown) FORMAT=markdown; shift ;;
    --no-command) SEND_RPG_ERRORS=0; shift ;;
    -h|--help) usage; exit 0 ;;
    *) echo "Unknown option: $1" >&2; usage >&2; exit 1 ;;
  esac
done

send_rpg_errors() {
  if [[ "$SEND_RPG_ERRORS" != "1" ]]; then
    return 0
  fi
  if ! tmux -L "$TMUX_SOCKET" has-session -t "$TMUX_SESSION" 2>/dev/null; then
    echo "WARN: tmux session $TMUX_SESSION not found; skipped /rpg errors" >&2
    return 0
  fi
  tmux -L "$TMUX_SOCKET" send-keys -t "$TMUX_SESSION" "/rpg errors" Enter
  sleep 1
}

list_reports() {
  if [[ ! -d "$REPORT_DIR" ]]; then
    return 0
  fi
  find "$REPORT_DIR" -maxdepth 1 -type f -name '*.md' ! -name 'latest.md' -printf '%T@ %p\n' 2>/dev/null \
    | sort -rn \
    | head -n "$RECENT_REPORTS" \
    | cut -d' ' -f2-
}

latest_mtime() {
  local path="$1"
  if [[ -f "$path" ]]; then
    stat -c '%Y' "$path" 2>/dev/null || stat -f '%m' "$path" 2>/dev/null || echo 0
  else
    echo 0
  fi
}

log_errors_block() {
  if [[ ! -f "$LOG" ]]; then
    echo ""
    return
  fi
  grep -inE 'ERROR|SEVERE|Exception|Caused by|Could not load|Disabling' "$LOG" 2>/dev/null | tail -n "$LOG_TAIL_LINES" || true
}

send_rpg_errors

LATEST="$REPORT_DIR/latest.md"
mapfile -t REPORT_FILES < <(list_reports)
LATEST_MTIME="$(latest_mtime "$LATEST")"
LOG_BLOCK="$(log_errors_block)"

if [[ "$FORMAT" == "markdown" ]]; then
  {
    echo "# RPG error snapshot"
    echo ""
    echo "- **Server:** $(hostname -f 2>/dev/null || hostname)"
    echo "- **Fetched (UTC):** $(date -u +%Y-%m-%dT%H:%M:%SZ)"
    echo "- **Report directory:** \`$REPORT_DIR\`"
    echo "- **Latest report:** \`$LATEST\` (mtime epoch $LATEST_MTIME)"
    echo ""
    echo "## Recent report files"
    if [[ ${#REPORT_FILES[@]} -eq 0 ]]; then
      echo "_No timestamped reports yet._"
    else
      for f in "${REPORT_FILES[@]}"; do
        echo "- \`$(basename "$f")\`"
      done
    fi
    echo ""
    if [[ -f "$LATEST" ]]; then
      echo "## latest.md"
      echo ""
      cat "$LATEST"
      echo ""
    else
      echo "## latest.md"
      echo ""
      echo "_No latest.md — server may have no captured errors yet._"
      echo ""
    fi
    echo "## Recent log errors (tail)"
    echo ""
    echo '```text'
    if [[ -n "$LOG_BLOCK" ]]; then
      printf '%s\n' "$LOG_BLOCK"
    else
      echo "(no matching ERROR/SEVERE lines in tail)"
    fi
    echo '```'
  }
  exit 0
fi

# JSON output
export LOG_BLOCK
python3 - "$REPORT_DIR" "$LATEST" "$LATEST_MTIME" "${REPORT_FILES[@]}" <<'PY'
import json, os, sys, socket, datetime

report_dir = sys.argv[1]
latest = sys.argv[2]
latest_mtime = int(sys.argv[3] or 0)
report_files = sys.argv[4:]
log_block = os.environ.get("LOG_BLOCK", "")

def read_preview(path, limit=1200):
    if not os.path.isfile(path):
        return None
    with open(path, encoding="utf-8", errors="replace") as f:
        data = f.read(limit)
    if len(data) >= limit:
        data += "\n...(truncated)..."
    return data

payload = {
    "server": socket.gethostname(),
    "fetchedAtUtc": datetime.datetime.now(datetime.timezone.utc).strftime("%Y-%m-%dT%H:%M:%SZ"),
    "reportDirectory": report_dir,
    "latestReport": {
        "path": latest,
        "exists": os.path.isfile(latest),
        "mtimeEpoch": latest_mtime,
        "preview": read_preview(latest),
    },
    "recentReports": [
        {"path": p, "name": os.path.basename(p), "mtimeEpoch": int(os.path.getmtime(p)) if os.path.isfile(p) else 0}
        for p in report_files
    ],
    "logErrorsTail": log_block.strip().splitlines() if log_block.strip() else [],
}
print(json.dumps(payload, ensure_ascii=False, indent=2))
PY
