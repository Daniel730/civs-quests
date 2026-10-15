#!/usr/bin/env python3
from pathlib import Path

p = Path("/home/daniel/mineserver/plugins/RPGServer/config.yml")
text = p.read_text(encoding="utf-8")

if "layout: hearts-slot" not in text:
    text = text.replace(
        "  composed:\n    enabled: true\n    interval-ticks: 10\n",
        "  composed:\n    enabled: true\n    layout: hearts-slot\n    interval-ticks: 10\n"
        "    hearts-slot:\n      shift-left: 82\n      gap: 4\n      segments: 10\n",
        1,
    )

text = text.replace(
    "esconde corações vanilla (vida/mana no ActionBar)",
    "vida/mana no lugar dos corações (fome fica)",
)

# Drop quest from legacy format line if still present
needle = ' <dark_gray>|</dark_gray> <gold>{quest}</gold>"'
if needle in text and "format:" in text:
    text = text.replace(needle, '"')

p.write_text(text, encoding="utf-8")
print("OK")
show = False
for line in p.read_text(encoding="utf-8").splitlines():
    if line.startswith("hud:"):
        show = True
    if show:
        print(line)
        if line.startswith("messages:"):
            break
