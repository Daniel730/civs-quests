# GitHub issue sync — Civs + RPG

**Repos:** [Daniel730/Civs](https://github.com/Daniel730/Civs/issues) · [Daniel730/civs-quests](https://github.com/Daniel730/civs-quests/issues)  
**gh CLI:** `C:\Users\Danie\tools\gh\bin\gh.exe`  
**Bootstrap script:** `scripts/github-setup-backlog.ps1` (labels, milestones, initial backlog — run once per repo)

---

## When to sync

Run this workflow **after every sprint ticket batch**, **after production deploy**, and **before FINAL-HANDOFF** / session end:

1. List open issues on **both** repos (local work may span Civs + RPG).
2. Create missing issues for completed local work not yet tracked.
3. Close completed issues with a comment linking branch + key files + brain doc.
4. Comment on partial tickets (e.g. CIVS-009 turrets done, shields open).
5. Close sprint milestones when the sprint is done; keep Sprint 3+ open and update description if scope shifted.

Agents: add **“Sync GitHub issues (both repos)”** as the last step of sprint work — see `project-stack/SKILL.md` and `DEPLOY.md` post-deploy checklist.

---

## Issue naming

| Prefix | Repo | Example |
|--------|------|---------|
| `CIVS-###` | Daniel730/Civs | `CIVS-009: Turrets and town shields` |
| `RPG-###` | Daniel730/civs-quests | `RPG-016: Daily/weekly quest content` |

Number in title matches GitHub issue number when possible. New tickets use the next free number in the title (check `gh issue list` first).

**Labels:** `civs` / `rpg`, `sprint-N`, `P0`–`P2`, `enhancement` / `integration`.

**Milestones:** Sprint 1 (closed), Sprint 2 (closed 2026-07-03), Sprint 3 (open), Sprint 4+.

---

## gh commands

```powershell
$gh = "C:\Users\Danie\tools\gh\bin\gh.exe"

# List all issues
& $gh issue list --repo Daniel730/Civs --state all --limit 100
& $gh issue list --repo Daniel730/civs-quests --state all --limit 100

# Create issue
& $gh issue create --repo Daniel730/Civs `
  --title "CIVS-012: Short title" `
  --body "Scope, key files, brain ref." `
  --label "civs,sprint-3,P1,enhancement" `
  --milestone "Sprint 3"

# Close with evidence (preferred over silent close)
& $gh issue close <N> --repo Daniel730/Civs --comment "Done on branch X. Files: Foo.java. Brain: SPRINT-N-STATUS.md."

# Progress comment on partial work
& $gh issue comment <N> --repo Daniel730/Civs --body "MVP done; shields still open."

# Milestones
& $gh api repos/Daniel730/Civs/milestones?state=all --jq '.[] | {number, title, state}'
& $gh api repos/Daniel730/Civs/milestones/<N> -X PATCH -f state=closed
```

---

## Close via PR (preferred after commit)

In PR description or commit message:

```
Closes #16
Closes Daniel730/Civs#9
```

Cross-repo: use full `owner/repo#N` when one PR only closes issues in the other repo.

---

## Current snapshot (2026-07-03 late night)

### Open (prioritize)

| # | Repo | Ticket | Notes |
|---|------|--------|-------|
| 9 | Civs | CIVS-009 | Turret MVP local; town shields open |
| 9 | RPG | RPG-009 | VeinMiner hook stubbed; config default false — deferred |

### Recently closed / fixed this session

| # | Repo | Ticket | Notes |
|---|------|--------|-------|
| — | both | Playtest hot-swap | NoClassDefFoundError — fixed by full-restart deploy (`wsl-deploy-bot-server.sh`) |
| — | RPG | LuckPerms hook | ServicesManager fix; removed Paper library loader |

### Deploy rule (agents)

**Never scp JARs while server is running.** Always `wsl-deploy-bot-server.sh` or stop → scp → start.

### Previously closed

| # | Repo | Ticket |
|---|------|--------|
| 10 | Civs | CIVS-010 Custom mob YAML — closed 2026-07-03 with deploy evidence |
| 11 | Civs | CIVS-011 Smokeshow bugfixes |
| 16 | RPG | RPG-016 Daily/weekly quests |
| 17 | RPG | RPG-017 `/rpg sync` |

Sprint 2 already covered: journal (#12), quest book (#8), `cast_spell` (#15), weekly content (#16).

---

## Agent checklist

- [ ] `gh issue list` both repos
- [ ] Create issues for untracked completed work
- [ ] Close with file/branch comment
- [ ] Partial tickets get progress comment, stay open
- [ ] Milestone state matches sprint brain (`SPRINT-*-STATUS.md`)
- [ ] Update this file’s snapshot if issue numbers changed
