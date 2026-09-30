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
