# Backgammon Legacy v1.26.0 — Starter Board Cleanup

This release fixes the visual defects reported from the v1.25 on-device gameplay screen without changing rules, AI, progression or the locked v1.23 BoardMap.

- Rebuilt the default **Classic Wood** LIGHT/DARK checker sprites from one shared geometry. Both sides now have identical top-down size, rim shape and transparent bounds.
- Rebuilt matching edge-on LIGHT/DARK sprites for the right bear-off tray using the same wood design language.
- Removed repeating point textures from the starter Classic Burgundy board. The cream/burgundy points now use simple restrained gradients and one quiet seam, eliminating the unwanted diamond/pattern artefacts.
- Kept the realistic black field, walnut frame, burgundy trays and approved emerald cloth background.
- Re-measured the bottom-deck button wells from `premium_bottom_deck.webp` and snapped Roll/End Turn, Undo and Hint to those exact well bounds so the controls no longer float low or outside their slots.
- Dice well coordinates remain the v1.22 asset-derived positions.
- v1.23 point-grid/checker/touch geometry remains locked.

GitHub APK: `Backgammon-Legacy-v1.26.0.apk`
