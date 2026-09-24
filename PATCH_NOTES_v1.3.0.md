# Backgammon Legacy v1.3.0 — Restoration + Rendering Foundation

This build is based on the actual **v1.2.0 source**, not the older prototype branch. It preserves the Backgammon Legacy branding, launcher icon, main menu, Store, Customise, Profile, AI, opening roll, dice animation, move animation, two-dice move indicator and existing gameplay.

## Restored / preserved from v1.2.0
- Backgammon Legacy app name and branding.
- Existing launcher icon and splash branding.
- Main menu with Vs Bot, 2-Player, Store, Customise and Profile.
- Store / Customise / Profile screens.
- Vs Bot and local 2-player gameplay.
- Random dice rolls for both human and AI.
- Opening roll to decide first player.
- Smooth checker and dice animation.
- Undo / End Turn flow.
- Green single-die destination, gold `2` two-dice destination, orange capture and blue selected-checker highlighting.
- Three board, checker and movement-animation cosmetic choices.

## New permanent board architecture
- Added one invisible `BoardMap` that owns all 24 point anchors, bar, bear-off areas, hit regions and movement endpoints.
- Board cosmetics no longer provide gameplay coordinates.
- Every production board is exactly **1860 × 1000 px** and uses the same master point geometry.
- Runtime validation rejects a board image with the wrong production dimensions rather than silently misaligning pieces.
- Classic Walnut, Aegean Marble and Midnight Teal now use one static `board.webp` image each underneath the invisible map.
- Added **Show Board Map** to the in-game menu for alignment QA.

## Checker consistency
- Checker artwork is normalised to one renderer-controlled diameter.
- Transparent padding is trimmed at runtime before a checker is mapped into the standard render square.
- All checker themes now use the same logical size, stack spacing, bar size and preview size.
- Up to five physical checkers are displayed on a point; 6+ uses the existing count badge.

## Customise / Store previews
- Board and checker previews now use the same BoardMap and checker renderer as gameplay instead of manually positioned preview artwork.
- This removes the preview alignment issue where checkers did not sit on the same point centres as the real board.
- Store preview cards also use the shared board renderer.

## Visual / layout polish
- Rebuilt the three board assets against the same master geometry.
- Replaced the large low-quality gameplay filler area with the darker premium tabletop surface.
- Main menu has been compacted vertically so the bottom controls no longer clip on shorter landscape displays.
- Back controls now use a scalable vector chevron instead of the pixelated bitmap arrow.
- Coin/header controls were tightened to better match the premium UI.

## Match setup foundation
After selecting **Vs Bot** or **2-Player**, a new Match Setup screen now supports:
- AI difficulty: Easy / Normal / Hard.
- Player 1 direction: clockwise or counter-clockwise.
- Player 1 visual checker colour: Light or Dark.
- Match length: Single Game / First to 3 / First to 5 / First to 7.
- Match score is tracked across games and `Next Game` starts the next game in the match.

The setup screen also shows the future regional rulesets **Mahbouseh** and **Tawle 31**. They are deliberately marked as not playable in v1.3.0; selecting them will not start a match until their separate rules logic is implemented. Standard Backgammon / Sheish Beish remains the playable ruleset in this build.

## AI difficulty
- Easy uses the same legal-move evaluator with significantly more decision noise.
- Normal uses the established positional AI behaviour.
- Hard removes the random tie/noise component so it consistently takes the evaluator's highest-scoring line.
- Dice remain random at every difficulty.

## GitHub upload package
- Project consolidated to stay below the browser upload limit.
- Redundant density launcher assets, obsolete board layers and duplicate preview graphics were removed.
- GitHub Actions artifact name: **Backgammon-Legacy-v1.3.0-APK**.
