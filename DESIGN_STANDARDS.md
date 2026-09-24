# Backgammon Legacy — locked production standards

These rules are part of the renderer architecture. New boards/checkers should follow them automatically; they are not per-theme preferences.

## 1. One permanent invisible board map

Gameplay geometry lives in `rendering/BoardMap.java` and is projected by `BoardGeometry.java`.

The map owns:
- all 24 point locations
- stack anchors
- bar anchors
- bear-off tray anchors
- touch/hit regions
- animation start/end positions

A cosmetic board **must never** supply gameplay coordinates.

## 2. Master board artwork

Production board size: **1860 × 1000 px** (aspect **1.86:1**).

Every board image is authored against `production_templates/MASTER_BOARD_TEMPLATE.png` and must retain the same:
- point bases and tips
- centre bar
- trays
- playing-field rectangle
- overall aspect ratio

The visible materials can change completely. The geometry cannot.

Runtime protection: `StaticBoardRenderer` only accepts a production board asset when it is exactly the master size. Invalid art falls back to the debug/procedural renderer instead of silently misaligning gameplay.

## 3. Board layering

Runtime stack:

1. premium background/table surface
2. selected static board image
3. invisible BoardMap (logic/hitboxes; not visible)
4. checkers
5. dice
6. move indicators
7. movement/dice animations

Board art contains no checker or dice state.

## 4. Checker sizing

Source checker canvas: **512 × 512 px**.
Target visible checker content: **448 px diameter**, centred.

On-screen checker diameter is controlled only by `BoardGeometry`. Theme artwork cannot make a checker larger or smaller.

`AssetTextureRepository.visibleBounds()` additionally trims source transparent padding at runtime and maps the visible checker into the same square render bounds. This prevents future checker assets with different padding from displaying at different sizes.

## 5. Stack standard

- Up to five physical checker positions are shown on a point.
- Five visible checkers finish at approximately the triangle tip.
- 6+ uses the stack count badge rather than extending into the centre of the board.

## 6. Previews

Store and Customise previews must use the same board image, `BoardMap`, checker renderer and standard sample position as gameplay. Do not hand-place preview checkers or create alternate triangle geometry.

## 7. New cosmetic workflow

### New board
1. Start from `MASTER_BOARD_TEMPLATE.png`.
2. Keep all geometry untouched.
3. Replace only materials/art treatment.
4. Export to `app/src/main/assets/cosmetics/boards/<id>/board.webp` at 1860 × 1000.
5. Register the theme in `CosmeticCatalog`.

### New checker set
1. Start from `MASTER_CHECKER_TEMPLATE.png`.
2. Keep the 512 × 512 canvas.
3. Centre the visible disc to the standard bounds.
4. Export transparent `light.webp` and `dark.webp`.
5. Register the set in `CosmeticCatalog`.

## 8. Validation / developer overlay

In-game menu → **Show Board Map** displays the canonical point anchors and critical regions over the selected artwork. Use it whenever a new board is introduced.
