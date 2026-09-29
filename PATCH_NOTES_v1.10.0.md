# Backgammon Legacy v1.10.0 — Approved Gameplay Visual Lock

This release is a corrective visual pass built directly from `design/reference/CLASSIC_BURGUNDY_APPROVED.png` rather than a loose interpretation of the concept.

- Locks the live gameplay screen to one 510×280 logical reference frame.
- Uses the measured rectangles in `APPROVED_GAMEPLAY_LAYOUT.md` for every HUD, board and control element.
- Uses the dark emerald tabletop artwork as the live gameplay background.
- Introduces production Classic Burgundy raster chrome for the left/right player plaques, gold turn plaque, bottom control deck and buttons.
- Uses separate mirrored player plaques so the score bays match the approved concept.
- Primary and secondary controls use raster Normal / Pressed / Disabled states; pressed controls visibly sink without glow.
- Menu and Settings use matching square Normal / Pressed states.
- Dice live in the bottom-left control deck, including during the roll animation; they are no longer drawn over the board.
- Roll and End Turn use the same fixed main-action rectangle.
- Hint keeps its reserved rectangle when disabled so the layout never reflows.
- Right-side bear-off progress remains a single two-direction edge-on stack.
- Preserves Backgammon, Mahbouseh, Tawla 31, AI, hints, XP and Gold systems from v1.8/v1.9.
- Keeps the 2048×977 canonical BoardMap/source board assets and v1.6.5 checker-size rule.
- GitHub artifact/APK name: `Backgammon-Legacy-v1.10.0.apk`.
