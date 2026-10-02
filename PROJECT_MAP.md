# Backgammon Legacy project map

Use this map before editing so future updates stay targeted.

- `game/` — rules/state/moves only. Contains Backgammon, Mahbouseh and Tawla 31 domain behaviour. No UI resources.
- `ai/` — AI strategies and move selection.
- `progression/` — persistent XP/Gold and match reward calculation.
- `cosmetics/` — board/checker/dice/UI-theme registries and unlock metadata.
- `rendering/` — BoardMap, BoardGeometry, board/checker rendering, hit regions and animations.
- `animation/` — interchangeable checker movement styles.
- `boss/` — boss definitions/catalog for future progression.
- `ui/` — activities and screen orchestration. `GameActivity` owns the 510×280 approved gameplay composition (v1.11 size lock; v1.12 detailed chrome) but not game rules.
- `res/drawable-nodpi/premium_*` — Classic Burgundy gameplay chrome assets and state artwork.
- `assets/cosmetics/boards/` — canonical 2048×977 production board images.
- `design/reference/CLASSIC_BURGUNDY_APPROVED.png` — Classic Burgundy material/style source.
- `design/reference/V1_11_RUNTIME_COMPOSITION.png` — original slim-HUD runtime proportion reference.
- `design/reference/V1_12_SIZE_REFERENCE.jpg` — accepted in-game sizing reference for future visual polish.
- `APPROVED_GAMEPLAY_LAYOUT.md` — locked coordinates/proportions measured from that reference.

For visual-only gameplay updates, normally edit only `cosmetics/`, `ui/GameActivity`, relevant `res/drawable*` assets, and the selected board asset. Do not touch `game/`, `progression/`, AI or BoardMap unless the requested feature actually changes them.


## v1.14 Classic Burgundy final implementation
- Approved visual: `design/reference/CLASSIC_BURGUNDY_FINAL_APPROVED.png`
- Gameplay chrome assets: `app/src/main/res/drawable-nodpi/premium_*`
- Classic board art: `app/src/main/assets/cosmetics/boards/classic_burgundy/board.webp`
- Runtime placement: `ui/GameActivity.applyReferenceComposition()`
- Board logic remains in `rendering/BoardMap.java` and `BoardGeometry.java`.


## v1.15.0 fidelity lock
- Classic Burgundy board is authored from the permanent BoardMap geometry using material references from the approved preview.
- Runtime chrome uses dedicated texture assets in drawable-nodpi; gameplay sizing remains unchanged.
- Preview-first validation is required before future visual packaging.

## v1.16.0 layered gameplay presentation
- `design/reference/CLASSIC_BURGUNDY_APPROVED.png` — final visual reference.
- `design/reference/CLASSIC_BURGUNDY_LAYERED_ASSETS.png` — approved separated asset specification.
- `design/reference/CLASSIC_BURGUNDY_TEXT_SPEC.png` — approved live-text specification.
- `design/reference/V1_16_LAYERED_RUNTIME_PREVIEW.png` — preview assembled from the runtime asset layers.
- `app/src/main/res/drawable/tabletop.webp` — gameplay tabletop background.
- `app/src/main/res/drawable-nodpi/premium_*` — HUD/button layer assets.
- `app/src/main/assets/cosmetics/boards/classic_burgundy/board.webp` — replaceable board skin.
- `GameActivity.applyReferenceComposition()` — one 1672×941 master coordinate system.

## v1.18 Match Setup
- `ui/MatchSetupActivity.java`
  - Builds VS Bot / Local 2-player setup dynamically.
  - Uses `SlidingChoice` instead of Android Spinner/dropdown menus.
  - Owns game-version-specific rule summaries and setup labels.
  - Opponent colour selector: Auto / Light / Dark.
- `cosmetics/CheckerTheme.java`
  - `SIDE_LIGHT` / `SIDE_DARK` metadata tags.
  - light/dark tags are gameplay-side metadata, not literal visual colours.
