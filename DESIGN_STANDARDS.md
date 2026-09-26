# Backgammon Legacy — locked production standards (v1.6.4)

These are architecture rules, not per-theme preferences. Future boards/checkers must follow them automatically.

## 1. One permanent invisible BoardMap

Gameplay geometry lives in `rendering/BoardMap.java` and is projected by `BoardGeometry.java`.

The map owns:
- all 24 point locations
- stack anchors
- bar anchors
- bear-off tray anchors
- touch/hit regions
- movement/animation start and end positions

Board artwork never supplies gameplay coordinates.

## 2. Master board artwork

Production board size: **2048 × 977 px** (aspect ~**2.096:1**).

Every board image must retain the same:
- 24 triangle positions
- point bases and tips
- centre bar
- left/right trays
- playing-field rectangle
- overall aspect ratio

Only materials, colours, ornamentation and lighting may change.

Runtime protection: `StaticBoardRenderer` accepts a production board only when it matches the master dimensions exactly. An invalid board falls back to the debug renderer rather than silently misaligning gameplay.

### Canonical pixel regions
- Field envelope: x **236–1840**, y **58–916**
- Left tray: x **78–219**
- Right tray: x **1849–1976**
- Bar: x **996–1085**
- Point height: measured per triangle from its base to its apex (approximately **358–392 px**)

## 3. Layering

Runtime stack:
1. premium tabletop/background artwork
2. selected static board image
3. invisible BoardMap (logic/hitboxes)
4. checkers
5. dice
6. move indicators
7. movement/dice animations

Board art contains no live checker/dice state.

## 4. Checker sizing

Source checker canvas: **512 × 512 px**.
Target visible checker content: **448 px diameter**, centred.

On-screen checker size comes only from `BoardGeometry`. Theme artwork cannot make one set larger/smaller.
`AssetTextureRepository.visibleBounds()` trims source transparent padding at runtime before fitting artwork to the same render square.

## 5. Stack standard

- Up to five physical checker positions are shown on a point.
- Five visible checkers finish at approximately the point tip.
- 6+ uses a count badge instead of extending into the centre.

## 6. Shared previews

Gameplay, Store and Customise use the same board image, BoardMap and checker renderer. No hand-positioned preview pieces and no alternate preview geometry.

## 7. Visual target

The approved premium gameplay reference is the production target for Classic Walnut:
- deep walnut case with physical bevel/depth
- black leather playing surface
- cream + walnut points
- polished ivory/walnut checkers
- brass/gold trim
- dark emerald luxury tabletop
- dark brown player panels and emerald/gold status/action controls
- compact UI so the board remains the focus

## 8. New board workflow

1. Start from the 2048×977 canonical template/regions above.
2. Never move any gameplay geometry.
3. Redesign only the materials/art treatment.
4. Export to `app/src/main/assets/cosmetics/boards/<id>/board.webp`.
5. Register the theme in `CosmeticCatalog`.
6. Verify with in-game **Show Board Map** before release.

## 9. New checker workflow

1. Use a 512×512 transparent canvas.
2. Centre the visible disc to the standard bounds.
3. Export `light.webp` and `dark.webp`.
4. Register the set in `CosmeticCatalog`.
5. Confirm it renders the same diameter as every existing set.

## 10. GitHub browser upload limit

The distributable source package must remain below **100 files**. Reuse assets and render previews from production assets rather than adding duplicate preview files.

## Triangle-centred checker placement

Checker X positions are **not** a generic column centre. Each production point has measured base-left, base-right and apex coordinates. At every checker slot Y, the renderer interpolates both sloping triangle edges and places the checker at their exact midpoint. The same rule applies to landing previews, move indicators and animation endpoints. Future board artwork must preserve these triangle vertices.
## 11. App-wide premium UI system

The approved VS Bot Setup and gameplay HUD are the visual source of truth for non-board screens. All screens should use the same design language:
- dark emerald tabletop/background
- walnut/brown framed panels
- cream/gold serif headings
- emerald primary controls with gold borders
- dark secondary controls with gold borders
- consistent rounded back button, tabs, cards and score badges
- restrained decorative elements; the board/content remains the focus
- no low-contrast grey/green body text when warm cream/gold is clearer

Gameplay player nameplates show only the checker icon, player name and rounds won. Do not reintroduce OFF/BAR/colour/direction text into the nameplates.

Setup screens use the same two-panel pattern: settings on the left, rules/summary on the right, with a single strong emerald START MATCH action. Layouts must remain compact enough for landscape phones and must not clip at the bottom.


## 12. Gameplay HUD proportions

The live match screen remains board-first. On extra-wide phones the physical board must be
given a rectangle that already matches the production **2048:977** aspect ratio; do not
stretch a board-sized View and rely on internal letterboxing.

Top name/status plates and bottom controls should remain compact and share a common control
height. Horizontal spacing can still derive from the approved 1672-wide reference. Vertical
spacing is allowed to compress on short/wide phones so the board gets the maximum practical
height.

## 13. Uniform board scaling (v1.6.4)

For gameplay:

- determine the safe landscape window,
- reserve compact top HUD and bottom-control bands,
- calculate the remaining board slot height,
- size the board with one uniform scale using `BoardMap.MASTER_ASPECT`,
- centre the board horizontally,
- never independently scale board width and height.

HUD text/icons may use their own UI scale, but the board art, point map, checker anchors,
hitboxes and animation endpoints must all share the final board rectangle.

## 14. Symmetric master board art (v1.6.4)

Production board size remains **2048 × 977 px**. The v1.6.4 master geometry is horizontally
rebalanced around the centre bar. All production board themes use that same rebalance, and
`BoardMap` stores the matching post-rebalance coordinates.

Do not introduce a board skin based on the pre-v1.6.4 horizontal geometry. A new board must
be authored/aligned to the current production template and validated with **Show Board Map**.

## 15. Gameplay UI polish rules

- Opening dice may be temporarily enlarged and centred for the opening-roll presentation.
- Normal-turn dice return to the standard right-centre board position.
- Opening-roll result text should appear briefly before AI movement begins.
- Main action, Undo and Menu use one control height and one corner family.
- Player plates contain checker identity, player name and match score only.
- Bear-off trays may use a restrained `OFF` label; avoid adding dense metadata to the board.
- The live gameplay background should remain visually quiet enough that the board is the hero.
