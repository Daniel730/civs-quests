#!/usr/bin/env bash
# Configure production error-reporting: GitHub PAT file + RPG config section.
set -euo pipefail
HOST="${MINESERVER_HOST:-daniel@bot-server}"
SERVER=/home/daniel/mineserver
SECRETS="$SERVER/secrets"
PAT_FILE="$SECRETS/github-pat.txt"
CONFIG="$SERVER/plugins/RPGServer/config.yml"
GH="/mnt/c/Users/Danie/tools/gh/bin/gh.exe"

if ! "$GH" auth status >/dev/null 2>&1; then
  echo "GitHub CLI not authenticated. Run: gh auth login"
  exit 1
fi

TOKEN=$("$GH" auth token)
test -n "$TOKEN"

ssh -o BatchMode=yes "$HOST" "mkdir -p '$SECRETS' && chmod 700 '$SECRETS'"
printf '%s' "$TOKEN" | ssh -o BatchMode=yes "$HOST" "umask 077 && cat > '$PAT_FILE' && chmod 600 '$PAT_FILE'"

ssh -o BatchMode=yes "$HOST" "python3 -" <<'PY'
from pathlib import Path
path = Path("/home/daniel/mineserver/plugins/RPGServer/config.yml")
text = path.read_text(encoding="utf-8") if path.exists() else ""
block = """
error-reporting:
  enabled: true
  server-name: bot-server
  dedupe-minutes: 15
  include-plugins: []
  sources:
    paper-server-exception: true
    uncaught-threads: true
    log4j-errors: true
  github:
    enabled: true
    token: ""
    token-file: "/home/daniel/mineserver/secrets/github-pat.txt"
    owner: Daniel730
    repo: civs-quests
    labels:
      - server-error
  webhook:
    enabled: false
    url: ""
""".strip("\n")
if "error-reporting:" in text:
    import re
    text = re.sub(r"error-reporting:.*?(?=\n\S|\Z)", block + "\n", text, count=1, flags=re.S)
else:
    text = text.rstrip() + "\n\n" + block + "\n"
path.write_text(text, encoding="utf-8")
print("CONFIG_OK")
PY

echo "Error reporting configured. PAT: $PAT_FILE (mode 600)"
echo "Run /rpg reload on server or restart to activate."
