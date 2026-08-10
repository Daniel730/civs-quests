# Agent engineering standards — Civs + RPG

**Audience:** any agent / model working on `Daniel730/civs-quests` (RPG) or `Daniel730/Civs`.  
**Stack reality:** Paper **26.1.2** Java plugins — **not** a web SPA. Map web tooling to Java equivalents below; do not install Biome/Playwright/Knip/Stryker into this repo.

Milestone: [Engineering Standards](https://github.com/Daniel730/civs-quests/milestone/5)

| Issue | Theme |
|-------|--------|
| [#67](https://github.com/Daniel730/civs-quests/issues/67) | Observability (OTel / Sentry / APM agents) |
| [#68](https://github.com/Daniel730/civs-quests/issues/68) | Quality gates (Spotless, ArchUnit, commitlint, PIT) |
| [#69](https://github.com/Daniel730/civs-quests/issues/69) | Tests + JaCoCo/Codecov |
| [#70](https://github.com/Daniel730/civs-quests/issues/70) | GUI motion (skeleton / lazy / enter-exit) |
| [#71](https://github.com/Daniel730/civs-quests/issues/71) | Agent standards doc + issue/PR workflow |
| [#72](https://github.com/Daniel730/civs-quests/issues/72) | CI workflow |

Related: [ARCHITECTURE.md](ARCHITECTURE.md) · [GITHUB-SYNC.md](../.cursor/skills/rpg-server-plugin/GITHUB-SYNC.md) · skill `design-motion-principles` (global Cursor) + [bukkit-gui-motion.md](bukkit-gui-motion.md).

---

## 0. Non-negotiable workflow (issues → PR → deploy)

1. **Every** correction, improvement, or new feature starts as a **GitHub Issue** (labels + milestone).
2. Work happens on a **feature branch**; open a **Pull Request** before merge/deploy.
3. PR description **must** reference the issue: `Fixes #N` / `Closes #N` (cross-repo: `Closes Daniel730/Civs#N`).
4. **Never** deploy production JARs from unreviewed local-only commits when an issue exists — merge (or explicitly approved PR) first when practical.
5. After merge/deploy: comment on the issue with branch, key files, and close if done.
6. Sync both repos per [GITHUB-SYNC.md](../.cursor/skills/rpg-server-plugin/GITHUB-SYNC.md).

### Issue title prefixes

| Prefix | Repo | Example |
|--------|------|---------|
| `RPG-###` | civs-quests | `RPG-070: OpenTelemetry bootstrap` |
| `CIVS-###` | Civs | `CIVS-020: ArchUnit package rules` |

### Labels (create if missing)

`bug` · `enhancement` · `documentation` · `engineering` · `observability` · `quality` · `testing` · `ui-motion` · `P0`–`P2` · `rpg` / `civs`

### PR body template

```markdown
## Summary
- …

## Issue
Fixes #<N>

## Test plan
- [ ] `mvn -B test`
- [ ] …
```

---

## 1. Observability

| Requested (web/APM) | Use in this stack | Notes |
|---------------------|-------------------|--------|
| **OpenTelemetry** | **Primary** — OTel Java API/SDK or javaagent on the JVM hosting Paper | Config-gated; export to collector when available |
| **Sentry** | **Optional** — `io.sentry:sentry` / Sentry Log4j appender | Complements existing `GitHubIssuesReporter` + error-reports |
| **Datadog** | **Optional** — Datadog Java agent on host (not shaded into plugin) | Ops/infra; no API keys in git |
| **New Relic** | **Optional** — New Relic Java agent on host | Same as Datadog |

**Rules**

- Secrets only via env / server `config.yml` (gitignored overrides) — never commit tokens.
- Prefer enriching existing error reporting ([CURSOR-AUTOMATION-ERROR-MONITORING.md](CURSOR-AUTOMATION-ERROR-MONITORING.md)) before adding a second APM.
- Plugin code should expose **structured breadcrumbs** (quest id, player uuid redacted, hook name); agents attach OTel spans around high-value paths (quest complete, reward grant, hub open).

---

## 2. Code quality & lint

| Requested | Java / Maven equivalent | Status target |
|-----------|-------------------------|---------------|
| Biome | **Spotless** (google-java-format) + **Checkstyle** / **PMD** / **SpotBugs** | CI gate |
| Arch-contract | **ArchUnit** tests (`..arch` package) | Unit test phase |
| Commitlint | **Conventional Commits** + GH Action on PR titles/commits | CI |
| Knip | **maven-dependency-plugin** `analyze` + IDE unused inspection | CI warn→error |
| Stryker | **PIT** (pitest-maven) mutation testing | Nightly / opt-in job |

**ArchUnit expectations (RPG)**

- `hook` must not depend on concrete GUI classes.
- `quest` must not call Civs managers for writes (events/API only) — encode as ArchUnit where possible.
- No dependency from `test` production packages into server-only NMS.

---

## 3. Tests & coverage

| Requested | Equivalent | Notes |
|-----------|------------|--------|
| Unit | **JUnit 4/5** + **Mockito** (existing) | Expand per feature PR |
| Integration | **MockBukkit** (or in-process Paper test harness) | Prefer over live server for CI |
| E2E browser (**Playwright**) | **N/A** for Bukkit inventories | Optional only if a future web admin exists |
| Codecov | **JaCoCo** report + Codecov upload in CI | Badge in README when token available |

**GUI “E2E” substitute:** tests that open Hub/Journal holders, assert slot materials/actions, and exercise `GuiMotion` tick sequences without a Minecraft client.

Minimum for merge: `mvn -B test` green. Coverage floors can rise once JaCoCo baseline exists.

---

## 4. UI motion (Bukkit inventories)

Web skill: **`design-motion-principles`** (`kylezantos/design-motion-principles`, installed under `~/.cursor/skills/`).

**Project type weighting for RPG Hub / Civs menus:** productivity tool → **Emil primary**, **Jakub secondary**, Jhey selective (rare delighters only).

Inventory constraints (no CSS/Framer): implement motion as **tick-scheduled slot updates** — see [bukkit-gui-motion.md](bukkit-gui-motion.md).

Every player-facing GUI **must** support:

| Capability | Implementation |
|------------|----------------|
| Skeleton | Placeholder panes while data resolves |
| Lazy loading | Fill content slots over 1–N ticks (not one giant sync hitch) |
| Enter | Short stagger fill (≤ ~8 ticks total for frequent opens) |
| Exit | Cancel tasks; optional brief clear before close |
| Loading / progress | Reuse `ProgressBarUtil` / lore bars; never infinite pulse spam |
| Reduced motion | `config.yml` → `ui.motion.reduced: true` → instant fill |

**Anti-slop (from motion skill):** no decorative animation on every click; no long stagger on Hub tab switches (high frequency → instant or 1-tick).

---

## 5. Hermes / Cursor orchestration

- Prefer **Hermes** (`daniel-hermes-forge` → `hermes_delegate.py`) for surveys, issue drafts, and patch sketches.
- Parent Cursor **reviews** captures under `data/delegations/` before applying.
- Commits / pushes / deploys only when Daniel asks (or explicit workflow step).

---

## 6. Agent checklist (every task)

- [ ] Issue exists (or created) with correct label + milestone  
- [ ] Branch named `rpg/<issue>-short-slug` or `civs/<issue>-short-slug`  
- [ ] PR mentions `Fixes #N`  
- [ ] `mvn -B test` (and format/arch when wired)  
- [ ] GUI changes go through `GuiMotion` / motion doc  
- [ ] No secrets committed  
- [ ] Update sprint brain / this doc if process changed  
- [ ] GitHub sync both repos if Civs touched  

---

## 7. Tooling map (quick reference)

```
Sentry / Datadog / New Relic / OTel  →  OTel (+ optional Sentry SDK); APM agents on JVM host
Biome                               →  Spotless + Checkstyle/PMD/SpotBugs
Arch-contract                       →  ArchUnit
Commitlint                          →  Conventional Commits + GH Action
Knip                                →  maven-dependency-plugin analyze
Stryker                             →  PIT mutation testing
Codecov                             →  JaCoCo + Codecov Action
Playwright                          →  N/A (MockBukkit GUI tests; future web admin only)
Motion principles (GitHub skill)    →  design-motion-principles + docs/bukkit-gui-motion.md
```
