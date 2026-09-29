# v1.16.0 — Layered Classic Burgundy UI

This update replaces the previous approximation-first gameplay chrome with the approved layered-image system.

## Rendering architecture
- 1672 × 941 approved design is now the master gameplay coordinate system.
- The whole composition scales uniformly to landscape displays.
- Tabletop, top HUD plaques, board, bottom control deck, dice, checkers and button states are separate assets.
- Dynamic text is rendered live by Android using system fonts; no player names, scores, status messages or button labels are baked into the image assets.
- Menu/settings icons remain artwork inside their button assets.
- The board remains a replaceable theme asset beneath the existing gameplay renderer.

## Classic Burgundy assets
- New approved tabletop background.
- New clean Classic Burgundy board artwork with no baked checkers.
- Separate Player 1 / status / Player 2 plaques.
- Separate Menu and Settings button artwork.
- Bottom deck includes its normal button wells and dice wells.
- Roll / Undo / Hint use transparent normal hit targets with image overlays for Pressed and Disabled states.
- Dice and checker pieces remain independent dynamic sprites.

## Gameplay
Backgammon, Mahbouseh, Tawla 31, AI, Hint logic, XP and Gold systems are unchanged.
