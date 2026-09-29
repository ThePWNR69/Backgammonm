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


CLASSIC BURGUNDY PREVIEW LOCK
- approved static preview source: /mnt/data/luxurious_backgammon_board_interface.png
- HUD chrome assets: app/src/main/res/drawable-nodpi/premium_*
- board skin: app/src/main/assets/cosmetics/boards/classic_burgundy/board.webp
