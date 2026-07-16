#!/usr/bin/env python3
"""Build a minimal resource pack that hides vanilla HUD hearts (transparent textures)."""
from __future__ import annotations

import hashlib
import json
import zipfile
from pathlib import Path

# 1x1 fully transparent PNG
PNG = bytes.fromhex(
    "89504e470d0a1a0a0000000d49484452000000010000000108060000001f15c489"
    "0000000a49444154789c63000100000500010d0a2db40000000049454e44ae426082"
)

HEARTS = [
    "full", "full_blinking", "half", "half_blinking",
    "hardcore_full", "hardcore_full_blinking", "hardcore_half", "hardcore_half_blinking",
    "absorbing_full", "absorbing_full_blinking", "absorbing_half", "absorbing_half_blinking",
    "poisoned_full", "poisoned_full_blinking", "poisoned_half", "poisoned_half_blinking",
    "withered_full", "withered_full_blinking", "withered_half", "withered_half_blinking",
    "frozen_full", "frozen_full_blinking", "frozen_half", "frozen_half_blinking",
    "container", "container_hardcore", "container_blinking",
]

ROOT = Path(__file__).resolve().parents[1] / "resource-packs" / "hide-vanilla-hearts"
ZIP_PATH = Path(__file__).resolve().parents[1] / "resource-packs" / "hide-vanilla-hearts.zip"


def main() -> None:
    ROOT.mkdir(parents=True, exist_ok=True)
    meta = {
        "pack": {
            "pack_format": 84,
            "description": "Civs/RPG — hide vanilla hearts (HP on ActionBar)",
        }
    }
    (ROOT / "pack.mcmeta").write_text(json.dumps(meta, indent=2) + "\n", encoding="utf-8")
    heart_dir = ROOT / "assets" / "minecraft" / "textures" / "gui" / "sprites" / "hud" / "heart"
    heart_dir.mkdir(parents=True, exist_ok=True)
    for name in HEARTS:
        (heart_dir / f"{name}.png").write_bytes(PNG)

    if ZIP_PATH.exists():
        ZIP_PATH.unlink()
    with zipfile.ZipFile(ZIP_PATH, "w", compression=zipfile.ZIP_DEFLATED) as zf:
        for path in ROOT.rglob("*"):
            if path.is_file():
                zf.write(path, path.relative_to(ROOT).as_posix())
    digest = hashlib.sha1(ZIP_PATH.read_bytes()).hexdigest()
    (ZIP_PATH.with_suffix(".zip.sha1")).write_text(digest + "\n", encoding="utf-8")
    # Also embed in plugin jar resources
    resources = Path(__file__).resolve().parents[1] / "src" / "main" / "resources" / "resource-packs"
    resources.mkdir(parents=True, exist_ok=True)
    dest = resources / "hide-vanilla-hearts.zip"
    dest.write_bytes(ZIP_PATH.read_bytes())
    print(f"Wrote {ZIP_PATH} sha1={digest}")
    print(f"Copied to {dest}")


if __name__ == "__main__":
    main()
