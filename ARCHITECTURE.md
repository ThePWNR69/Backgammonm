# Backgammon v0.7 architecture

The project is now split by responsibility so future content can grow without turning the board view into a monolith.

## `game/`
Pure backgammon domain code. No Android/UI dependencies.

- `BackgammonGame` — match/turn state, dice state, undo snapshots and win state.
- `RulesEngine` — legal-move enumeration, higher-die/max-dice rules, bar entry and bearing off.
- `GameState` — position snapshot.
- `Move` — immutable move value object.

## `ai/`
AI is a strategy plug-in.

- `AiStrategy` — contract used by the app.
- `PositionalAi` — current normal AI.

Future `EasyAi`, `HardAi`, `AggressiveBossAi`, etc. can be added without changing `BackgammonGame`.

## `cosmetics/`
Data model for visual content and unlocking.

- `BoardTheme`
- `CheckerTheme`
- `DiceTheme`
- `HighlightTheme`
- `PlayerLoadout`
- `CosmeticItem`, `CosmeticCategory`, `UnlockRule`
- `CosmeticCatalog`

A cosmetic entry can be default, level+coin gated, achievement gated, or boss gated.

## `animation/`
Movement effects are interchangeable.

- `MoveAnimationStyle` — timing/easing/arc contract.
- `SmoothSlideAnimation` — equipped default.
- `FastSnapAnimation` — sample alternate.
- `RollAndFallAnimation` — reserved premium style matching the planned rolling checker concept.
- `AnimationCatalog`

The renderer reads the equipped animation from `PlayerLoadout`; game rules do not know which animation is used.

## `rendering/`
Android drawing and interaction only.

- `BackgammonBoardView` — touch input, dynamic composition, move/dice animation orchestration.
- `BoardGeometry` — screen-independent board geometry.
- `StaticBoardRenderer` — immutable board skin drawing, cached to a bitmap.
- `AssetTextureRepository` — loads/caches optional production artwork.

The static renderer accepts theme data. If art files exist under `assets/cosmetics`, it uses them; otherwise it falls back to procedural drawing.

## `progression/`
- `PlayerProgress` — level, coins, achievement/boss flags and cosmetic ownership/unlock checks. Persistence/UI intentionally comes later.

## `boss/`
- `BossDefinition` — links a boss identity to an AI strategy, visual theme and reward cosmetics.
- `BossCatalog` — future campaign registry.

## `ui/`
- `MainActivity` — Android screen/controller only.

## Content workflow later

Adding a checker set should become:

1. Add light/dark artwork under `assets/cosmetics/checkers/<id>/`.
2. Add one `CheckerTheme` and `CosmeticItem` entry.
3. Set its unlock rule (level/coins, achievement or boss).

Adding a board is the same pattern with board layers. Adding a movement animation means implementing `MoveAnimationStyle` (or later a richer renderer-backed effect) and registering it in `AnimationCatalog`.


## v0.7 Graphics Lab
The menu now exposes three boards, three checker sets and three movement styles. The selected IDs are stored in SharedPreferences and rebuilt into `PlayerLoadout` at launch. These are showcase/testing choices; normal progression locks can be re-enabled later without changing the renderer.

## v0.9 locked rendering foundation

`BoardMap` is now the authoritative invisible board. `BoardGeometry` projects it to the device. Production board skins are static 1860×1000 images underneath that map. Theme artwork never owns point positions or checker sizes.

All future board/checker additions must follow `DESIGN_STANDARDS.md` and the files in `production_templates/`.
