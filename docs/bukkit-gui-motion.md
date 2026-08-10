# Bukkit GUI motion contract

Adapts [design-motion-principles](https://github.com/kylezantos/design-motion-principles) to Paper inventory UIs (`PlayerHubGui`, `QuestJournalGui`, `SkillTreeGui`, `CodexGui`, `RebirthGui`, Civs menus).

## Weighting

| Lens | Role here |
|------|-----------|
| Emil Kowalski | **Primary** — Hub/tabs are high-frequency; keep motion short or off |
| Jakub Krehel | Secondary polish on first open / rare screens |
| Jhey Tompkins | Selective only (e.g. first-time path picker) |

## Required behaviours

1. **Skeleton** — On open, show border + grey/light placeholder panes in content slots before real items.
2. **Lazy load** — Populate content via `BukkitRunnable` / scheduler ticks; never block the main thread with heavy sync work inside `open()`.
3. **Enter** — Stagger content slots (e.g. 1–2 slots per tick) with total duration ≤ 8 ticks for Hub; longer only for rare screens.
4. **Exit** — Cancel motion task ids on close/re-render; do not leave runnables leaking.
5. **Progress** — Use existing progress lore/`ProgressBarUtil`; avoid pulsing enchanted items every tick.
6. **Reduced motion** — If `ui.motion.reduced` (or player preference later), skip stagger: one-frame fill.

## API target (`GuiMotion`)

```java
GuiMotion.enter(plugin, inventory, slotsAndItems, MotionProfile.HUB);
GuiMotion.skeleton(inventory, contentSlots, placeholder);
GuiMotion.cancel(holder); // on close / re-render
```

Profiles: `INSTANT`, `HUB` (fast), `RARE` (slightly longer stagger).

## Anti-patterns

- Staggering every Hub tab switch (too frequent).
- Enchantment glint flash loops as “loading”.
- Opening empty inventory for >10 ticks with no skeleton.
- Animating keyboard-driven refreshes (`/rpg reload` admin paths).
