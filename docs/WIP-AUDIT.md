# RPG / civs-quests — WIP audit (2026-07-17)

Living map from branch/PR/issue scan **plus commit archaeology**. Issues alone under-report older WIP; commits are the source of truth.

## Snapshot

| Item | State |
|------|--------|
| `origin/master` | `0f32bfc` (PR #64 SoftHook merged) |
| Open PR | [#65](https://github.com/Daniel730/civs-quests/pull/65) `cursor/fix-hud-hub-menus` — **hearts-slot being abandoned**; split keep vs drop |
| Local dirty | `mineserver-control-remote.sh` (likely CRLF), untracked `_patch_*` / `_redeploy_*` live helpers |
| Stashes | 5 — error-reporting already on master; sprint leftovers docs-only |

## Branch tips vs master

| Branch | Ahead | Verdict |
|--------|-------|---------|
| `cursor/fix-hud-hub-menus` | 7 | **Split**: keep hub Magias + transient HUD coexistence; **abandon** hearts-slot pack/`HeartsSlotHudComposer`/`HideHeartsPackService` |
| `cursor/fix-civs-integration-sweep` | 0 | Merged (#64) — delete remote/local |
| `sprint-2-3/rpg-features`, `sprint-3/rpg-features` | 0 | Merged — delete |

## Committed but unfinished (no issue)

| Commit(s) | Date | Summary | Recommend |
|-----------|------|---------|-----------|
| `b8c4f6e` → `925d703` → **`35dd633` deleted** | 2026-07-03→04 | `InteractiveBooksHook` + `QuestBookService` added then removed for Player Hub | **Abandon code path**; **fix docs** (README/skills still claim IB hook exists). Optional reuse: restore grant-only hook later |
| `696536a`, `35dd633` | 2026-07-04 | Hub replaces written-book guide | **Done** on master — docs drift only |
| `3bb9729` (#64) | 2026-07-16 | SoftHookFactory for AuraSkills/LuckPerms | **Done** — README still says hard runtime deps; update |
| `d83d38b`…`45516a3` (PR #65) | 2026-07-16 | Composed HUD → hearts-slot bitmap experiment | **Abandon/simplify** hearts-slot; optionally keep ActionBar composed + quest chat pulses |
| `04859c8` | 2026-07-16 | Hub Magias opens class menu | **Finish/keep** (independent of HUD) |
| Config: `settings.debug`, `require-town-for-quests`, overlapping AuraSkills sync keys | various | Loaded or documented, not wired | **Park** (product decision) |
| `scripts/wsl-deploy-questbook.sh` | — | Deploy script for deleted book flow | **Delete or archive** |

## Issues

- Closed obsolete: #66 (hearts-slot tracker).
- Open `server-error` (#34, #41, #47, #49–#51 + remaining dups): Civs-side stacks from 1.11.6 jar; several fixed on Civs master — verify after 1.11.7 deploy, then close.

## Cleanup recommendations (do not force-delete without review)

```bash
# after confirming squash content on master
git push origin --delete cursor/fix-civs-integration-sweep
git branch -d cursor/fix-civs-integration-sweep sprint-2-3/rpg-features sprint-3/rpg-features

# PR #65: prefer close or retarget after hearts revert; cherry-pick 04859c8 / transient-channel if needed
gh pr close 65  # only after parallel revert lands or superseding PR exists

git stash drop  # only after reviewing stash@{2..4} (docs leftovers)
```
