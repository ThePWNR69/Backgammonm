# Backgammon Android — v0.4 Dice, Opening Roll & AI

This build keeps the v0.3 board polish and adds the first single-player gameplay layer.

## v0.4 changes
- Added a visible on-board **dice rolling animation** with tumbling/rotation and a short settling phase
- Slowed checker movement from the v0.3 speed to a more deliberate **720 ms** default movement
- Hit animations take slightly longer so both the moving checker and captured checker are readable
- Added a first-pass **single-player AI**
- Default game mode is now **You vs AI**; the menu can switch back to **2 Players**
- AI uses the same legal-move engine as the player and respects bar entry, forced dice usage, higher-die rule, doubles and bearing off
- AI has a lightweight positional evaluator that favours hits, safe points, home-board strength, fewer blots and efficient racing/bearing off
- Added the standard **opening roll**:
  - Player 1/You rolls one die
  - Player 2/AI rolls one die
  - Ties roll again
  - Higher die starts
  - The two opening dice become the first turn's dice
- AI turns roll automatically and use the same checker movement animation as human turns
- Human Undo remains available until End Turn; AI turns are not undoable

## Existing v0.3 polish retained
- Compact interface so the board remains the main visual focus
- One central action button switches **Roll Dice → End Turn → Roll Dice**
- New Game is inside the ☰ menu
- Real-board-inspired checker scale
- Maximum 5 physically drawn checkers per point; 6+ uses a count badge
- Blue selected-checker glow
- Green legal destinations
- Ghost-checker landing preview for occupied friendly points
- Orange capture highlight for opponent blots
- Red invalid-move feedback
- Animated checker movement, hits and Undo

## Game modes
The ☰ menu contains:
- **New Game**
- **Switch to 2 Players / Play vs AI**
- **About v0.4**

In single player, You are Ivory/White and the AI is Walnut/Black.

## Build
Push the project files to GitHub and run **Actions → Build Android APK**.
The workflow uploads an artifact named `Backgammon-v0.4-APK` containing `app-debug.apk`.

Project settings:
- Package: `com.george.backgammon`
- Version: 0.4.0
- minSdk 24
- target/compileSdk 35
- Landscape orientation
