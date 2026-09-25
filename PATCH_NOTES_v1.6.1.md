# Backgammon Legacy v1.6.1 — Reference Proportion Pass

This release is intentionally limited to the live gameplay presentation. No rules, AI, checker mapping or board geometry logic was changed.

## What changed

- Rebuilt the **screen proportions** against the approved luxury opening-roll reference instead of the full ultrawide phone width.
- Top player/status HUD is now thicker and centred to the physical board width.
- Player nameplates no longer span excessive ultrawide space.
- Score circles reduced from 27dp to **22dp** and remain fully inside their nameplates.
- Checker identity icons reduced to **24dp** so the icon, name, divider and score all sit comfortably inside the panel.
- HUD typography rebalanced for the thicker panels without clipping.
- Added deliberate breathing room between HUD → board and board → controls.
- Bottom Undo / Opening Roll / Menu controls are thicker and use the same proportions as the approved reference.
- Bottom control group now tracks the board width instead of stretching across the whole screen.
- Increased bottom tabletop breathing room to match the approved composition.
- Board artwork, corrected triangle-centred checker placement, dice, gameplay logic and animation behaviour are unchanged from v1.6.0.

## Responsive matching

The gameplay HUD and control row now measure the rendered board at runtime. This means a wide 20:9/22:9 phone keeps the same visual relationship as the approved 16:9-ish reference instead of stretching the UI horizontally.

## Validation target

For the user's 1730×799 gameplay capture, the layout is designed to land within a few pixels of the approved reference after fitting that reference by height:

- HUD target ≈ y 13–73; implementation ≈ y 12–77
- Board target ≈ y 86–665; implementation ≈ y 85–671
- Controls target ≈ y 685–757; implementation ≈ y 683–755
- HUD width and control-group placement are tied mathematically to the board width.

## Build

GitHub Actions artifact: `Backgammon-Legacy-v1.6.1-APK`
