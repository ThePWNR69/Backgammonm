# Backgammon Legacy v1.7.0 — Premium Gameplay HUD

## Efficient modular update
This release is a targeted presentation/UI patch on top of v1.6.5. The game rules, AI architecture, canonical BoardMap, point coordinates, touch regions and locked checker geometry are unchanged.

## New gameplay presentation
- Added the approved burgundy + restrained gold gameplay chrome.
- Kept the established top layout: Player 1 on the left, scores around the central turn-status plaque, and Player 2 / AI difficulty on the right.
- AI header now displays the selected difficulty (Beginner / Normal / Hard).
- Moved the menu control to the top-left of the gameplay HUD.
- Rebuilt the bottom control strip around the approved concept.
- Dice results now display at the bottom-left during normal turns instead of duplicating a second dice display on the board.
- The main action uses one location and changes from Roll / Roll Again to End Turn as the turn progresses.
- Replaced the bright green action styling with a burgundy action treatment so it belongs to the board instead of overpowering it.
- Undo and Hint now sit on the bottom-right in restrained brown/gold controls.
- Added a functional Hint button. It highlights the source checker for a currently legal move without altering game state.

## Bear-off progress tray
- The existing canonical right-side bear-off area remains the gameplay target.
- Replaced the old OFF count markers with edge-on checker segments.
- White borne-off pieces fill from the top downward.
- Dark borne-off pieces fill from the bottom upward.
- Both stacks use the same segment dimensions and visually progress toward the centre.
- No second bear-off tray was added on the left.

## Preserved from v1.6.5
- 90% checker-diameter-to-point-base rule.
- Five-checker stack reaches the triangle apex.
- Canonical 24-point BoardMap and touch geometry.
- 1860 × 1000 board cosmetic standard.
- Existing board/checker cosmetic architecture.
- Opening roll, AI, undo, movement animation and match scoring.

## Version
Version code: **25**
Version name: **1.7.0**
GitHub Actions artifact: `Backgammon-Legacy-v1.7.0-APK`
