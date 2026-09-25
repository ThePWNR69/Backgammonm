# Backgammon Legacy v1.4.1 — Premium Gameplay Rebuild

This release rebuilds the live match presentation around the premium Walnut reference approved during development, while preserving the v1.3 game systems and branding.

## Premium gameplay screen
- Rebuilt the gameplay composition around a larger physical-board presentation.
- New dark emerald/black luxury tabletop background with restrained edge decoration.
- Reworked top player panels with larger serif names, compact status text and equipped checker icons.
- Reworked central status panel to match the emerald/gold reference treatment.
- Reworked bottom controls so Undo / Roll-End Turn / Menu form one compact centred control group.
- Updated the main emerald action button and dark secondary controls with stronger gold bevels, depth and shadows.

## Classic Walnut board
- Completely replaced the prior Classic Walnut artwork.
- New 2048×992 production board with deeper walnut frame, black leather field, recessed side trays, brass hinges, gold trim and central compass medallion.
- Exactly 24 points: six per quadrant.
- Point geometry is permanently aligned with the invisible BoardMap.
- No checkers or dice are baked into the board image.

## Rendering foundation
- Master board format is now 2048×992 (~2.0645:1) to better match the approved premium reference and use more of a landscape display.
- Updated invisible BoardMap coordinates to match the new master board artwork.
- Rebuilt Aegean Marble and Midnight Teal against the identical master geometry.
- Production board images are rejected if their dimensions do not match the master template.

## Checker consistency
- On-screen checker diameter remains controlled exclusively by BoardGeometry.
- All checker themes remain normalised through the same 512×512 / visible-bounds renderer.
- Header player icons are loaded from the equipped checker set, so the UI reflects the active cosmetic.

## Preserved from v1.3
- Backgammon Legacy app name, branding and launcher icon.
- Main menu, Store, Customise and Profile.
- Vs Bot / 2-Player Match Setup.
- AI difficulty selection.
- Player direction and colour options.
- Match length selection.
- Opening roll, legal-move rules, Undo and End Turn flow.
- Dice/checker animations.
- Green one-die, gold two-step, orange capture and blue selection move language.
- Shared gameplay/Store/Customise cosmetic mapping architecture.

## Developer / QA
- **Show Board Map** remains available from the in-game menu for board-skin alignment testing.
- Package remains below the GitHub browser 100-file limit.
- GitHub Actions artifact: `Backgammon-Legacy-v1.4.1-APK`.
