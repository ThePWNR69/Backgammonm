# Backgammon Legacy v1.5.0 — Premium UI Consistency Release

This release keeps the v1.4.4 triangle-centred checker geometry and gameplay behaviour, while applying the approved walnut / emerald / gold visual language across the rest of the app.

## Gameplay HUD
- Rebuilt the two player nameplates to match the approved clean design.
- Removed OFF, BAR, colour and direction text from the top HUD.
- Added a dedicated rounds-won badge to each player nameplate.
- Scores are always visible, including single-game matches (0–1 at match end).
- Match-end centre text is cleaner: e.g. `AI Wins Match` rather than repeating the score already shown in the badges.
- Kept the selected checker artwork as the player identity icon.
- Simplified the Undo control to a single-line compact treatment.

## Match setup
- Rebuilt both VS Bot Setup and 2-Player Setup around the approved two-panel design.
- Dark walnut/leather settings panel on the left.
- Match Rules panel on the right.
- Gold icon badges and warm cream labels.
- Emerald/gold choice fields with readable cream text instead of black Android default spinner text.
- Large emerald `START MATCH` action.
- Responsive compact row heights to avoid bottom clipping on landscape phones.
- Existing functional choices are preserved: game version, difficulty (bot only), Player 1 direction, Player 1 colour and match length.

## App-wide visual consistency
- Main Menu now uses the same dark emerald tabletop, walnut/gold framed menu panel and emerald controls.
- Store, Customise and Profile now use the same premium tabletop background and strengthened walnut/gold card system.
- Back buttons, tabs, cards, coin chip and secondary controls have been restyled to a shared premium treatment.
- Muted text on Store / Customise / Profile was warmed toward cream/gold for better readability and consistency.

## Preserved from v1.4.4
- Triangle-edge midpoint checker placement.
- Exact 24-point BoardMap geometry.
- Fixed checker sizing rules.
- Shared gameplay / Store / Customise board renderer.
- AI, opening roll, Undo, move animation, legal-move highlighting and match scoring.
- Backgammon Legacy app name, icon and branding.

## Build / GitHub
- Version: `1.5.0`
- Version code: `18`
- GitHub Actions artifact: `Backgammon-Legacy-v1.5.0-APK`
- Compact source package remains below the 100-file browser upload limit.
