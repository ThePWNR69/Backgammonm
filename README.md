# Backgammon Android — v0.2 Visual Polish Build

A native Android backgammon prototype focused on a polished top-down wooden-board presentation and a safer, reversible turn flow.

## v0.2 changes
- Premium top-down walnut + dark leather board styling
- Ivory and walnut checker styling with depth/highlights
- Blue glow around the selected checker
- Green glowing rings only on legal landing positions for that selected checker
- Brief red X feedback when a selected checker is tapped toward an illegal destination
- Dice rendered directly on the board
- Symmetrical Player 1 / Player 2 header panels
- New **Undo** button for moves made during the current unconfirmed turn
- New **End Turn** confirmation button
- A move is provisional until End Turn is pressed
- Multiple moves in the current turn can be undone one-by-one
- End Turn is rules-safe: it only enables once all legally required dice have been used or no legal move remains
- A final bear-off can still be undone until End Turn commits the win

## Existing game rules
- Standard 24-point starting layout
- Local two-player / pass-and-play
- Dice rolls and doubles
- Legal move filtering
- Forced bar re-entry
- Hitting blots
- Bearing off including oversized-die rule
- Maximum-dice rule and higher-die rule when only one die can be played
- Win detection after turn confirmation

## Interaction
1. Tap **Roll Dice**.
2. Tap a checker that has a legal move. It gets a blue glow.
3. Legal destinations for that checker appear as green glowing rings.
4. Tap a green destination to make the provisional move.
5. Continue until all legally required dice have been used.
6. Tap **Undo** to reverse provisional moves if you want to change the turn.
7. Tap **End Turn** to commit the turn and hand play to the other player.

## Build
Push the project to GitHub and run **Actions → Build Android APK**. The included workflow uploads an artifact named `Backgammon-v0.2-APK` containing `app-debug.apk`.

Project settings:
- Package: `com.george.backgammon`
- minSdk 24
- target/compileSdk 35
- Landscape orientation
