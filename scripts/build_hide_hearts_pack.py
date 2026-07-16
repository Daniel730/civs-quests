#!/usr/bin/env python3
"""Build RPG HUD resource pack:
- blank vanilla heart sprites (hunger untouched)
- custom font rpg:hud with HP/mana bar glyphs + negative spaces

ActionBar text is centered above the hotbar; bitmap ascent + negative space
shift the glyphs onto the vacated hearts row (left of hunger).
"""
from __future__ import annotations

import hashlib
import json
import struct
import zlib
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1] / "resource-packs" / "hide-vanilla-hearts"
ZIP_PATH = Path(__file__).resolve().parents[1] / "resource-packs" / "hide-vanilla-hearts.zip"

# Transparent 1x1
PNG_EMPTY = bytes.fromhex(
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

# Bitmap bars sit on the hearts row when sent via ActionBar (tuned for GUI scale 1–2).
BAR_ASCENT = -4
BAR_HEIGHT = 9
BAR_W = 8
BAR_H = 9

# AmberWat-style space widths (subset). Negative height + ascent -32768 = advance only.
SPACE_SIZES = [
    (-1, "\uF801"), (-2, "\uF802"), (-3, "\uF803"), (-4, "\uF804"),
    (-5, "\uF805"), (-6, "\uF806"), (-7, "\uF807"), (-8, "\uF808"),
    (-16, "\uF809"), (-32, "\uF80A"), (-64, "\uF80B"), (-128, "\uF80C"),
    (1, "\uF821"), (2, "\uF822"), (3, "\uF823"), (4, "\uF824"),
    (5, "\uF825"), (6, "\uF826"), (7, "\uF827"), (8, "\uF828"),
    (16, "\uF829"), (32, "\uF82A"), (64, "\uF82B"), (128, "\uF82C"),
]

# Glyphs used by HeartsSlotHudComposer
CHAR_HP_FULL = "\uE010"
CHAR_HP_EMPTY = "\uE011"
CHAR_MANA_FULL = "\uE020"
CHAR_MANA_EMPTY = "\uE021"
CHAR_SEP = "\uE030"


def png_rgba(width: int, height: int, pixels: list[tuple[int, int, int, int]]) -> bytes:
    """pixels row-major RGBA."""
    raw = bytearray()
    for y in range(height):
        raw.append(0)  # filter None
        for x in range(width):
            r, g, b, a = pixels[y * width + x]
            raw.extend((r, g, b, a))
    compressed = zlib.compress(bytes(raw), 9)

    def chunk(tag: bytes, data: bytes) -> bytes:
        return struct.pack(">I", len(data)) + tag + data + struct.pack(
            ">I", zlib.crc32(tag + data) & 0xFFFFFFFF
        )

    ihdr = struct.pack(">IIBBBBB", width, height, 8, 6, 0, 0, 0)
    return (
        b"\x89PNG\r\n\x1a\n"
        + chunk(b"IHDR", ihdr)
        + chunk(b"IDAT", compressed)
        + chunk(b"IEND", b"")
    )


def solid_bar(fill: tuple[int, int, int, int], edge: tuple[int, int, int, int]) -> bytes:
    pixels: list[tuple[int, int, int, int]] = []
    for y in range(BAR_H):
        for x in range(BAR_W):
            if x == 0 or y == 0 or x == BAR_W - 1 or y == BAR_H - 1:
                pixels.append(edge)
            else:
                pixels.append(fill)
    return png_rgba(BAR_W, BAR_H, pixels)


def empty_bar(edge: tuple[int, int, int, int], inside: tuple[int, int, int, int]) -> bytes:
    return solid_bar(inside, edge)


def sep_bar() -> bytes:
    # 2x9 thin separator
    w, h = 2, BAR_H
    pixels = []
    for y in range(h):
        for x in range(w):
            pixels.append((40, 40, 40, 180) if x == 0 else (0, 0, 0, 0))
    return png_rgba(w, h, pixels)


def main() -> None:
    ROOT.mkdir(parents=True, exist_ok=True)
    meta = {
        "pack": {
            "pack_format": 84,
            "description": "Civs/RPG — hearts hidden; HP/mana bars in hearts slot",
        }
    }
    (ROOT / "pack.mcmeta").write_text(json.dumps(meta, indent=2) + "\n", encoding="utf-8")

    heart_dir = ROOT / "assets" / "minecraft" / "textures" / "gui" / "sprites" / "hud" / "heart"
    heart_dir.mkdir(parents=True, exist_ok=True)
    for name in HEARTS:
        (heart_dir / f"{name}.png").write_bytes(PNG_EMPTY)

    tex = ROOT / "assets" / "rpg" / "textures" / "font"
    tex.mkdir(parents=True, exist_ok=True)
    (tex / "space.png").write_bytes(PNG_EMPTY)
    (tex / "hp_full.png").write_bytes(solid_bar((220, 40, 40, 255), (80, 10, 10, 255)))
    (tex / "hp_empty.png").write_bytes(empty_bar((80, 10, 10, 255), (35, 10, 10, 200)))
    (tex / "mana_full.png").write_bytes(solid_bar((40, 180, 220, 255), (10, 50, 80, 255)))
    (tex / "mana_empty.png").write_bytes(empty_bar((10, 50, 80, 255), (10, 25, 40, 200)))
    (tex / "sep.png").write_bytes(sep_bar())

    providers = []
    for width, char in SPACE_SIZES:
        providers.append({
            "type": "bitmap",
            "file": "rpg:font/space.png",
            "ascent": -32768,
            "height": width,
            "chars": [char],
        })
    providers.extend([
        {
            "type": "bitmap",
            "file": "rpg:font/hp_full.png",
            "ascent": BAR_ASCENT,
            "height": BAR_HEIGHT,
            "chars": [CHAR_HP_FULL],
        },
        {
            "type": "bitmap",
            "file": "rpg:font/hp_empty.png",
            "ascent": BAR_ASCENT,
            "height": BAR_HEIGHT,
            "chars": [CHAR_HP_EMPTY],
        },
        {
            "type": "bitmap",
            "file": "rpg:font/mana_full.png",
            "ascent": BAR_ASCENT,
            "height": BAR_HEIGHT,
            "chars": [CHAR_MANA_FULL],
        },
        {
            "type": "bitmap",
            "file": "rpg:font/mana_empty.png",
            "ascent": BAR_ASCENT,
            "height": BAR_HEIGHT,
            "chars": [CHAR_MANA_EMPTY],
        },
        {
            "type": "bitmap",
            "file": "rpg:font/sep.png",
            "ascent": BAR_ASCENT,
            "height": BAR_HEIGHT,
            "chars": [CHAR_SEP],
        },
    ])

    font_dir = ROOT / "assets" / "rpg" / "font"
    font_dir.mkdir(parents=True, exist_ok=True)
    (font_dir / "hud.json").write_text(
        json.dumps({"providers": providers}, indent=2, ensure_ascii=False) + "\n",
        encoding="utf-8",
    )

    if ZIP_PATH.exists():
        ZIP_PATH.unlink()
    with zipfile.ZipFile(ZIP_PATH, "w", compression=zipfile.ZIP_DEFLATED) as zf:
        for path in ROOT.rglob("*"):
            if path.is_file():
                zf.write(path, path.relative_to(ROOT).as_posix())

    digest = hashlib.sha1(ZIP_PATH.read_bytes()).hexdigest()
    (ZIP_PATH.with_suffix(".zip.sha1")).write_text(digest + "\n", encoding="utf-8")
    resources = Path(__file__).resolve().parents[1] / "src" / "main" / "resources" / "resource-packs"
    resources.mkdir(parents=True, exist_ok=True)
    dest = resources / "hide-vanilla-hearts.zip"
    dest.write_bytes(ZIP_PATH.read_bytes())
    print(f"Wrote {ZIP_PATH} sha1={digest} files={sum(1 for _ in ROOT.rglob('*') if _.is_file())}")
    print(f"Copied to {dest}")
    print(f"BAR_ASCENT={BAR_ASCENT} BAR_HEIGHT={BAR_HEIGHT}")


if __name__ == "__main__":
    main()
