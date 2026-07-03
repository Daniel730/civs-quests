#!/usr/bin/env python3
"""
Offline bulk patch for RPG quest profiles from Civs + AuraSkills YAML (no server required).

Usage on bot-server:
  python3 scripts/bulk-sync-quest-profiles.py \\
    --civs-dir /home/daniel/mineserver/plugins/Civs/players \\
    --auraskills-dir /home/daniel/mineserver/plugins/AuraSkills/userdata \\
    --rpg-dir /home/daniel/mineserver/plugins/RPGServer/players \\
    --quests-dir /home/daniel/mineserver/plugins/RPGServer/quests

Does NOT grant rewards or AuraSkills XP — only updates RPG profile step state.
"""
from __future__ import annotations

import argparse
import sys
from pathlib import Path

try:
    import yaml
except ImportError:
    print("PyYAML required: pip install pyyaml", file=sys.stderr)
    sys.exit(1)


def load_yaml(path: Path) -> dict:
    if not path.exists():
        return {}
    with path.open(encoding="utf-8") as handle:
        data = yaml.safe_load(handle)
    return data if isinstance(data, dict) else {}


def save_yaml(path: Path, data: dict) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    with path.open("w", encoding="utf-8") as handle:
        yaml.dump(data, handle, default_flow_style=False, allow_unicode=True, sort_keys=False)


def aura_level(aura_data: dict, skill: str) -> int:
    skills = aura_data.get("skills") or {}
    key = f"auraskills/{skill.lower()}"
    entry = skills.get(key) or {}
    return int(entry.get("level") or 0)


def civs_has_region(civs_data: dict, region: str) -> bool:
    region = region.lower()
    building = (civs_data.get("skills") or {}).get("building") or {}
    if int(building.get(region) or 0) > 0:
        return True
    items = civs_data.get("items") or {}
    return int(items.get(region) or 0) > 0


def civs_mob_kills(civs_data: dict, mob: str) -> int:
    mob = mob.lower()
    total = 0
    skills = civs_data.get("skills") or {}
    for combat in ("sword", "axe"):
        for key, count in (skills.get(combat) or {}).items():
            if key.lower() == mob or mob in key.lower():
                total += int(count or 0)
    return total


def objective_key(quest_id: str, objective_id: str) -> str:
    return f"{quest_id}:{objective_id}"


def is_objective_done(quest: dict, objective: dict, civs: dict, aura: dict) -> bool:
    otype = str(objective.get("type", "")).lower()
    if otype == "build_region":
        return civs_has_region(civs, str(objective.get("region", "")))
    if otype == "skill_level":
        return aura_level(aura, str(objective.get("skill", ""))) >= int(objective.get("level") or 1)
    if otype == "civs_skill_level":
        # Civs internal skill levels need live API; skip in offline script
        return False
    if otype == "kill_mob":
        return civs_mob_kills(civs, str(objective.get("mob", ""))) >= int(objective.get("amount") or 1)
    return False


def sync_profile(profile: dict, quests: list[dict], civs: dict, aura: dict) -> tuple[int, int]:
    completed = set(profile.get("completed-objectives") or [])
    done_quests = set(profile.get("completed-quests") or [])
    started = set(profile.get("started-quests") or [])
    active = set(profile.get("active-quests") or [])
    objectives_done = 0
    quests_done = 0

    for quest in quests:
        qid = quest["id"]
        if qid in done_quests:
            continue
        requires = quest.get("requires") or []
        if any(req not in done_quests for req in requires):
            continue
        if qid not in started and qid not in active and not any(
            objective_key(qid, o["id"]) in completed for o in (quest.get("objectives") or [])
        ):
            continue
        started.add(qid)
        all_done = True
        for objective in quest.get("objectives") or []:
            oid = objective["id"]
            key = objective_key(qid, oid)
            if key in completed:
                continue
            if is_objective_done(quest, objective, civs, aura):
                completed.add(key)
                objectives_done += 1
                archetype = quest.get("archetype")
                if archetype and not profile.get("archetype"):
                    profile["archetype"] = archetype
                active.add(qid)
            else:
                all_done = False
        if all_done and (quest.get("objectives") or []):
            done_quests.add(qid)
            quests_done += 1

    profile["completed-objectives"] = sorted(completed)
    profile["completed-quests"] = sorted(done_quests)
    profile["started-quests"] = sorted(started)
    profile["active-quests"] = sorted(active)
    return objectives_done, quests_done


def main() -> int:
    parser = argparse.ArgumentParser(description="Bulk-sync RPG quest profiles from Civs/AuraSkills YAML")
    parser.add_argument("--civs-dir", type=Path, required=True)
    parser.add_argument("--auraskills-dir", type=Path, required=True)
    parser.add_argument("--rpg-dir", type=Path, required=True)
    parser.add_argument("--quests-dir", type=Path, required=True)
    parser.add_argument("--dry-run", action="store_true")
    args = parser.parse_args()

    quests = []
    for quest_file in sorted(args.quests_dir.glob("*.yml")):
        data = load_yaml(quest_file)
        if data.get("id"):
            quests.append(data)

    total_o = total_q = 0
    for civs_file in sorted(args.civs_dir.glob("*.yml")):
        uuid = civs_file.stem
        civs = load_yaml(civs_file)
        aura = load_yaml(args.auraskills_dir / f"{uuid}.yml")
        rpg_path = args.rpg_dir / f"{uuid}.yml"
        profile = load_yaml(rpg_path)
        if not profile:
            profile = {"uuid": uuid}
        o, q = sync_profile(profile, quests, civs, aura)
        if o or q:
            print(f"{uuid}: +{o} objectives, +{q} quests")
            total_o += o
            total_q += q
            if not args.dry_run:
                save_yaml(rpg_path, profile)

    print(f"Done: {total_o} objectives, {total_q} quests across {len(list(args.civs_dir.glob('*.yml')))} Civs profiles")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
