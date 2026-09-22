# Backgammon Android — v0.6 Architecture Overhaul

v0.6 is primarily an internal rebuild so the game can grow into the larger project planned: many purchasable checker sets, level locks, achievement boards, boss-exclusive cosmetic sets, different dice, move highlights and collectible movement animations.

## What changed

### Game rules are isolated
`game/RulesEngine` now owns legal backgammon movement. `BackgammonGame` owns the match/turn state and Undo snapshots. Neither depends on Android graphics, AI or cosmetics.

### AI is modular
The current computer player is now `ai/PositionalAi`, implementing the `AiStrategy` contract. Future Easy, Hard, Expert and boss personalities can be added without changing the rules engine.

### Cosmetics are data-driven
Boards, checkers, dice, highlights and movement animations are represented by theme/loadout objects. `CosmeticCatalog` is the central registry.

The unlock model already supports:
- default/free items
- level + coin shop items
- achievement rewards
- boss rewards

The sample catalogue includes the planned Classic Walnut, Aegean Marble and Midnight Teal board definitions, Ivory/Walnut and Marble/Bronze checker definitions, and the future Legendary Roll & Fall animation slot. Only the current default visuals are equipped in gameplay; these extra entries are architecture examples for future content.

### Production artwork can be dropped in
The renderer now has an `AssetTextureRepository`. It looks under:

`app/src/main/assets/cosmetics/`

for optional board/checker/dice artwork. Missing art safely falls back to the procedural graphics, so visual assets can be replaced gradually without breaking the game.

See `app/src/main/assets/cosmetics/README.md` for exact filenames.

### Movement animations are swappable
`MoveAnimationStyle` controls duration, easing, lift and arc. The default is Smooth Slide. Fast Snap and the future Roll & Fall style are registered separately to prove the game no longer hard-codes one movement animation into the rules.

### Rendering is more focused
`BackgammonBoardView` is no longer responsible for the full static board skin. `BoardGeometry` owns proportions and `StaticBoardRenderer` owns immutable board artwork, which remains cached for animation performance.

### Future progression/boss hooks exist
`PlayerProgress` keeps level/coins/achievement/boss unlock logic separate from gameplay. `BossDefinition`/`BossCatalog` show how a boss can have its own AI, board/checker identity and exclusive reward cosmetics later.

## Existing gameplay retained

- opening roll to determine first player
- proper legal movement / higher-die rules
- bar entry, hits and bearing off
- Undo before End Turn
- Roll Dice ↔ End Turn main button flow
- dice animation
- smooth checker animation
- local two-player mode
- current positional AI
- 5-visible-checker stack cap with count badge
- move highlights and occupied-stack landing preview
- optional FPS overlay / hardware-accelerated cached rendering

## Validation

The pure Java rules/AI/progression/cosmetic architecture compiles independently of Android and was stress-tested after the refactor across 200 complete AI games, more than 30,000 moves and hundreds of Undo operations while checking that both players always retain exactly 15 checkers.

## Build

Upload/replace the files in the GitHub repository and run:

**Actions → Build Android APK → Run workflow**

The artifact is named **Backgammon-v0.6-APK**.

See `ARCHITECTURE.md` for the package map and future content workflow.
