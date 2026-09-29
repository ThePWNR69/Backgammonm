# Backgammon Legacy v1.14.0 — Classic Burgundy Final Implementation

This pass implements the approved Classic Burgundy preview as the real gameplay visual target rather than only storing it as a reference.

- Rebuilt the Classic Burgundy board artwork from the locked BoardMap geometry with a straight, symmetrical walnut frame.
- Removed the perspective/skewed right-hand frame and hard cut-out appearance of the old board artwork.
- Added rounded transparent board corners so the existing tabletop shadow visually integrates the physical board into the background.
- Rebuilt the player plaques with score bays sized to the live score TextViews.
- Rebuilt the gold turn plaque, burgundy bottom deck, recessed dice wells, primary action button, secondary buttons and square controls.
- Normal / Pressed / Disabled button states remain separate.
- Adjusted only the final gameplay projection to 506 x 213 logical units to better match the approved board proportions while preserving the permanent BoardMap.
- Backgammon, Mahbouseh, Tawla 31, AI, hints, XP and Gold are unchanged.
- GitHub output: `Backgammon-Legacy-v1.14.0.apk`.
