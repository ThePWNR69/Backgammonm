# Backgammon Legacy — locked production standards (v1.6.5)

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
- burgundy leather player panels and bottom controls with restrained antique-gold edging
- faceted brushed-gold turn/status plaque
- dark secondary Undo/Hint buttons with gold trim
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

## 15. Five-checker point coverage (v1.6.5)

Checker-to-point geometry is now a locked renderer invariant:

- The gameplay checker diameter is **90% of the narrowest visible triangle base width**.
- A five-checker stack begins flush with the triangle base and spans the complete triangle height.
- The fifth checker reaches and slightly covers the apex so the triangle tip cannot remain visible after scaling or anti-aliasing.
- The required centre-to-centre pitch is calculated independently for each measured point from its real base-to-apex height.
- The stack remains centred on the measured midpoint between the two triangle edges at each checker Y position.
- 1-4 checkers use the same pitch; 6+ continues to use the count-badge treatment until a later high-stack design is approved.

Reference design relationship: a **100 px** triangle base uses a **90 px** checker. For a **278 px** point height, five 90 px checkers use approximately **47.56 px centre-to-centre pitch**, including the small apex-cover guard.

Do not tune checker size or stack spacing independently per cosmetic theme.


## 16. v1.10.0 approved gameplay-layout override

For the **live match screen only**, sections 12–13's older uniform-aspect gameplay-board rule are superseded by `APPROVED_GAMEPLAY_LAYOUT.md` and `design/reference/CLASSIC_BURGUNDY_APPROVED.png`.

The approved match screen is a single **510 × 280 logical composition**. The board occupies `0,44,510,178` inside that composition and the canonical 2048×977 `BoardMap` is projected independently on X/Y into that exact rectangle. This intentional presentation projection is what makes the runtime screen match the approved wide/short concept while preserving one map for artwork, anchors, hit regions and animation endpoints.

The board source assets themselves remain 2048×977 and Store/Customise previews may still show the canonical source aspect. Do not change the live gameplay rectangle without replacing the approved reference and updating `APPROVED_GAMEPLAY_LAYOUT.md` in the same release.

Classic Burgundy UI chrome is stored as raster theme resources at 4× logical size in `res/drawable-nodpi/premium_*`. Button state selectors must use Normal / Pressed / Disabled assets rather than substituting generic Android button styling.

## 17. v1.11.0 slim-chrome gameplay override

v1.11.0 supersedes the v1.10.0 live-match rectangles in section 16 after in-game review showed the 42-unit top HUD and 56-unit bottom deck visually compressing the board.

The overall logical frame remains **510 × 280**, but the live match now uses:
- top controls: **30** logical units high,
- live board: `0,32,510,207`,
- bottom control deck: **39** logical units high.

The board must remain the dominant visual element. Do not increase HUD/control height to solve text or icon problems; refine typography/assets within the locked chrome instead.

Classic Burgundy chrome uses thin double antique-gold edges, restrained leather texture, compact faceted score/turn treatments and no glow. Normal / Pressed / Disabled button states remain mandatory.


## 18. v1.12.0 detailed premium chrome lock

v1.12.0 keeps every v1.11 runtime rectangle unchanged. The screenshot at `design/reference/V1_12_SIZE_REFERENCE.jpg` is the accepted size/proportion reference; future polish work must not make the top or bottom chrome taller unless a later explicit design decision replaces this lock.

Classic Burgundy detail requirements:
- layered antique-gold outer rim plus darker inner hairline,
- subtle leather grain and restrained stitched seams,
- faceted recessed score bays,
- small metal rivet/stud details used sparingly,
- a brushed-metal centre turn plaque with a subtle inset title plate,
- recessed framed dice wells in the bottom deck,
- visual separators between dice / primary action / utility control zones,
- Normal / Pressed / Disabled button assets with shadow/compression changes,
- no neon glow or oversized ornamentation.

The board remains the hero. Additional detail should come from material treatment, depth, fine trim, typography and icon polish rather than increasing chrome size.
