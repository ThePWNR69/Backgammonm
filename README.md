# Backgammon Android — v0.1

A simple native Android backgammon prototype built as a clean base for expansion.

## Included
- Standard 24-point starting layout
- Local two-player/pass-and-play
- Dice rolls and doubles
- Legal move filtering
- Forced bar re-entry
- Hitting blots
- Bearing off, including oversized-die rule
- Uses as many dice as legally possible; when only one of two different dice can be played, uses the higher die
- Win detection
- Tap a checker, then tap a green legal destination
- Tap the OFF tray on the right to bear off

## Build
Open the folder in Android Studio and choose **Build > Build APK(s)**.

Project settings:
- Package: `com.george.backgammon`
- minSdk 24
- target/compileSdk 35
- Landscape orientation

## Good next additions
1. Single-player AI with difficulty levels
2. Opening-roll rule and doubling cube
3. Match scoring / Crawford rule
4. Undo and move confirmation
5. Animations, sound and haptics
6. Portrait/tablet responsive layout
7. Online multiplayer
8. Player profiles, stats and achievements
9. Board/checker themes
10. Rule/tutorial overlay

## Automated APK build
A GitHub Actions workflow is included at `.github/workflows/build-apk.yml`. When the project is pushed to a GitHub repository, it can build and upload `app-debug.apk` as a workflow artifact.