- `ui/CustomiseActivity.java` + `activity_customise.xml`
  - Persists player's preferred checker gameplay side as `checker_side` in `backgammon_visuals`.


## v1.19 SETUP CHROME
- Setup screen logic: `app/src/main/java/com/george/backgammon/ui/MatchSetupActivity.java`
- 3D setup raster assets: `app/src/main/res/drawable-xxhdpi/setup_*`
- Setup selectors/dividers: `app/src/main/res/drawable/setup_*`
- Layout geometry remains the approved v1.18 programmatic setup layout.

## v1.20 geometry + blend files
- Board map calibration: `app/src/main/java/com/george/backgammon/rendering/BoardMap.java`
- Checker rendering: `app/src/main/java/com/george/backgammon/rendering/BackgammonBoardView.java`
- Clean Classic checker sprites: `app/src/main/assets/cosmetics/checkers/ivory_walnut/light.png`, `dark.png`
- Gameplay HUD chrome: `app/src/main/res/drawable-nodpi/premium_player_left.webp`, `premium_player_right.webp`, `premium_status.webp`, `premium_square_*`, `premium_settings_*`
- Gameplay composition / live icon sizing: `app/src/main/java/com/george/backgammon/ui/GameActivity.java`


## v1.21.0 polished gameplay visual pass
- `assets/cosmetics/boards/classic_burgundy/board.webp`: exact BoardMap-aligned polished board skin.
- `assets/cosmetics/checkers/ivory_walnut/{light,dark}.png`: clean polished production checker sprites.
- `res/drawable-nodpi/premium_*`: gameplay HUD/deck/button chrome.
- `res/drawable-nodpi/die_*.webp`: bottom-deck dice artwork.
- `res/drawable/hud_avatar_ring*.xml`: live identity icon rings.
- Live text and gameplay state remain code-driven; artwork contains no dynamic text.


### v1.22.0 rendering lock
- `StaticBoardRenderer.drawGeometryLockedPlayfield()` repaints Classic Burgundy field/points from BoardMap after the material board bitmap.
- Dice-well placement lives in `GameActivity.layoutGameplayForSize()` and is asset-derived.


## v1.23 Geometry rule
- Classic Burgundy board asset is FRAME/MATERIAL ONLY: no baked point triangles.
- BoardMap field/bar boundaries generate all 12 top and 12 bottom point bases at runtime.
- Point bases partition each side into six exact columns and cannot enter trays/bar.
- Checkers and hit regions derive from the same generated point geometry.


## v1.24 realistic material pipeline
- Board geometry remains owned only by `rendering/BoardMap.java`.
- Material paths live on `cosmetics/BoardTheme.java`.
- Runtime material clipping/rendering lives in `rendering/StaticBoardRenderer.java`.
- Classic Burgundy material assets: `app/src/main/assets/cosmetics/boards/classic_burgundy/materials/`.
- Never bake point geometry into material textures. Materials tile inside BoardMap-defined shapes.


## v1.25.0 Classic Wood starter
- Default checker set id remains `checkers_ivory_walnut` for save compatibility; display name is now **Classic Wood**.
- Top-down assets: `assets/cosmetics/checkers/ivory_walnut/light.png` and `dark.png`.
- Bear-off side assets: `light_side.png` and `dark_side.png`.
- LIGHT/DARK side metadata remains the source for Auto opponent colour selection.
- App-wide tabletop: `res/drawable/tabletop.webp` is now the clean emerald cloth reference with no decorative objects.
- v1.23 BoardMap / point-grid geometry is unchanged.

## v1.26 starter cleanup
- `tools/build_classic_wood_checkers.py` rebuilds the paired starter top-down/side-view checker assets with shared geometry.
- `cosmetics/CosmeticCatalog.java`: Classic Burgundy leaves light/dark point texture assets null so starter points render as simple gradients.
- `rendering/StaticBoardRenderer.java`: simple point fallback; no starter diamond/pattern treatment.
- `ui/GameActivity.java`: bottom main/Undo/Hint frames are snapped to the exact `premium_bottom_deck.webp` wells.
