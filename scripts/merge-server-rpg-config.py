#!/usr/bin/env python3
"""Merge missing keys from template into live server config without overwriting existing values."""
from __future__ import annotations

import sys
from pathlib import Path

try:
    import yaml
except ImportError:
    print("PyYAML required: pip install pyyaml", file=sys.stderr)
    sys.exit(1)


def deep_merge(base: dict, template: dict) -> dict:
    for key, value in template.items():
        if key not in base:
            base[key] = value
        elif isinstance(base[key], dict) and isinstance(value, dict):
            deep_merge(base[key], value)
    return base


def main() -> int:
    if len(sys.argv) != 3:
        print(f"Usage: {sys.argv[0]} <live-config> <template-config>", file=sys.stderr)
        return 1
    live_path = Path(sys.argv[1])
    template_path = Path(sys.argv[2])
    live = yaml.safe_load(live_path.read_text(encoding="utf-8")) or {}
    template = yaml.safe_load(template_path.read_text(encoding="utf-8")) or {}
    merged = deep_merge(live, template)
    live_path.write_text(
        yaml.dump(merged, allow_unicode=True, default_flow_style=False, sort_keys=False),
        encoding="utf-8",
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
