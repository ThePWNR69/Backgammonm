# Backgammon Legacy v1.9.0 — Locked Premium Theme Foundation

- Keeps v1.8.0 rulesets, XP and Gold progression.
- Keeps BoardMap and v1.6.5 checker geometry unchanged.
- Introduces UiTheme + UiThemeCatalog so board themes can own matching HUD/control chrome without gameplay dependencies.
- Classic Burgundy is the first complete UI chrome theme.
- Adds an integrated full-width burgundy/gold bottom control bar.
- Dice, Roll/End Turn, Undo and Hint now sit inside the fixed bottom bar composition.
- Primary and secondary buttons now have Normal / Pressed / Disabled visual states.
- Pressed state visually sinks the button by changing bevel/shadow position; no glow/neon.
- Hints remain fixed-position and become invisible (not reflowed) when disabled.
- GitHub output: Backgammon-Legacy-v1.9.0.apk.

The UI theme layer contains appearance only. BoardMap, touch regions, rules, AI and reward calculations remain independent.
