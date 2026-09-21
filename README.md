# Backgammon Android — v0.3 Board & Movement Polish

A native Android backgammon prototype focused on making the board the main visual focus while improving checker scale, move feedback and turn feel.

## v0.3 changes
- Compact interface: thinner player header and a much smaller bottom control strip
- One central action button now switches **Roll Dice → End Turn → Roll Dice**
- **New Game** moved into the ☰ menu
- Larger checkers based on real-board proportions
- A point shows no more than **5 physical checkers**; stacks of 6+ show a small count badge on the fifth checker
- Five visible checker positions are fitted to the height of the point/spike
- Blue glow remains the selected-checker state
- Empty legal destinations use a green ring
- Legal destinations containing your own checkers use a translucent **ghost checker** landing preview
- Full 5+ stacks use a green ring plus a **+1** badge so the landing option cannot be hidden by the stack
- Single opposing blots use an orange capture highlight
- Invalid taps briefly show a red X
- No movement arrows/lines
- Checker moves now animate with a smooth lift/slide/settle instead of teleporting
- Hits animate the opposing checker toward the bar
- Undo animates the move back before restoring the previous provisional state
- Board drawing keeps a more physical-board-like aspect ratio instead of stretching to fill the entire phone width

## Turn flow
1. Tap **Roll Dice**. The same centre button becomes **End Turn**.
2. Select a movable checker. It gets a blue glow.
3. Legal destinations appear using the appropriate green/ghost/capture indicator.
4. Tap a legal destination. The checker animates there.
5. Use **Undo** any time before committing the turn.
6. **End Turn** only enables once all legally required dice have been used or no legal move remains.
7. After End Turn, the centre button becomes **Roll Dice** for the next player.

## Existing rules
- Standard 24-point starting layout
- Local two-player / pass-and-play
- Dice rolls and doubles
- Legal move filtering
- Forced bar re-entry
- Hitting blots
- Bearing off including oversized-die rule
- Maximum-dice rule and higher-die rule when only one die can be played
- Win detection after turn confirmation

## Build
Push the project to GitHub and run **Actions → Build Android APK**. The included workflow uploads an artifact named `Backgammon-v0.3-APK` containing `app-debug.apk`.

Project settings:
- Package: `com.george.backgammon`
- Version: 0.3.0
- minSdk 24
- target/compileSdk 35
- Landscape orientation
