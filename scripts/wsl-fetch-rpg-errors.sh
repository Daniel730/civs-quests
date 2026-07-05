#!/usr/bin/env bash
# WSL entry: SSH to bot-server, send /rpg errors, fetch reports locally for Cursor Automation.
set -euo pipefail

HOST="${MINESERVER_HOST:-daniel@bot-server}"
SERVER="${MINESERVER_ROOT:-/home/daniel/mineserver}"
REPORT_DIR="$SERVER/plugins/RPGServer/error-reports"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REMOTE_SCRIPT="$SCRIPT_DIR/fetch-error-reports.sh"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
SNAPSHOT_DIR="${RPG_ERROR_SNAPSHOT_DIR:-$REPO_ROOT/.error-snapshots}"
SEND_RPG_ERRORS="${SEND_RPG_ERRORS:-1}"
FORMAT="${FORMAT:-both}"

usage() {
  cat <<EOF
Usage: wsl-fetch-rpg-errors.sh [--json-only|--markdown-only]

Fetches RPG error reports from $HOST into:
  $SNAPSHOT_DIR/

Outputs paths for Cursor Automation / manual review.
EOF
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --json-only) FORMAT=json; shift ;;
    --markdown-only) FORMAT=markdown; shift ;;
    -h|--help) usage; exit 0 ;;
    *) echo "Unknown option: $1" >&2; usage >&2; exit 1 ;;
  esac
done

if [[ ! -f "$REMOTE_SCRIPT" ]]; then
  echo "Missing $REMOTE_SCRIPT" >&2
  exit 1
fi

mkdir -p "$SNAPSHOT_DIR/reports"

echo "==> SSH check: $HOST"
ssh -o BatchMode=yes "$HOST" echo ok >/dev/null

echo "==> Remote fetch (tmux /rpg errors + scan reports)"
REMOTE_JSON="$SNAPSHOT_DIR/summary.json"
REMOTE_MD="$SNAPSHOT_DIR/summary.md"

if [[ "$FORMAT" == "json" || "$FORMAT" == "both" ]]; then
  SEND_RPG_ERRORS="$SEND_RPG_ERRORS" ssh -o BatchMode=yes "$HOST" "bash -s -- --json" <"$REMOTE_SCRIPT" >"$REMOTE_JSON"
  echo "Wrote $REMOTE_JSON"
fi

if [[ "$FORMAT" == "markdown" || "$FORMAT" == "both" ]]; then
  SEND_RPG_ERRORS="$SEND_RPG_ERRORS" ssh -o BatchMode=yes "$HOST" "bash -s -- --markdown" <"$REMOTE_SCRIPT" >"$REMOTE_MD"
  echo "Wrote $REMOTE_MD"
fi

echo "==> SCP latest.md + recent reports"
scp -o BatchMode=yes -q "${HOST}:${REPORT_DIR}/latest.md" "$SNAPSHOT_DIR/latest.md" 2>/dev/null \
  || echo "WARN: no latest.md on server yet" >&2

mapfile -t REMOTE_NAMES < <(ssh -o BatchMode=yes "$HOST" \
  "find '$REPORT_DIR' -maxdepth 1 -type f -name '*.md' ! -name 'latest.md' -printf '%f\n' 2>/dev/null | sort -r | head -5" || true)

for name in "${REMOTE_NAMES[@]}"; do
  [[ -z "$name" ]] && continue
  scp -o BatchMode=yes -q "${HOST}:${REPORT_DIR}/${name}" "$SNAPSHOT_DIR/reports/${name}" 2>/dev/null || true
done

FETCHED_AT="$(date -u +%Y-%m-%dT%H:%M:%SZ)"
cat >"$SNAPSHOT_DIR/README.txt" <<EOF
RPG error snapshot — do not commit (gitignored).
Fetched: $FETCHED_AT
Host: $HOST
Remote: $REPORT_DIR

For Cursor Automation, read:
  - .error-snapshots/summary.md  (human + latest.md body)
  - .error-snapshots/summary.json
  - .error-snapshots/latest.md
EOF

echo ""
echo "=== Snapshot ready ==="
echo "Directory: $SNAPSHOT_DIR"
[[ -f "$SNAPSHOT_DIR/summary.md" ]] && echo "Summary:   $SNAPSHOT_DIR/summary.md"
[[ -f "$SNAPSHOT_DIR/latest.md" ]] && echo "Latest:    $SNAPSHOT_DIR/latest.md"
echo ""
echo "Cursor prompt example:"
echo '  Leia .error-snapshots/summary.md, analise a causa raiz e proponha fix (sem deploy).'
