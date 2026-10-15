#!/usr/bin/env python3
"""Idempotent patch: ensure hud.hide-vanilla-hearts on live RPGServer config."""
from pathlib import Path

CFG = Path("/home/daniel/mineserver/plugins/RPGServer/config.yml")
HOST = "<TAILSCALE_IP>"

SECTION = f"""  hide-vanilla-hearts:
    enabled: true
    http-port: 8765
    host: "{HOST}"
    url: ""
    force: true
    prompt: "<yellow>Pacote HUD</yellow><gray> — esconde corações vanilla (vida/mana no ActionBar)</gray>"
"""


def main() -> None:
    raw = CFG.read_text(encoding="utf-8")
    lines = raw.splitlines(keepends=True)
    out = []
    i = 0
    skipped_old = False
    while i < len(lines):
        line = lines[i]
        if line.startswith("  hide-vanilla-hearts:"):
            # drop old section (indented under hud)
            i += 1
            while i < len(lines) and (lines[i].startswith("    ") or lines[i].strip() == ""):
                # stop if next key at hud indent level that isn't blank continuation
                if lines[i].startswith("  ") and not lines[i].startswith("    ") and lines[i].strip():
                    break
                if lines[i].startswith("messages:") or (lines[i] and not lines[i].startswith(" ")):
                    break
                i += 1
            if not skipped_old:
                out.append(SECTION if SECTION.endswith("\n") else SECTION + "\n")
                skipped_old = True
            continue
        if line.startswith("messages:") and not skipped_old and "hide-vanilla-hearts:" not in raw:
            out.append(SECTION if SECTION.endswith("\n") else SECTION + "\n")
            skipped_old = True
        out.append(line)
        i += 1

    if "hide-vanilla-hearts:" not in "".join(out):
        # last resort: before messages
        final = []
        inserted = False
        for line in out:
            if line.startswith("messages:") and not inserted:
                final.append(SECTION if SECTION.endswith("\n") else SECTION + "\n")
                inserted = True
            final.append(line)
        out = final

    CFG.write_text("".join(out), encoding="utf-8")
    print("OK")
    # show hud block
    show = False
    for line in CFG.read_text(encoding="utf-8").splitlines():
        if line.startswith("hud:"):
            show = True
        if show:
            print(line)
            if line.startswith("messages:"):
                break


if __name__ == "__main__":
    main()
