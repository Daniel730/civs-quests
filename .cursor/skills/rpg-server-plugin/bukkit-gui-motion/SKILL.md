---
name: bukkit-gui-motion
description: >-
  Apply design-motion-principles to Paper/Bukkit inventory GUIs in RPG Server
  and Civs. Use when editing PlayerHubGui, QuestJournalGui, SkillTreeGui,
  CodexGui, RebirthGui, or any inventory open/render path.
---

# Bukkit GUI motion

1. Read global skill `design-motion-principles` (Emil primary for Hub).
2. Follow [docs/bukkit-gui-motion.md](../../../../docs/bukkit-gui-motion.md).
3. Prefer `dev.daniel730.rpgserver.gui.GuiMotion` once present; do not invent per-GUI timers.
4. Respect `ui.motion.reduced` / `MotionProfile.INSTANT`.
5. Cancel tasks on inventory close and before re-render.
